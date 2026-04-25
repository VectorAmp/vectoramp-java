package com.vectoramp.models;

import java.util.LinkedHashMap;
import java.util.Map;

/** S3 ingestion source input. Optional builder fields omitted from JSON use API defaults. */
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

    /**
     * Starts an S3 source builder named from the bucket.
     * @param bucket S3 bucket
     * @return source builder
     */
    public static Builder builder(String bucket) { return builder(GenericSource.defaultName(SourceType.S3, bucket), bucket); }
    /**
     * Starts a named S3 source builder.
     * @param name source name
     * @param bucket S3 bucket
     * @return source builder
     */
    public static Builder builder(String name, String bucket) { return new Builder(name, bucket); }
    /**
     * Creates an S3 source named from the bucket.
     * @param bucket S3 bucket
     * @return source input
     */
    public static S3Source of(String bucket) { return builder(bucket).build(); }
    /**
     * Creates a named S3 source with an optional prefix.
     * @param name source name
     * @param bucket S3 bucket
     * @param prefix optional key prefix
     * @return source input
     */
    public static S3Source of(String name, String bucket, String prefix) { return builder(name, bucket).prefix(prefix).build(); }

    /**
     * @return SourceType.S3
     */
    public String getSourceType() { return SourceType.S3; }
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
        return new CreateSourceRequest(SourceType.S3, name, description, config, metadata);
    }

    /** Builder for S3Source inputs. */
    public static final class Builder {
        private final String name;
        private String description;
        private final Map<String, Object> config = new LinkedHashMap<>();
        private final Map<String, Object> metadata = new LinkedHashMap<>();

        private Builder(String name, String bucket) {
            this.name = name;
            GenericSource.put(config, "bucket", bucket);
        }
        /**
     * Sets optional S3 key prefix; null is omitted.
     * @param prefix S3 key prefix
     * @return this builder
     */
        public Builder prefix(String prefix) { GenericSource.put(config, "prefix", prefix); return this; }
        /**
     * Sets optional S3 region; null is omitted.
     * @param region AWS region
     * @return this builder
     */
        public Builder region(String region) { GenericSource.put(config, "region", region); return this; }
        /**
     * Sets optional IAM role ARN; null is omitted.
     * @param roleArn IAM role ARN
     * @return this builder
     */
        public Builder roleArn(String roleArn) { GenericSource.put(config, "role_arn", roleArn); return this; }
        /**
     * Sets optional sync mode; API default applies when omitted.
     * @param syncMode sync mode
     * @return this builder
     */
        public Builder syncMode(String syncMode) { GenericSource.put(config, "sync_mode", syncMode); return this; }
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
        public Builder config(String key, Object value) { GenericSource.put(config, key, value); return this; }
        /**
     * Adds optional metadata; null values are omitted.
     * @param key metadata key
     * @param value metadata value
     * @return this builder
     */
        public Builder metadata(String key, Object value) { GenericSource.put(metadata, key, value); return this; }
        /**
     * Adds optional metadata values; null values are omitted.
     * @param values metadata values
     * @return this builder
     */
        public Builder metadata(Map<String, Object> values) { GenericSource.putAll(metadata, values); return this; }
        /**
     * @return immutable source input
     */
        public S3Source build() { return new S3Source(this); }
    }
}
