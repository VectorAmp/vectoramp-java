package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/** Ingestion source resource returned by the API. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Source {
    private String id;
    private String name;
    private String type;
    private String description;
    private JsonNode config;
    private List<String> warnings;
    private String validationMessage;
    private List<Object> samples;
    private Map<String, Object> metadata;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    /**
     * @return id
     */
    public String getId() { return id; }
    /**
     * @return name
     */
    public String getName() { return name; }
    /**
     * @return type
     */
    public String getType() { return type; }
    /**
     * @return description
     */
    public String getDescription() { return description; }
    /**
     * @return config
     */
    public JsonNode getConfig() { return config; }
    /**
     * @return warnings
     */
    public List<String> getWarnings() { return warnings; }
    /**
     * @return validationMessage
     */
    public String getValidationMessage() { return validationMessage; }
    /**
     * @return samples
     */
    public List<Object> getSamples() { return samples; }
    /**
     * @return metadata
     */
    public Map<String, Object> getMetadata() { return metadata; }
    /**
     * @return createdAt
     */
    public OffsetDateTime getCreatedAt() { return createdAt; }
    /**
     * @return updatedAt
     */
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
