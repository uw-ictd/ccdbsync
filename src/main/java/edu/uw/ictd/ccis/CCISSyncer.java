package edu.uw.ictd.ccis;

import edu.uw.ictd.CustomDate;
import edu.uw.ictd.DatabaseColumn;
import org.apache.logging.log4j.Logger;
import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.sync.client.SyncClient;

import java.sql.*;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class CCISSyncer {

  public static final String POSTGRESQL_DRIVER = "org.postgresql.Driver";
  private static final String DELETE_FROM_S = "DELETE FROM %s";

  private static final String INSERT_ROWS_ERROR_ROW_DATA_IS_NULL = "Insert rows error - rowData is null";

  private static final String VARCHAR = "VARCHAR";
  private static final String DOUBLE = "DOUBLE";
  private static final String INT = "INT";
  private static final String DATETIME_CAP = "DATETIME";
  private static final String TIME = "TIME";

  private static final String MILLI_TO_NANO_TIMESTAMP_EXTENSION = "000000";

  private static final String INSERT_INTO_S_S_VALUES_S = "INSERT INTO %s (%s) values (%s)";
  private static final String UPDATE_INTO_S_S_VALUES_S_WHERE_S = "UPDATE INTO %s (%s) values (%s) WHERE %s = ?";

  private static final String COULD_NOT_UPDATE_ROW_ID_S_INTO_TABLE_S = "Could not update rowId: "
      + "%s into table: %s";

  private static final String COULD_NOT_INSERT_ROW_ID_S_INTO_TABLE_S = "Could not insert rowId: "
      + "%s into table: %s";
  private static final String COMMIT_THE_SQL_BATCH = "Commit the SQL batch";
  private static final String COMMIT_THE_REMAINING_SQL_BATCH = "Commit the remaining SQL batch";

  private static final int BATCH_SIZE = 100;

  Connection conn;
  String dbUrl;
  String dbUsername;
  String dbPassword;
  String defaultTZ;
  Logger logger;

  public CCISSyncer() {

  }

  public CCISSyncer(Connection connection) {
    this.conn = connection;
  }

  public CCISSyncer(String dbUrl, String dbUsername, String dbPassword, String defaultTZ,
      Logger logger)
      throws ClassNotFoundException, SQLException {
    this.dbUrl = dbUrl;
    this.dbUsername = dbUsername;
    this.dbPassword = dbPassword;
    this.defaultTZ = defaultTZ;
    this.logger = logger;

    Class.forName(POSTGRESQL_DRIVER);

    conn = DriverManager.getConnection(dbUrl, dbUsername,
        dbPassword);
  }

  public int deleteAllRowsInColdChainTable(String tableId) throws SQLException {
    Statement delStmt = conn.createStatement();
    int delCnt = delStmt.executeUpdate(String.format(DELETE_FROM_S, tableId));
    return delCnt;
  }

  public void insertSingleCCDBRow(String destTable, CCDBRow ccdbRow, Map<String,
      DatabaseColumn> destTableColMap,
      String rowId, String rowETag, Logger logger)
      throws SQLException, ParseException {
    PreparedStatement selectSqlCmd = conn.prepareStatement(
        "SELECT " + destTableColMap.get(SyncClient.ROW_ETAG_ROW_DEF).columnName + " FROM "
            + destTable + " WHERE " + destTableColMap.get(SyncClient.ID_ROW_DEF).columnName
            + " = ?");

    selectSqlCmd.setString(1, rowId);

    ResultSet rs = selectSqlCmd.executeQuery();
    if (rs.next()) {
      // If change occurred, update the row
      String currRowETag = rs
          .getString(destTableColMap.get(SyncClient.ROW_ETAG_ROW_DEF).columnName);

      if (!Objects.equals(currRowETag, rowETag)) {
        // UPDATE into db
        PreparedStatement updateSqlCmd = conn.prepareStatement(String
            .format(UPDATE_INTO_S_S_VALUES_S_WHERE_S, destTable, ccdbRow.cols,
                ccdbRow.vals, destTableColMap.get(SyncClient.ID_ROW_DEF).columnName));

        int idx = addValuesToSqlCommand(destTableColMap, ccdbRow.colToKeyValueMap, updateSqlCmd);

        // Account for rowId in WHERE clause
        updateSqlCmd.setString(idx, rowId);

        if (updateSqlCmd.executeUpdate() != 1) {
          logger.warn(String.format(COULD_NOT_UPDATE_ROW_ID_S_INTO_TABLE_S, rowId, destTable));
        }
      }
    } else {
      // INSERT into db
      PreparedStatement insertSqlCmd = conn.prepareStatement(String
          .format(INSERT_INTO_S_S_VALUES_S, destTable, ccdbRow.cols, ccdbRow.vals));

      addValuesToSqlCommand(destTableColMap, ccdbRow.colToKeyValueMap, insertSqlCmd);

      if (insertSqlCmd.executeUpdate() != 1) {
        logger.warn(String.format(COULD_NOT_INSERT_ROW_ID_S_INTO_TABLE_S, rowId, destTable));
      }
    }
  }

  public void insertBatchCCDBRows(String destTable, List<CCDBRow> ccdbRowList,
      Map<String, DatabaseColumn> destTableColMap, Logger logger) throws SQLException,
      ParseException {
    int cnt = 0;

    PreparedStatement batchSqlCmd = conn.prepareStatement(String
        .format(INSERT_INTO_S_S_VALUES_S, destTable, ccdbRowList.get(0).cols,
            ccdbRowList.get(0).vals));

    for (CCDBRow ccdbRow : ccdbRowList) {
      addValuesToSqlCommand(destTableColMap, ccdbRow.colToKeyValueMap, batchSqlCmd);
      batchSqlCmd.addBatch();
      cnt++;

      if (cnt % BATCH_SIZE == 0) {
        logger.info(COMMIT_THE_SQL_BATCH);
        int[] res = batchSqlCmd.executeBatch();
      }
    }

    if (cnt % BATCH_SIZE != 0) {
      logger.info(COMMIT_THE_REMAINING_SQL_BATCH);
      int[] res = batchSqlCmd.executeBatch();
    }
  }

  private int addValuesToSqlCommand(Map<String, DatabaseColumn> destTableColMap,
      Map<String, String> colToKeyValueMap, PreparedStatement updateSqlCmd)
      throws SQLException, ParseException {
    int idx = 1;
    for (Map.Entry<String, DatabaseColumn> entry : destTableColMap.entrySet()) {
      DatabaseColumn dbCol = entry.getValue();

      if (dbCol.colType == null || dbCol.colType.length() == 0 || Objects
          .equals(dbCol.colType.toUpperCase(), VARCHAR)) {
        if (colToKeyValueMap.get(dbCol.columnName) != null) {
          updateSqlCmd.setString(idx, colToKeyValueMap.get(dbCol.columnName));
        } else {
          updateSqlCmd.setNull(idx, Types.VARCHAR);
        }
      } else if (Objects.equals(dbCol.colType.toUpperCase(), INT)) {
        if (colToKeyValueMap.get(dbCol.columnName) != null) {
          updateSqlCmd.setInt(idx, Integer.parseInt(colToKeyValueMap.get(dbCol.columnName)));
        } else {
          updateSqlCmd.setNull(idx, Types.INTEGER);
        }
      } else if (Objects.equals(dbCol.colType.toUpperCase(), DOUBLE)) {
        if (colToKeyValueMap.get(dbCol.columnName) != null) {
          updateSqlCmd
              .setDouble(idx, Double.parseDouble(colToKeyValueMap.get(dbCol.columnName)));
        } else {
          updateSqlCmd.setNull(idx, Types.DOUBLE);
        }
      } else if (Objects.equals(dbCol.colType.toUpperCase(), DATETIME_CAP)) {
        if (colToKeyValueMap.get(dbCol.columnName) != null) {
          updateSqlCmd.setTimestamp(idx, new Timestamp(
              TableConstants.milliSecondsFromNanos(colToKeyValueMap.get(dbCol.columnName))));
        } else {
          updateSqlCmd.setNull(idx, Types.TIMESTAMP);
        }
      } else if (Objects.equals(dbCol.colType.toUpperCase(), TIME)) {
        if (colToKeyValueMap.get(dbCol.columnName) != null) {
          String truncated = colToKeyValueMap.get(dbCol.columnName).substring(0,
              colToKeyValueMap.get(dbCol.columnName).length()
                  - MILLI_TO_NANO_TIMESTAMP_EXTENSION.length());
          CustomDate ad = new CustomDate("HH:mm:ss.SSS", truncated,
              TimeZone.getTimeZone(this.defaultTZ));
          updateSqlCmd.setTime(idx, new Time(ad.getMSTime()));
        } else {
          updateSqlCmd.setNull(idx, Types.TIME);
        }
      }
      idx++;
    }
    return idx;
  }

}
