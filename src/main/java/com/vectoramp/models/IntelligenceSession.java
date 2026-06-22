package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;
import java.util.Map;

/** Intelligence workflow session returned by {@code /intelligence/sessions}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IntelligenceSession {
    private String id;
    @JsonAlias("organizationId") private String organizationId;
    @JsonAlias("userId") private String userId;
    @JsonAlias("workspaceId") private String workspaceId;
    @JsonAlias("datasetId") private String datasetId;
    private String title;
    private String status;
    private Map<String, Object> metadata;
    @JsonAlias("createdAt") private OffsetDateTime createdAt;
    @JsonAlias("updatedAt") private OffsetDateTime updatedAt;

    /** @return session id */
    public String getId() { return id; }
    /** @return organization id */
    public String getOrganizationId() { return organizationId; }
    /** @return user id */
    public String getUserId() { return userId; }
    /** @return workspace id, when set */
    public String getWorkspaceId() { return workspaceId; }
    /** @return dataset id scoped to this session, when set */
    public String getDatasetId() { return datasetId; }
    /** @return session title, when set */
    public String getTitle() { return title; }
    /** @return session status, for example active or archived */
    public String getStatus() { return status; }
    /** @return metadata blob attached to the session */
    public Map<String, Object> getMetadata() { return metadata; }
    /** @return created-at timestamp */
    public OffsetDateTime getCreatedAt() { return createdAt; }
    /** @return updated-at timestamp */
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
