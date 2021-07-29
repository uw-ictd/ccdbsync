package edu.uw.ictd.ccis;

import java.util.Map;

public class CCDBRow {
  String cols;
  String vals;
  Map<String, String> colToKeyValueMap;

  public CCDBRow(String cols, String vals, Map<String, String> colToKeyValueMap) {
    this.cols = cols;
    this.vals = vals;
    this.colToKeyValueMap = colToKeyValueMap;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    CCDBRow ccdbRow = (CCDBRow) o;

    if (cols != null ? !cols.equals(ccdbRow.cols) : ccdbRow.cols != null)
      return false;
    if (vals != null ? !vals.equals(ccdbRow.vals) : ccdbRow.vals != null)
      return false;
    return colToKeyValueMap != null ?
        colToKeyValueMap.equals(ccdbRow.colToKeyValueMap) :
        ccdbRow.colToKeyValueMap == null;
  }

  @Override
  public int hashCode() {
    int result = cols != null ? cols.hashCode() : 0;
    result = 31 * result + (vals != null ? vals.hashCode() : 0);
    result = 31 * result + (colToKeyValueMap != null ? colToKeyValueMap.hashCode() : 0);
    return result;
  }
}
