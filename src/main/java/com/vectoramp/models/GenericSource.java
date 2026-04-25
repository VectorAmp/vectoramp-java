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

    public static Builder builder(String sourceType, String name) {
        return new Builder(sourceType, name);
    }

    public String getSourceType() { return sourceType; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Map<String, Object> getConfig() { return config; }
    public Map<String, Object> getMetadata() { return metadata; }

    @Override public CreateSourceRequest toCreateSourceRequest() {
        return new CreateSourceRequest(sourceType, name, description, config, metadata);
    }

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

        public Builder description(String description) { this.description = description; return this; }
        public Builder config(String key, Object value) { put(config, key, value); return this; }
        public Builder config(Map<String, Object> values) { putAll(config, values); return this; }
        public Builder metadata(String key, Object value) { put(metadata, key, value); return this; }
        public Builder metadata(Map<String, Object> values) { putAll(metadata, values); return this; }
        public GenericSource build() { return new GenericSource(this); }
    }

    static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        return value;
    }

    static void put(Map<String, Object> map, String key, Object value) {
        requireText(key, "key");
        if (value != null) map.put(key, value);
    }

    static void putAll(Map<String, Object> map, Map<String, Object> values) {
        if (values != null) values.forEach((key, value) -> put(map, key, value));
    }
}
