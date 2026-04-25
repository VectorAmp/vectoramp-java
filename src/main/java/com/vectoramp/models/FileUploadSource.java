package com.vectoramp.models;

import java.util.LinkedHashMap;
import java.util.Map;

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

    public static Builder builder(String name, String datasetId) { return new Builder(name, datasetId); }
    public static FileUploadSource of(String name, String datasetId) { return builder(name, datasetId).build(); }

    public String getSourceType() { return SourceType.FILE_UPLOAD; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Map<String, Object> getConfig() { return config; }
    public Map<String, Object> getMetadata() { return metadata; }

    @Override public CreateSourceRequest toCreateSourceRequest() {
        return new CreateSourceRequest(SourceType.FILE_UPLOAD, name, description, config, metadata);
    }

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
        public Builder storageProvider(String storageProvider) { GenericSource.put(config, "storage_provider", storageProvider); return this; }
        public Builder syncMode(String syncMode) { GenericSource.put(config, "sync_mode", syncMode); return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder config(String key, Object value) { GenericSource.put(config, key, value); return this; }
        public Builder metadata(String key, Object value) { GenericSource.put(metadata, key, value); return this; }
        public Builder metadata(Map<String, Object> values) { GenericSource.putAll(metadata, values); return this; }
        public FileUploadSource build() { return new FileUploadSource(this); }
    }
}
