package edu.uw.ictd.odk;

import edu.uw.ictd.RowConverter;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.opendatakit.aggregate.odktables.rest.entity.RowResourceList;
import org.opendatakit.sync.client.SyncClient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncEndpointDownloader {
  public static final String TABLES = "tables";

  SyncClient sc;
  String url;
  String user;
  String password;
  String appId;

  public SyncEndpointDownloader(String aggUrl, String aggUsername, String aggPassword, String appId)
      throws MalformedURLException {
    this.url = aggUrl;
    this.user = aggUsername;
    this.password = aggPassword;
    this.appId = appId;

    URL url = new URL(aggUrl);
    sc = new SyncClient();
    sc.init(url.getHost(), aggUsername, aggPassword);
  }

  public String getUrl() {
    return url;
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }

  public String getAppId() {
    return appId;
  }

  public List<String> getTableIds() throws IOException, JSONException {
    List<String> tableList = new ArrayList<>();

    JSONObject tablesJSONObj = sc.getTables(url, appId);
    JSONArray tablesJSONArray = tablesJSONObj.getJSONArray(TABLES);

    for (int i = 0; i < tablesJSONArray.size(); i++) {
      JSONObject tableJSONObj = tablesJSONArray.getJSONObject(i);
      String tableId = tableJSONObj.getString(SyncClient.TABLE_ID_JSON);
      tableList.add(tableId);
    }

    return tableList;
  }

  public Map<String, String> getTablesAndSchemaETags() throws IOException, JSONException {
    Map<String, String> tableToSchemaETagMap = new HashMap<>();

    JSONObject tablesJSONObj = sc.getTables(url, appId);
    JSONArray tablesJSONArray = tablesJSONObj.getJSONArray(TABLES);

    for (int i = 0; i < tablesJSONArray.size(); i++) {
      JSONObject tableJSONObj = tablesJSONArray.getJSONObject(i);
      String tableId = tableJSONObj.getString(SyncClient.TABLE_ID_JSON);
      String schemaETag = tableJSONObj.getString(SyncClient.SCHEMA_ETAG_JSON);
      tableToSchemaETagMap.put(tableId, schemaETag);
    }

    return tableToSchemaETagMap;
  }

  public RowResourceList getRows(String tableId, String schemaETag, String cursor,
      String fetchLimit) throws IOException {
    RowResourceList rowResList = null;

    JSONObject tableRows = sc.getRows(url, appId, tableId, schemaETag, cursor, fetchLimit);
    rowResList = RowConverter.convertJSONRowArrayToODKResourceList(tableRows);

    return rowResList;
  }

}
