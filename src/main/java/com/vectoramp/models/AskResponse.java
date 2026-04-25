package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

/** Non-streaming intelligence query response. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AskResponse {
    private String answer;
    private List<Map<String, Object>> sources;
    private List<Map<String, Object>> chunks;
    private String message;
    private Map<String, Object> metadata;

    /**
     * @return answer
     */
    public String getAnswer() { return answer; }
    /**
     * @return sources
     */
    public List<Map<String, Object>> getSources() { return sources; }
    /**
     * @return chunks
     */
    public List<Map<String, Object>> getChunks() { return chunks; }
    /**
     * @return message
     */
    public String getMessage() { return message; }
    /**
     * @return metadata
     */
    public Map<String, Object> getMetadata() { return metadata; }
}
