package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/** Request to append a message to an intelligence session. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionMessageCreateRequest {
    private final String role;
    private final String content;
    private Map<String, Object> metadata;

    /**
     * Creates a message request.
     * @param role message role: user, assistant, system, or tool
     * @param content message content
     */
    public SessionMessageCreateRequest(String role, String content) {
        this.role = role;
        this.content = content;
    }

    /**
     * Creates a message request.
     * @param role message role: user, assistant, system, or tool
     * @param content message content
     * @return message request
     */
    public static SessionMessageCreateRequest of(String role, String content) {
        return new SessionMessageCreateRequest(role, content);
    }

    /**
     * Creates a {@code user}-role message request.
     * @param content message content
     * @return message request
     */
    public static SessionMessageCreateRequest user(String content) {
        return new SessionMessageCreateRequest("user", content);
    }

    /**
     * Sets optional message metadata.
     * @param metadata metadata map
     * @return this request
     */
    public SessionMessageCreateRequest metadata(Map<String, Object> metadata) { this.metadata = metadata; return this; }

    /** @return message role */
    public String getRole() { return role; }
    /** @return message content */
    public String getContent() { return content; }
    /** @return metadata */
    public Map<String, Object> getMetadata() { return metadata; }
}
