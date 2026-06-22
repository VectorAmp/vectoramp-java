package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;
import java.util.Map;

/** A message within an intelligence session. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionMessage {
    private String id;
    @JsonAlias("sessionId") private String sessionId;
    private String role;
    private String content;
    private Map<String, Object> metadata;
    @JsonAlias("createdAt") private OffsetDateTime createdAt;

    /** @return message id */
    public String getId() { return id; }
    /** @return owning session id */
    public String getSessionId() { return sessionId; }
    /** @return message role: user, assistant, system, or tool */
    public String getRole() { return role; }
    /** @return message content */
    public String getContent() { return content; }
    /** @return metadata blob attached to the message */
    public Map<String, Object> getMetadata() { return metadata; }
    /** @return created-at timestamp */
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
