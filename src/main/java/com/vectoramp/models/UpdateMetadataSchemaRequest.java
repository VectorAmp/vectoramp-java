package com.vectoramp.models;

import java.util.List;
import java.util.Objects;

/** Request body for merging or replacing a dataset metadata schema. */
public final class UpdateMetadataSchemaRequest {
    private final List<MetadataSchemaField> schema;
    private final String mode;

    private UpdateMetadataSchemaRequest(List<MetadataSchemaField> schema, String mode) {
        this.schema = List.copyOf(Objects.requireNonNull(schema, "schema"));
        this.mode = mode;
    }
    public static UpdateMetadataSchemaRequest merge(List<MetadataSchemaField> schema) {
        return new UpdateMetadataSchemaRequest(schema, "merge");
    }
    public static UpdateMetadataSchemaRequest replace(List<MetadataSchemaField> schema) {
        return new UpdateMetadataSchemaRequest(schema, "replace");
    }
    public List<MetadataSchemaField> getSchema() { return schema; }
    public String getMode() { return mode; }
}
