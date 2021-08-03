package edu.uw.ictd;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import edu.uw.ictd.ccis.CCDBRow;
import edu.uw.ictd.ccis.CCISSyncer;
import edu.uw.ictd.odk.SuitcaseRunner;
import edu.uw.ictd.odk.SyncEndpointDownloader;
import io.sentry.Sentry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opendatakit.aggregate.odktables.rest.entity.RowResource;
import org.opendatakit.aggregate.odktables.rest.entity.RowResourceList;
import org.opendatakit.sync.client.SyncClient;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

public class ColdChainVizSyncUtil {
  static final String TABLES = "tables";
  static final String ROWS = "rows";

  private static final String EXCEPTION_WHILE_SYNCING = "Exception while syncing";
  private static final String ERROR_WHILE_CLOSING_SQL_OR_WINK_CLIENT_CONNECTION = "Error while "
      + "closing SQL or Wink Client connection";
  private static final String PROCESSING_AGGREGATE_TABLE_S = "Processing Aggregate table %s";
  private static final String INSERT_INTO_S_IN_REMOTE_DB = "Insert into %s in remote DB";
  private static final String DELETION_COUNT_D = "Deletion count: %d";
  private static final String NO_DATA_IN_S = "No data in %s";

  private static final String SENTRY_DNS = "https://53deb3a1623645cd9ae41e27d78813c8@o175243.ingest.sentry.io/5453719";
  private static final String COULD_NOT_CREATE_LOG_FILE_DIRECTORY_S = "Could not create log file "
      + "directory %s";

  private static final String CONNECTING_TO_DATABASE = "Connecting to database...";
  private static final String SYNC_COMPLETED = "Sync completed";
  private static Logger logger = null;

  private static final String INSERT_ROWS_ERROR_ROW_DATA_IS_NULL = "Insert rows error - rowData is null";


  final Map<String, String> tableNameMap = new HashMap<>();
  private final ArrayList<TablesToConvert> tablesToConvertList = new ArrayList<>();
  private final ArrayList<String> blackListedTablesList = new ArrayList<>();
  private final Map<String, Map<String, DatabaseColumn>> dbColLookup = new HashMap<>();

  private ConfigSettings configSettings = null;

  private String datedCsvDir = null;

  String defaultConfigPath = "config";

  private String configFileName = "config.txt";
  private String blackListedTablesFileName = "blacklisted_tables.txt";
  private String tablesToConvertFileName = "tables_to_convert.csv";

  Map<String, String> tableIdToSchemaETagMap = new HashMap<>();
  List<String> tableIdList = new ArrayList<>();

  CCISSyncer ccisSyncer;
  SyncEndpointDownloader syncDownloader;
  String configDirPath;

  ColdChainVizSyncUtil() {
  }

  ColdChainVizSyncUtil(String configDirPath) {
    this.configDirPath = configDirPath;
  }

  private static String exceptionStacktraceToString(Exception e) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    e.printStackTrace(ps);
    ps.close();
    return baos.toString();
  }

  void initCCISSyncer(String dbUrl, String dbUsername, String dbPassword, String defaultTZ,
      Logger logger)
      throws SQLException,
      ClassNotFoundException  {
    ccisSyncer = new CCISSyncer(dbUrl, dbUsername, dbPassword, defaultTZ, logger);
  }

  void initSyncDownloader(String aggUrl, String username, String password, String appId)
      throws MalformedURLException {
    syncDownloader = new SyncEndpointDownloader(aggUrl, username, password, appId);
  }

  void initFilesInConfigDir() throws IOException {
    String configPathToUse = defaultConfigPath;
    if (configDirPath != null && configDirPath.length() > 0) {
      configPathToUse = configDirPath;
    }

    String configFilePath = configPathToUse + File.separator + configFileName;
    initConfigValues(configFilePath);

    String blacklistedFilePath = configPathToUse + File.separator + blackListedTablesFileName;
    initBlacklistedTables(blacklistedFilePath);

    String tablesToConvertFilePath = configPathToUse + File.separator + tablesToConvertFileName;
    initTableValues(tablesToConvertFilePath);
  }

  void sync() {

    Connection conn = null;
    SyncClient sc = null;

    try {
      initSentry();
      initFilesInConfigDir();
      TimeZone.setDefault(TimeZone.getTimeZone(configSettings.getDefaultTZ()));

      // Directory to write out all csv files to
      CustomDate currentTime = new CustomDate("yyyy-MM-dd_HH_mm_ss",
          TimeZone.getTimeZone(configSettings.getLogTZ()));
      String currentTimeStr = currentTime.getUtilString();
      setDatedCsvDir(currentTimeStr);

      File datedLogDir = new File(getDatedCsvDir());
      if (!datedLogDir.exists()) {
        boolean mkDirs = datedLogDir.mkdirs();
        if (!mkDirs) {
          throw new IOException(String.format(COULD_NOT_CREATE_LOG_FILE_DIRECTORY_S,
              datedLogDir.getName()));
        }
      }
      System.setProperty("datedLogDir", getDatedCsvDir());
      initLogger(getDatedCsvDir());

      URL url = new URL(configSettings.getAggUrl());

      // Setup the sql driver
      logger.info(CONNECTING_TO_DATABASE);
      initCCISSyncer(configSettings.getDbUrl(), configSettings.getDbUsername(),
          configSettings.getDbPassword(), configSettings.getDefaultTZ(), logger);

      initSyncDownloader(configSettings.getAggUrl(), configSettings.getAggUsername(),
          configSettings.getAggPassword(), configSettings.getAppId());

      // Download sync-endpoint data into csv files
      // in directory with logging
      createCSVFilesForAllData(getDatedCsvDir());

      // Loop through each table - delete table and then upload data
      moveODKDataToColdChainVizDB();

      logger.info(SYNC_COMPLETED);

    } catch (Exception e) {
      logger.error(EXCEPTION_WHILE_SYNCING, e);
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }

        if (sc != null) {
          sc.close();
        }
      } catch (Exception e) {
        logger.error(ERROR_WHILE_CLOSING_SQL_OR_WINK_CLIENT_CONNECTION,
            exceptionStacktraceToString(e));
      }
    }
  }

  private void initSentry() {
    Sentry.init(options -> {
      options.setDsn(SENTRY_DNS);
    });
  }

  void initConfigValues(String configFilePathToUse) throws FileNotFoundException {
    // Get the properties from the config file
    configSettings = new ConfigSettings(configFilePathToUse);
  }

  void initBlacklistedTables(String pathToBlacklistedFile) throws FileNotFoundException {
    Scanner lineScan = new Scanner(new File(pathToBlacklistedFile));

    while (lineScan.hasNext()) {
      blackListedTablesList.add(lineScan.next());
    }

    lineScan.close();
  }

  void initTableValues(String pathToTableValuesFile) throws FileNotFoundException, IOException {
    CsvMapper csvMapper = new CsvMapper();
    CsvSchema schema = CsvSchema.emptySchema().withHeader();

    ObjectReader oReader = csvMapper.reader(TablesToConvert.class).with(schema);
    try (Reader reader = new FileReader(pathToTableValuesFile)) {
      MappingIterator<TablesToConvert> mi = oReader.readValues(reader);
      tablesToConvertList.addAll(mi.readAll());
    }

    for (TablesToConvert tblToConvert : tablesToConvertList) {
      if (!tableNameMap.containsKey(tblToConvert.sourceTable)) {
        tableNameMap.put(tblToConvert.sourceTable, tblToConvert.destinationTable);
      }

      dbColLookup.putIfAbsent(tblToConvert.destinationTable, new LinkedHashMap<>());

      Map<String, DatabaseColumn> srcToDestColMap = dbColLookup.get(tblToConvert.destinationTable);
      DatabaseColumn dbCol = new DatabaseColumn(tblToConvert.targetField, tblToConvert.fieldType);
      srcToDestColMap.put(tblToConvert.sourceField, dbCol);
    }
  }

  void initLogger(String dirToUse) {
    // Initializes the logger to be used
    String logFileName = "log.txt";
    logFileName = (dirToUse != null && dirToUse.length() > 0) ?
        dirToUse + logFileName :
        logFileName;

    // Set property for log4j2.xml file in resources dir
    System.setProperty("datedLogFile", logFileName);
    logger = LogManager.getLogger(ColdChainVizSyncUtil.class);
  }

  Map<String, DatabaseColumn> getTableColMap(String tableId) {
    return dbColLookup.getOrDefault(tableId, null);
  }

  private void insertSingleConvertedODKRowToCCDBRow(String destTable, RowResource row)
      throws SQLException, ParseException {

    if (destTable == null) {
      return;
    }

    Map<String, DatabaseColumn> destTableColMap = getTableColMap(destTable);
    if (destTableColMap == null) {
      return;
    }

    // Convert row
    CCDBRow ccdbRow = RowConverter.convertODKRowToCCDBRow(destTable, row, destTableColMap);

    // Insert row
    ccisSyncer.insertSingleCCDBRow(destTable, ccdbRow, destTableColMap, row.getRowId(),
        row.getRowETag(), logger);
  }

  private void createCSVFilesForAllData(String dirToUse) throws Exception {

    // Get all the tables that need to be moved to Aggregate
    tableIdList = syncDownloader.getTableIds();

    for (String tableId : tableIdList) {
      String csvFilePath = dirToUse + tableId;
      SuitcaseRunner.runSuitcaseDownloadCommand(configSettings.getAggUrl(), csvFilePath,
          configSettings.getAppId(), tableId, configSettings.getAggUsername(),
          configSettings.getAggPassword(), logger);
    }
  }

  private void moveODKDataToColdChainVizDB() throws Exception {
    // Get all the tables that need to be moved to Aggregate
    tableIdToSchemaETagMap = syncDownloader.getTablesAndSchemaETags();

    for (String tableId : tableIdList) {
      String schemaETag = tableIdToSchemaETagMap.get(tableId);

      // Check if the table is black listed
      boolean blackListedTable = isTableBlackListed(tableId);

      // Only process the table if it is not black listed
      if (!blackListedTable) {
        processTable(tableId, schemaETag);
      }
    }
  }

  public void processTable(String tableId, String schemaETag)
      throws Exception {

    logger.info(String.format(PROCESSING_AGGREGATE_TABLE_S, tableId));

    // Delete all rows
    String destTable = tableNameMap.getOrDefault(tableId, null);
    int delCnt = deleteAllRowsInColdChainTable(destTable);
    logger.info(String.format(DELETION_COUNT_D, delCnt));

    // Insert rows using fetchLimit
    String fetchLimit = "1000";
    String cursor = null;
    String dataETag = null;
    boolean hasMoreResults;

    do {
      RowResourceList rowResourceList = syncDownloader.getRows(tableId, schemaETag, cursor,
          fetchLimit);

      hasMoreResults = rowResourceList.isHasMoreResults();
      cursor = rowResourceList.getWebSafeResumeCursor();

      // Make sure that data ETag is not null
      dataETag = rowResourceList.getDataETag();
      if (dataETag != null && dataETag.length() > 0) {
        logger.info(String.format(INSERT_INTO_S_IN_REMOTE_DB, tableId));
        insertRowsIntoColdChainTable(rowResourceList.getRows(), destTable);
      } else {
        // otherwise the table has no rows
        logger.info(String.format(NO_DATA_IN_S, tableId));
      }
    } while (hasMoreResults);
  }

  boolean isTableBlackListed(String tableId) {
    boolean tableIsBlackListed = false;

    for (String s : blackListedTablesList) {
      if (tableId.equals(s)) {
        tableIsBlackListed = true;
      }
    }

    return tableIsBlackListed;
  }

  int deleteAllRowsInColdChainTable(String tableId) throws SQLException {
    return ccisSyncer.deleteAllRowsInColdChainTable(tableId);
  }

  void insertRowsIntoColdChainTable(List<RowResource> rowResourceList, String tableId)
      throws SQLException, ParseException, RuntimeException {

    if (rowResourceList == null) {
      throw new IllegalArgumentException(INSERT_ROWS_ERROR_ROW_DATA_IS_NULL);
    }

    if (rowResourceList.size() <= 0) {
      // No rows to add or delete out
      return;
    }

    insertBatchODKRowsToCCDBRows(tableId, rowResourceList);
  }

  private void insertBatchODKRowsToCCDBRows(String destTable, List<RowResource> rowResourceList)
      throws SQLException, ParseException {
    if (destTable == null) {
      return;
    }

    Map<String, DatabaseColumn> destTableColMap = getTableColMap(destTable);
    if (destTableColMap == null) {
      return;
    }

    ArrayList<CCDBRow> ccdbRowList = new ArrayList<>();
    for (RowResource row : rowResourceList) {
      // Convert row
      CCDBRow ccdbRow = RowConverter.convertODKRowToCCDBRow(destTable, row, destTableColMap);
      ccdbRowList.add(ccdbRow);
    }
    // Insert converted CCDB rows
    ccisSyncer.insertBatchCCDBRows(destTable, ccdbRowList, destTableColMap, logger);
  }

  private String getDatedCsvDir() {
    return datedCsvDir;
  }

  /**
   * Convenience function to set the datedCsvDir Useful for testing
   */
  private void setDatedCsvDir(String currentTimeStr) {
    if (currentTimeStr != null) {
      datedCsvDir = configSettings.getCsvDir() + File.separator + currentTimeStr + File.separator;
    }
  }

  public ConfigSettings getConfigSettings() {
    return configSettings;
  }

  public Logger getLogger() {
    return logger;
  }
}
