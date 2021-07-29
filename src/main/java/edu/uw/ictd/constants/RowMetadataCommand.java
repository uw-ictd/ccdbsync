package edu.uw.ictd.constants;

import org.opendatakit.aggregate.odktables.rest.entity.RowResource;

public interface RowMetadataCommand {
  String runCommand(RowResource row);
}