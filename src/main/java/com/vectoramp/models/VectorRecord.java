package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * Vector record for direct insertion.
 *
 * <p>The vector {@code id} accepts a {@link String} or a numeric value. Numeric ids
 * (for example {@code int} or {@code long}) are serialized as JSON numbers, not strings,
 * so the API preserves them exactly instead of rewriting them.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VectorRecord {
    private final Object id;
    private final List<Double> values;
    private final Map<String, Object> metadata;

    /**
     * Creates a vector record with a string id.
     * @param id vector ID
     * @param values vector values
     * @param metadata optional metadata
     */
    public VectorRecord(String id, List<Double> values, Map<String, Object> metadata) {
        this((Object) id, values, metadata);
    }

    /**
     * Creates a vector record with a numeric id that serializes as a JSON number.
     * @param id numeric vector ID
     * @param values vector values
     * @param metadata optional metadata
     */
    public VectorRecord(long id, List<Double> values, Map<String, Object> metadata) {
        this((Object) id, values, metadata);
    }

    private VectorRecord(Object id, List<Double> values, Map<String, Object> metadata) {
        this.id = id;
        this.values = values;
        this.metadata = metadata;
    }

    /**
     * Creates a vector record with a string id.
     * @param id vector ID
     * @param values vector values
     * @param metadata optional metadata
     * @return vector record
     */
    public static VectorRecord of(String id, List<Double> values, Map<String, Object> metadata) {
        return new VectorRecord(id, values, metadata);
    }

    /**
     * Creates a vector record with a numeric id that serializes as a JSON number.
     * @param id numeric vector ID
     * @param values vector values
     * @param metadata optional metadata
     * @return vector record
     */
    public static VectorRecord of(long id, List<Double> values, Map<String, Object> metadata) {
        return new VectorRecord(id, values, metadata);
    }

    /**
     * Creates a vector record with an arbitrary id type.
     *
     * <p>Use this when an id is read from an external source whose type is not known at
     * compile time. {@code String} ids serialize as JSON strings; {@link Number} ids
     * serialize as JSON numbers.</p>
     *
     * @param id vector ID; a {@link String} or {@link Number}
     * @param values vector values
     * @param metadata optional metadata
     * @return vector record
     */
    public static VectorRecord ofId(Object id, List<Double> values, Map<String, Object> metadata) {
        return new VectorRecord(id, values, metadata);
    }

    /**
     * The vector id as stored. This is a {@link String} or a {@link Number} and serializes
     * with the same JSON type it was created with.
     * @return id
     */
    public Object getId() { return id; }

    /**
     * @return values
     */
    public List<Double> getValues() { return values; }
    /**
     * @return metadata
     */
    public Map<String, Object> getMetadata() { return metadata; }
}
