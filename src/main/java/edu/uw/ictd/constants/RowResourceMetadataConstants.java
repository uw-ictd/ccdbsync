package edu.uw.ictd.constants;

import org.opendatakit.sync.client.SyncClient;

import java.util.HashMap;
import java.util.Map;

public class RowResourceMetadataConstants {
  private static final String SELF_URI = "selfUri";

  public static Map<String, RowMetadataCommand> mapMetadataToRowMethod = new HashMap<>();

  static {
    mapMetadataToRowMethod.put(SyncClient.CREATE_USER_ROW_DEF, row -> row.getCreateUser());
    mapMetadataToRowMethod
        .put(SyncClient.DATA_ETAG_AT_MODIFICATION_ROW_DEF, row -> row.getDataETagAtModification());
    mapMetadataToRowMethod.put(SyncClient.FORM_ID_ROW_DEF, row -> row.getFormId());
    mapMetadataToRowMethod.put(SyncClient.ID_ROW_DEF, row -> row.getRowId());
    mapMetadataToRowMethod.put(SyncClient.LAST_UPDATE_USER_ROW_DEF, row -> row.getLastUpdateUser());
    mapMetadataToRowMethod.put(SyncClient.LOCALE_ROW_DEF, row -> row.getLocale());
    mapMetadataToRowMethod.put(SyncClient.ROW_ETAG_ROW_DEF, row -> row.getRowETag());
    mapMetadataToRowMethod.put(SyncClient.SAVEPOINT_CREATOR_ROW_DEF, row -> row.getSavepointCreator());
    mapMetadataToRowMethod.put(SyncClient.SAVEPOINT_TIMESTAMP_ROW_DEF, row -> row.getSavepointTimestamp());
    mapMetadataToRowMethod.put(SyncClient.SAVEPOINT_TYPE_ROW_DEF, row -> row.getSavepointType());
    mapMetadataToRowMethod.put(SyncClient.DELETED_ROW_DEF, row -> String.valueOf(row.isDeleted()));
    mapMetadataToRowMethod.put(SELF_URI, row -> row.getSelfUri());
  }
}
