package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/** Request to create an intelligence session. All fields are optional. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionCreateRequest {
    private String title;
    private String workspaceId;
    private String datasetId;
    private Map<String, Object> metadata;

    /** Creates an empty session request. */
    public SessionCreateRequest() {}

    /**
     * Creates a session request with a title.
     * @param title session title
     * @return session request
     */
    public static SessionCreateRequest of(String title) {
        return new SessionCreateRequest().title(title);
    }

    /**
     * Sets the session title.
     * @param title session title
     * @return this request
     */
    public SessionCreateRequest title(String title) { this.title = title; return this; }
    /**
     * Sets the workspace id.
     * @param workspaceId workspace id
     * @return this request
     */
    public SessionCreateRequest workspaceId(String workspaceId) { this.workspaceId = workspaceId; return this; }
    /**
     * Scopes the session to a dataset.
     * @param datasetId dataset id
     * @return this request
     */
    public SessionCreateRequest datasetId(String datasetId) { this.datasetId = datasetId; return this; }
    /**
     * Sets optional session metadata.
     * @param metadata metadata map
     * @return this request
     */
    public SessionCreateRequest metadata(Map<String, Object> metadata) { this.metadata = metadata; return this; }

    /** @return session title */
    public String getTitle() { return title; }
    /** @return workspace id */
    public String getWorkspaceId() { return workspaceId; }
    /** @return dataset id */
    public String getDatasetId() { return datasetId; }
    /** @return metadata */
    public Map<String, Object> getMetadata() { return metadata; }
}
