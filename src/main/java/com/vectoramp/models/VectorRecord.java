package com.vectoramp.models;

import java.util.List;
import java.util.Map;

/** Vector record for direct insertion. */
public class VectorRecord {
    private final String id;
    private final List<Double> values;
    private final Map<String, Object> metadata;

    /**
     * Creates a vector record.
     * @param id vector ID
     * @param values vector values
     * @param metadata optional metadata
     */
    public VectorRecord(String id, List<Double> values, Map<String, Object> metadata) {
        this.id = id;
        this.values = values;
        this.metadata = metadata;
    }

    /**
     * Creates a vector record.
     * @param id vector ID
     * @param values vector values
     * @param metadata optional metadata
     * @return vector record
     */
    public static VectorRecord of(String id, List<Double> values, Map<String, Object> metadata) {
        return new VectorRecord(id, values, metadata);
    }

    /**
     * @return id
     */
    public String getId() { return id; }
    /**
     * @return values
     */
    public List<Double> getValues() { return values; }
    /**
     * @return metadata
     */
    public Map<String, Object> getMetadata() { return metadata; }
}
