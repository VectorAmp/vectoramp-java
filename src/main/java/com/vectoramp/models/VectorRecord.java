package com.vectoramp.models;

import java.util.List;
import java.util.Map;

public class VectorRecord {
    private final String id;
    private final List<Double> values;
    private final Map<String, Object> metadata;

    public VectorRecord(String id, List<Double> values, Map<String, Object> metadata) {
        this.id = id;
        this.values = values;
        this.metadata = metadata;
    }

    public static VectorRecord of(String id, List<Double> values, Map<String, Object> metadata) {
        return new VectorRecord(id, values, metadata);
    }

    public String getId() { return id; }
    public List<Double> getValues() { return values; }
    public Map<String, Object> getMetadata() { return metadata; }
}
