package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonValue;

/** Canonical typed metadata field types accepted by the dataset schema API. */
public enum MetadataFieldType {
    STRING("string"), U32("u32"), I32("i32"), I64("i64"), F32("f32"), F64("f64");

    private final String value;
    MetadataFieldType(String value) { this.value = value; }
    @JsonValue public String value() { return value; }
}
