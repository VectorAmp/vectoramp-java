package com.vectoramp.models;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Escape hatch for supported or newly-added ingestion source types. */
public final class GenericSource implements IngestionSourceInput {
    private final String sourceType;
    private final String name;
    private final String description;
    private final Map<String, Object> config;
    private final Map<String, Object> metadata;

    private GenericSource(Builder builder) {
        this.sourceType = requireText(builder.sourceType, "sourceType");
        this.name = requireText(builder.name, "name");
        this.description = builder.description;
        this.config = Map.copyOf(builder.config);
        this.metadata = Map.copyOf(builder.metadata);
    }

    /**
     * Starts a generic source builder.
     * @param sourceType API source type
     * @param name source name
     * @return source builder
     */
    public static Builder builder(String sourceType, String name) {
        return new Builder(sourceType, name);
    }

    /**
     * Starts a generic source builder using the default name {@code <source-type>-source}.
     * @param sourceType API source type
     * @return source builder
     */
    public static Builder builder(String sourceType) {
        return builder(sourceType, defaultName(sourceType));
    }

    /**
     * @return sourceType
     */
    public String getSourceType() { return sourceType; }
    /**
     * @return name
     */
    public String getName() { return name; }
    /**
     * @return description
     */
    public String getDescription() { return description; }
    /**
     * @return config
     */
    public Map<String, Object> getConfig() { return config; }
    /**
     * @return metadata
     */
    public Map<String, Object> getMetadata() { return metadata; }

    @Override public CreateSourceRequest toCreateSourceRequest() {
        return new CreateSourceRequest(sourceType, name, description, config, metadata);
    }

    /** Builder for GenericSource inputs. */
    public static final class Builder {
        private final String sourceType;
        private final String name;
        private String description;
        private final Map<String, Object> config = new LinkedHashMap<>();
        private final Map<String, Object> metadata = new LinkedHashMap<>();

        private Builder(String sourceType, String name) {
            this.sourceType = sourceType;
            this.name = name;
        }

        /**
     * Sets optional source description.
     * @param description description text
     * @return this builder
     */
        public Builder description(String description) { this.description = description; return this; }
        /**
     * Adds an optional config value; null values are omitted.
     * @param key config key
     * @param value config value
     * @return this builder
     */
        public Builder config(String key, Object value) { put(config, key, value); return this; }
        /**
     * Adds optional config values; null values are omitted.
     * @param values config values
     * @return this builder
     */
        public Builder config(Map<String, Object> values) { putAll(config, values); return this; }
        /**
     * References a stored OAuth connection by id (sets config {@code connection_id}); null is omitted.
     * @param connectionId connection id from the connections API
     * @return this builder
     */
        public Builder connection(String connectionId) { put(config, "connection_id", connectionId); return this; }
        /**
     * Adds optional metadata; null values are omitted.
     * @param key metadata key
     * @param value metadata value
     * @return this builder
     */
        public Builder metadata(String key, Object value) { put(metadata, key, value); return this; }
        /**
     * Adds optional metadata values; null values are omitted.
     * @param values metadata values
     * @return this builder
     */
        public Builder metadata(Map<String, Object> values) { putAll(metadata, values); return this; }
        /**
     * @return immutable generic source input
     */
        public GenericSource build() { return new GenericSource(this); }
    }

    static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        return value;
    }

    static String defaultName(String sourceType) {
        String normalized = requireText(sourceType, "sourceType").replace('_', '-');
        return normalized + "-source";
    }

    static String defaultName(String sourceType, String identifier) {
        String fallback = defaultName(sourceType);
        if (identifier == null || identifier.isBlank()) return fallback;
        String slug = identifier.trim()
                .replaceFirst("^[a-zA-Z][a-zA-Z0-9+.-]*://", "")
                .replaceAll("[^A-Za-z0-9._-]+", "-")
                .replaceAll("^-+|-+$", "");
        if (slug.length() > 48) slug = slug.substring(0, 48).replaceAll("[-._]+$", "");
        return slug.isBlank() ? fallback : slug;
    }

    static void put(Map<String, Object> map, String key, Object value) {
        requireText(key, "key");
        if (value != null) map.put(key, value);
    }

    static void putAll(Map<String, Object> map, Map<String, Object> values) {
        if (values != null) values.forEach((key, value) -> put(map, key, value));
    }
}
