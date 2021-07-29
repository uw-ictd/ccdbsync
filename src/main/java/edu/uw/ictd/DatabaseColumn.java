package edu.uw.ictd;

import java.util.Objects;

public class DatabaseColumn {

  public String columnName;
  public String colType;

  public DatabaseColumn(String name, String type) {
    this.columnName = name;
    this.colType = type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    DatabaseColumn that = (DatabaseColumn) o;

    if (!Objects.equals(columnName, that.columnName))
      return false;
    return Objects.equals(colType, that.colType);
  }

  @Override
  public int hashCode() {
    int result = columnName != null ? columnName.hashCode() : 0;
    result = 31 * result + (colType != null ? colType.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "DatabaseColumn{" + "columnName='" + columnName + '\'' + ", colType='" + colType + '\''
        + '}';
  }
}
