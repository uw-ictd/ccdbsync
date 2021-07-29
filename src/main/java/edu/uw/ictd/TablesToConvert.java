package edu.uw.ictd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({
    "source_table",
    "destination_table",
    "source_field",
    "target_field",
    "field_type"
})

public class TablesToConvert {
  @JsonProperty("source_table")
  public String sourceTable;

  @JsonProperty("destination_table")
  public String destinationTable;

  @JsonProperty("source_field")
  public String sourceField;

  @JsonProperty("target_field")
  public String targetField;

  @JsonProperty("field_type")
  public String fieldType;

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    TablesToConvert that = (TablesToConvert) o;

    if (sourceTable != null ? !sourceTable.equals(that.sourceTable) : that.sourceTable != null)
      return false;
    if (destinationTable != null ?
        !destinationTable.equals(that.destinationTable) :
        that.destinationTable != null)
      return false;
    if (sourceField != null ? !sourceField.equals(that.sourceField) : that.sourceField != null)
      return false;
    if (targetField != null ? !targetField.equals(that.targetField) : that.targetField != null)
      return false;
    return fieldType != null ? fieldType.equals(that.fieldType) : that.fieldType == null;
  }

  @Override
  public int hashCode() {
    int result = sourceTable != null ? sourceTable.hashCode() : 0;
    result = 31 * result + (destinationTable != null ? destinationTable.hashCode() : 0);
    result = 31 * result + (sourceField != null ? sourceField.hashCode() : 0);
    result = 31 * result + (targetField != null ? targetField.hashCode() : 0);
    result = 31 * result + (fieldType != null ? fieldType.hashCode() : 0);
    return result;
  }
}
