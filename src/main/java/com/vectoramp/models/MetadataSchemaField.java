package com.vectoramp.models;

import java.util.Objects;

/** A named field in a dataset's typed metadata schema. */
public final class MetadataSchemaField {
    private final String name;
    private final MetadataFieldType type;

    public MetadataSchemaField(String name, MetadataFieldType type) {
        this.name = Objects.requireNonNull(name, "name");
        this.type = Objects.requireNonNull(type, "type");
    }
    public static MetadataSchemaField of(String name, MetadataFieldType type) {
        return new MetadataSchemaField(name, type);
    }
    public String getName() { return name; }
    public MetadataFieldType getType() { return type; }
}
