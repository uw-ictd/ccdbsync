package edu.uw.ictd;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uw.ictd.ccis.CCDBRow;
import org.apache.wink.json4j.JSONObject;
import org.opendatakit.aggregate.odktables.rest.entity.DataKeyValue;
import org.opendatakit.aggregate.odktables.rest.entity.RowResource;
import org.opendatakit.aggregate.odktables.rest.entity.RowResourceList;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.uw.ictd.constants.RowResourceMetadataConstants.mapMetadataToRowMethod;

public class RowConverter {

  public static CCDBRow convertODKRowToCCDBRow(String destTable, RowResource row,
      Map<String, DatabaseColumn> destTableColMap)
      throws SQLException, ParseException {

    if (destTable == null) {
      return null;
    }

    if (destTableColMap == null) {
      return null;
    }

    Map<String, String> colToKeyValueMap = new HashMap<>();
    StringBuilder colsBld = new StringBuilder();
    StringBuilder valsQBld = new StringBuilder();

    List<DataKeyValue> dataKeyValueList = row.getValues();

    for (DataKeyValue dataKeyValue : dataKeyValueList) {
      if (destTableColMap.containsKey(dataKeyValue.column)) {
        colToKeyValueMap
            .put(destTableColMap.get(dataKeyValue.column).columnName, dataKeyValue.value);
      }
    }

    int destTableColMapSize = destTableColMap.size();
    int i = 0;

    // Add in the data fields
    for (String srcCol : destTableColMap.keySet()) {

      if (!colToKeyValueMap.containsKey(destTableColMap.get(srcCol).columnName)) {
        if (mapMetadataToRowMethod.containsKey(srcCol)) {
          colToKeyValueMap.put(destTableColMap.get(srcCol).columnName,
              mapMetadataToRowMethod.get(srcCol).runCommand(row));
        }
      }

      DatabaseColumn dbCol = destTableColMap.get(srcCol);
      colsBld.append(dbCol.columnName);
      valsQBld.append("?");

      if (i < destTableColMapSize - 1) {
        colsBld.append(", ");
        valsQBld.append(", ");
      }

      i++;
    }

    return new CCDBRow(colsBld.toString(), valsQBld.toString(), colToKeyValueMap);
  }

  public static RowResource convertJSONRowToODKRow(JSONObject currRowJson) throws IOException {
    return new ObjectMapper().readerFor(RowResource.class).readValue(currRowJson.toString());
  }

  public static RowResourceList convertJSONRowArrayToODKResourceList(JSONObject rowResourceListJson)
      throws IOException {
    return new ObjectMapper().readerFor(RowResourceList.class)
        .readValue(rowResourceListJson.toString());
  }
}
