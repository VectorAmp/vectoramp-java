package com.vectoramp.models;

import java.util.Map;

public class CreateSourceRequest implements IngestionSourceInput {
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
        return FileUploadSource.of(name, datasetId).toCreateSourceRequest();
    }

    @Override public CreateSourceRequest toCreateSourceRequest() { return this; }

    public String getSourceType() { return sourceType; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Map<String, Object> getConfig() { return config; }
    public Map<String, Object> getMetadata() { return metadata; }
}
