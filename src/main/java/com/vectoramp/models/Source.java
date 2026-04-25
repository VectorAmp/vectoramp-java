package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

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

    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public JsonNode getConfig() { return config; }
    public List<String> getWarnings() { return warnings; }
    public String getValidationMessage() { return validationMessage; }
    public List<Object> getSamples() { return samples; }
    public Map<String, Object> getMetadata() { return metadata; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
