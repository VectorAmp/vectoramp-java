package com.vectoramp.models;

import java.util.LinkedHashMap;
import java.util.Map;

/** Direct file-upload ingestion source input. Optional builder fields omitted from JSON use API defaults. */
public final class FileUploadSource implements IngestionSourceInput {
    private final String name;
    private final String description;
    private final Map<String, Object> config;
    private final Map<String, Object> metadata;

    private FileUploadSource(Builder builder) {
        this.name = GenericSource.requireText(builder.name, "name");
        this.description = builder.description;
        this.config = Map.copyOf(builder.config);
        this.metadata = Map.copyOf(builder.metadata);
    }

    /**
     * Starts a named file-upload source builder.
     * @param name source name
     * @param datasetId target dataset ID stored in metadata
     * @return source builder
     */
    public static Builder builder(String name, String datasetId) { return new Builder(name, datasetId); }
    /**
     * Starts a file-upload source builder named from the dataset ID.
     * @param datasetId target dataset ID stored in metadata
     * @return source builder
     */
    public static Builder builderForDataset(String datasetId) { return builder(defaultName(datasetId), datasetId); }
    /**
     * Creates a named file-upload source.
     * @param name source name
     * @param datasetId target dataset ID stored in metadata
     * @return source input
     */
    public static FileUploadSource of(String name, String datasetId) { return builder(name, datasetId).build(); }
    /**
     * Creates a file-upload source named from the dataset ID.
     * @param datasetId target dataset ID stored in metadata
     * @return source input
     */
    public static FileUploadSource of(String datasetId) { return builderForDataset(datasetId).build(); }
    /**
     * Returns the default generated file-upload source name.
     * @param datasetId target dataset ID
     * @return generated source name
     */
    public static String defaultName(String datasetId) {
        return "file-upload-" + GenericSource.defaultName(SourceType.FILE_UPLOAD, datasetId);
    }

    /**
     * @return SourceType.FILE_UPLOAD
     */
    public String getSourceType() { return SourceType.FILE_UPLOAD; }
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
        return new CreateSourceRequest(SourceType.FILE_UPLOAD, name, description, config, metadata);
    }

    /** Builder for FileUploadSource inputs. */
    public static final class Builder {
        private final String name;
        private String description = "Direct file upload";
        private final Map<String, Object> config = new LinkedHashMap<>();
        private final Map<String, Object> metadata = new LinkedHashMap<>();

        private Builder(String name, String datasetId) {
            this.name = name;
            GenericSource.put(config, "storage_provider", "s3");
            GenericSource.put(config, "sync_mode", "full");
            GenericSource.put(metadata, "dataset_id", datasetId);
        }
        /**
     * Sets upload storage provider; default is s3.
     * @param storageProvider storage provider
     * @return this builder
     */
        public Builder storageProvider(String storageProvider) { GenericSource.put(config, "storage_provider", storageProvider); return this; }
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
        public FileUploadSource build() { return new FileUploadSource(this); }
    }
}
