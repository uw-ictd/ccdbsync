package edu.uw.ictd;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uw.ictd.constants.RowResourceMetadataConstants;
import junit.framework.TestCase;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendatakit.aggregate.odktables.rest.entity.DataKeyValue;
import org.opendatakit.aggregate.odktables.rest.entity.RowResource;
import org.opendatakit.sync.client.SyncClient;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.*;

import static edu.uw.ictd.ColdChainVizSyncUtil.*;
import static edu.uw.ictd.ccis.CCISSyncer.POSTGRESQL_DRIVER;

public class ColdChainSyncVizUtilTest {

  String configDirPath = "src" + File.separator + "test" + File.separator + "config";
  String configFilePath =
      "src" + File.separator + "test" + File.separator + "config" + File.separator + "config.txt";
  String blacklistedFilePath =
      "src" + File.separator + "test" + File.separator + "config" + File.separator + "blacklisted_tables.txt";
  String tablesToConvertFilePath =
      "src" + File.separator + "test" + File.separator + "config" + File.separator + "tables_to_convert.csv";

  String testData = "src" + File.separator + "test" + File.separator + "data" + File.separator +
      "refrigerator_row.json";

  String modTestData = "src" + File.separator + "test" + File.separator + "data" + File.separator +
      "mod_refrigerator_row.json";

  @BeforeEach
  public void beforeEachTest() throws ClassNotFoundException, SQLException {

  }

  @Test
  void verifyRowOfDataAddedCorrectly() {
    ColdChainVizSyncUtil ccUtil = new ColdChainVizSyncUtil(configDirPath);

    try {
      // Init config values - set logging
      ccUtil.initFilesInConfigDir();
      ccUtil.initLogger(null);

      ConfigSettings configSettings = ccUtil.getConfigSettings();
      ccUtil.initCCISSyncer(configSettings.getDbUrl(), configSettings.getDbUsername(),
          configSettings.getDbPassword(), configSettings.getDefaultTZ(), ccUtil.getLogger());

      // Setup the sql driver
      Class.forName(POSTGRESQL_DRIVER);
      Connection conn = DriverManager.getConnection(configSettings.getDbUrl(),
          configSettings.getDbUsername(), configSettings.getDbPassword());

      Scanner scanner = new Scanner(new File(testData));
      String jsonArray = null;
      if (scanner.hasNext()) {
        jsonArray = scanner.next();
      }

      JSONArray rows = new JSONArray(jsonArray);
      List<RowResource> rowResourceList = new ArrayList<>();
      RowResource currRow = RowConverter.convertJSONRowToODKRow(rows.getJSONObject(0));
      rowResourceList.add(currRow);

      String tableId = "refrigerators_odkx";
      ccUtil.deleteAllRowsInColdChainTable(tableId);
      ccUtil.insertRowsIntoColdChainTable(rowResourceList, tableId);

      Statement queryStmt = conn.createStatement();
      ResultSet rs = queryStmt.executeQuery("SELECT * FROM " + tableId);

      Assert.assertTrue(isServerAndDBRowEqual(ccUtil, tableId, rs, currRow));

    } catch (Exception e) {
      e.printStackTrace();
      TestCase.fail();
    }
  }

  @Test
  void verifyRowOfDataUpdatedCorrectly() {
    ColdChainVizSyncUtil ccUtil = new ColdChainVizSyncUtil(configDirPath);

    try {
      // Init config values - set logging
      ccUtil.initFilesInConfigDir();
      ccUtil.initLogger(null);

      ConfigSettings configSettings = ccUtil.getConfigSettings();
      ccUtil.initCCISSyncer(configSettings.getDbUrl(), configSettings.getDbUsername(),
          configSettings.getDbPassword(), configSettings.getDefaultTZ(), ccUtil.getLogger());

      // Setup the sql driver
      Class.forName(POSTGRESQL_DRIVER);
      Connection conn = DriverManager.getConnection(configSettings.getDbUrl(),
          configSettings.getDbUsername(), configSettings.getDbPassword());

      Scanner scanner = new Scanner(new File(testData));
      String jsonArray = null;
      if (scanner.hasNext()) {
        jsonArray = scanner.next();
      }

      JSONArray rows = new JSONArray(jsonArray);
      List<RowResource> rowResourceList = new ArrayList<>();
      RowResource currRow = RowConverter.convertJSONRowToODKRow(rows.getJSONObject(0));
      rowResourceList.add(currRow);

      String tableId = "refrigerators_odkx";
      ccUtil.deleteAllRowsInColdChainTable(tableId);
      ccUtil.insertRowsIntoColdChainTable(rowResourceList, tableId);

      Statement queryStmt = conn.createStatement();
      ResultSet rs = queryStmt.executeQuery("SELECT * FROM " + tableId);

      Assert.assertTrue(isServerAndDBRowEqual(ccUtil, tableId, rs, currRow));

      Scanner modScanner = new Scanner(new File(modTestData));

      String modJsonArray = null;
      if (modScanner.hasNext()) {
        modJsonArray = modScanner.next();
      }

      JSONArray modRows = new JSONArray(modJsonArray);
      List<RowResource> modRowResourceList = new ArrayList<>();
      RowResource currModRow = RowConverter.convertJSONRowToODKRow(modRows.getJSONObject(0));
      rowResourceList.add(currRow);

      ccUtil.deleteAllRowsInColdChainTable(tableId);
      ccUtil.insertRowsIntoColdChainTable(modRowResourceList, tableId);

      Statement modQueryStmt = conn.createStatement();
      ResultSet modRS = modQueryStmt.executeQuery("SELECT * FROM " + tableId);

      Assert.assertTrue(checkValueInODKRow(currModRow, "notes", "new_notes"));

      Assert.assertTrue(isServerAndDBRowEqual(ccUtil, tableId, modRS, currModRow));

    } catch (Exception e) {
      e.printStackTrace();
      TestCase.fail();
    }
  }

  public boolean checkValueInODKRow(RowResource rowResource, String colName, String value) {
    List<DataKeyValue> dataKeyValueList = rowResource.getValues();
    for (int i = 0; i < dataKeyValueList.size(); i++) {
      DataKeyValue dkv = dataKeyValueList.get(i);
      if (dkv.column.equals(colName)) {
        if (dkv.value.equals(value)) {
          return true;
        }
      }
    }

    return false;
  }

  @Test
  void verifyRowOfDataDeletedCorrectly() {
    ColdChainVizSyncUtil ccUtil = new ColdChainVizSyncUtil(configDirPath);
    ObjectMapper objMapper = new ObjectMapper();

    try {
      // Init config values - set logging
      ccUtil.initFilesInConfigDir();
      ccUtil.initLogger(null);

      ConfigSettings configSettings = ccUtil.getConfigSettings();
      ccUtil.initCCISSyncer(configSettings.getDbUrl(), configSettings.getDbUsername(),
          configSettings.getDbPassword(), configSettings.getDefaultTZ(), ccUtil.getLogger());

      // Setup the sql driver
      Class.forName(POSTGRESQL_DRIVER);
      Connection conn = DriverManager.getConnection(configSettings.getDbUrl(),
          configSettings.getDbUsername(), configSettings.getDbPassword());

      Scanner scanner = new Scanner(new File(testData));
      String jsonArray = null;
      if (scanner.hasNext()) {
        jsonArray = scanner.next();
      }

      JSONArray rows = new JSONArray(jsonArray);
      List<RowResource> rowResourceList = new ArrayList<>();
      RowResource currRow = RowConverter.convertJSONRowToODKRow(rows.getJSONObject(0));
      rowResourceList.add(currRow);

      String tableId = "refrigerators_odkx";
      ccUtil.deleteAllRowsInColdChainTable(tableId);
      ccUtil.insertRowsIntoColdChainTable(rowResourceList, tableId);

      Statement queryStmt = conn.createStatement();
      ResultSet rs = queryStmt.executeQuery("SELECT * FROM " + tableId);

      Assert.assertTrue(isServerAndDBRowEqual(ccUtil, tableId, rs, currRow));

      JSONArray noRows = new JSONArray();

      ccUtil.deleteAllRowsInColdChainTable(tableId);
      ccUtil.insertRowsIntoColdChainTable(noRows, tableId);

      Statement emptyQueryStmt = conn.createStatement();
      ResultSet noRS = emptyQueryStmt.executeQuery("SELECT * FROM " + tableId);


      JSONObject noRowJson = noRows.length() > 0 ? noRows.getJSONObject(0) : null;
      RowResource noRow = noRowJson == null ? null :
          objMapper.readerFor(RowResource.class).readValue(noRowJson.toString());

      Assert.assertTrue(isServerAndDBRowEqual(ccUtil, tableId, noRS, noRow));

    } catch (Exception e) {
      e.printStackTrace();
      TestCase.fail();
    }
  }

  @Test
  void verifyLargeDataSet() {

    ColdChainVizSyncUtil ccUtil = new ColdChainVizSyncUtil(configDirPath);

    try {
      ccUtil.sync();

      // Setup the sql driver
      Class.forName(POSTGRESQL_DRIVER);
      ConfigSettings configSettings = ccUtil.getConfigSettings();
      Connection conn = DriverManager.getConnection(configSettings.getDbUrl(),
          configSettings.getDbUsername(), configSettings.getDbPassword());

      // Setup sync client
      URL url = new URL(configSettings.getAggUrl());
      SyncClient sc = new SyncClient();
      sc.init(url.getHost(), configSettings.getAggUsername(), configSettings.getAggPassword());

      // Get all the tables that need to be moved to Aggregate
      JSONObject tablesJSONObj = sc.getTables(configSettings.getAggUrl(), configSettings.getAppId());
      JSONArray tablesJSONArray = tablesJSONObj.getJSONArray(TABLES);

      String tableId;
      String schemaETag;
      boolean blackListedTable = false;
      for (int i = 0; i < tablesJSONArray.size(); i++) {
        JSONObject tableJSONObj = tablesJSONArray.getJSONObject(i);
        tableId = tableJSONObj.getString(SyncClient.TABLE_ID_JSON);
        schemaETag = tableJSONObj.getString(SyncClient.SCHEMA_ETAG_JSON);
        int dbCount = 0, serverCount = 0;

        // Check if the table is black listed
        blackListedTable = ccUtil.isTableBlackListed(tableId);

        // Only process the table if it is not black listed
        if (blackListedTable == false) {

          // Ensure number of rows in db match rows in server
          String destTable = ccUtil.tableNameMap.getOrDefault(tableId, null);
          Statement queryStmt = conn.createStatement();
          ResultSet rs = queryStmt.executeQuery("SELECT COUNT(*) AS ROW_COUNT FROM " + destTable);
          rs.next();
          dbCount = rs.getInt("ROW_COUNT");

          JSONObject tableRows = sc.getRows(configSettings.getAggUrl(), configSettings.getAppId(),
              tableId, schemaETag, null, null);

          serverCount = 0;
          // Make sure that data ETag is not null
          if (!tableRows.isNull(SyncClient.DATA_ETAG_JSON)) {
            JSONArray rows = tableRows.getJSONArray(ROWS);
            serverCount = rows.length();

          }
        }

        Assert.assertEquals(dbCount, serverCount);
      }
    } catch (Exception e) {
      e.printStackTrace();
      TestCase.fail();
    }
  }



  private boolean isServerAndDBRowEqual(ColdChainVizSyncUtil ccUtil, String tableId, ResultSet rs,
      RowResource currRow) throws SQLException {

    if(currRow == null && (rs == null || rs.next() == false)) {
      return true;
    }

    int cnt = 0;
    while (rs.next()) {
      Map<String, DatabaseColumn> destTableColMap = ccUtil.getTableColMap(tableId);

      List<DataKeyValue> dataKeyValueList = currRow.getValues();
      for (int i = 0; i < dataKeyValueList.size(); i++) {
        if (destTableColMap.containsKey(dataKeyValueList.get(i).column)) {
          if (!Objects.equals(dataKeyValueList.get(i).value,
              rs.getString(destTableColMap.get(dataKeyValueList.get(i).column).columnName))) {
            System.out.println(
                "DB Col " + destTableColMap.get(dataKeyValueList.get(i).column).columnName
                    + " does not equal Server Col " + dataKeyValueList.get(i).column);
            System.out.println(
                "DB Value " +
                    rs.getString(destTableColMap.get(dataKeyValueList.get(i).column).columnName)
                    + " does not equal Server " + dataKeyValueList.get(i).value);
            return false;
          }
        }
      }
      for (String srcCol : destTableColMap.keySet()) {
        if (RowResourceMetadataConstants.mapMetadataToRowMethod.containsKey(srcCol)) {
          if (!Objects
              .equals(RowResourceMetadataConstants.mapMetadataToRowMethod.get(srcCol).runCommand(currRow),
                  rs.getString(destTableColMap.get(srcCol).columnName))) {
            System.out.println(
                "DB Col " + destTableColMap.get(srcCol).columnName + " does not equal Server Col "
                    + srcCol);
            return false;
          }
        }
      }

      cnt++;

      if (cnt > 1) {
        System.out.println("Too many rows in db table");
        return false;
      }
    }

    return true;
  }
}
