package com.vectoramp.models;

import java.util.LinkedHashMap;
import java.util.Map;

public final class S3Source implements IngestionSourceInput {
    private final String name;
    private final String description;
    private final Map<String, Object> config;
    private final Map<String, Object> metadata;

    private S3Source(Builder builder) {
        this.name = GenericSource.requireText(builder.name, "name");
        this.description = builder.description;
        this.config = Map.copyOf(builder.config);
        this.metadata = Map.copyOf(builder.metadata);
    }

    public static Builder builder(String name, String bucket) { return new Builder(name, bucket); }
    public static S3Source of(String name, String bucket, String prefix) { return builder(name, bucket).prefix(prefix).build(); }

    public String getSourceType() { return SourceType.S3; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Map<String, Object> getConfig() { return config; }
    public Map<String, Object> getMetadata() { return metadata; }

    @Override public CreateSourceRequest toCreateSourceRequest() {
        return new CreateSourceRequest(SourceType.S3, name, description, config, metadata);
    }

    public static final class Builder {
        private final String name;
        private String description;
        private final Map<String, Object> config = new LinkedHashMap<>();
        private final Map<String, Object> metadata = new LinkedHashMap<>();

        private Builder(String name, String bucket) {
            this.name = name;
            GenericSource.put(config, "bucket", bucket);
        }
        public Builder prefix(String prefix) { GenericSource.put(config, "prefix", prefix); return this; }
        public Builder region(String region) { GenericSource.put(config, "region", region); return this; }
        public Builder roleArn(String roleArn) { GenericSource.put(config, "role_arn", roleArn); return this; }
        public Builder syncMode(String syncMode) { GenericSource.put(config, "sync_mode", syncMode); return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder config(String key, Object value) { GenericSource.put(config, key, value); return this; }
        public Builder metadata(String key, Object value) { GenericSource.put(metadata, key, value); return this; }
        public Builder metadata(Map<String, Object> values) { GenericSource.putAll(metadata, values); return this; }
        public S3Source build() { return new S3Source(this); }
    }
}
