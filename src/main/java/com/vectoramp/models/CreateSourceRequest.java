package com.vectoramp.models;

import java.util.Map;

/** Raw create-source request. Prefer typed source inputs unless using a custom source type. */
public class CreateSourceRequest implements IngestionSourceInput {
    private final String sourceType;
    private final String name;
    private final String description;
    private final Map<String, Object> config;
    private final Map<String, Object> metadata;

    /**
     * Creates a raw source request.
     *
     * @param sourceType API source type, for example {@code web}, {@code s3}, {@code gdrive}, or {@code file_upload}
     * @param name source name
     * @param description optional source description
     * @param config source config map
     * @param metadata optional metadata map
     */
    public CreateSourceRequest(String sourceType, String name, String description, Map<String, Object> config, Map<String, Object> metadata) {
        this.sourceType = sourceType;
        this.name = name;
        this.description = description;
        this.config = config;
        this.metadata = metadata;
    }

    /**
     * Creates a file-upload source request using the default generated name.
     * @param datasetId target dataset ID
     * @return create-source request
     */
    public static CreateSourceRequest fileUpload(String datasetId) {
        return FileUploadSource.of(datasetId).toCreateSourceRequest();
    }

    /**
     * Creates a named file-upload source request.
     * @param name source name
     * @param datasetId target dataset ID
     * @return create-source request
     */
    public static CreateSourceRequest fileUpload(String name, String datasetId) {
        return FileUploadSource.of(name, datasetId).toCreateSourceRequest();
    }

    @Override public CreateSourceRequest toCreateSourceRequest() { return this; }

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
}
