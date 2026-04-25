package com.vectoramp.models;

import java.util.Map;

public class CreateSourceRequest {
    private final String sourceType;
    private final String name;
    private final String description;
    private final Map<String, Object> config;
    private final Map<String, Object> metadata;

    public CreateSourceRequest(String sourceType, String name, String description, Map<String, Object> config, Map<String, Object> metadata) {
        this.sourceType = sourceType;
        this.name = name;
        this.description = description;
        this.config = config;
        this.metadata = metadata;
    }

    public static CreateSourceRequest fileUpload(String name, String datasetId) {
        return new CreateSourceRequest("file_upload", name, "Direct file upload", Map.of("storage_provider", "s3", "sync_mode", "full"), Map.of("dataset_id", datasetId));
    }

    public String getSourceType() { return sourceType; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Map<String, Object> getConfig() { return config; }
    public Map<String, Object> getMetadata() { return metadata; }
}
