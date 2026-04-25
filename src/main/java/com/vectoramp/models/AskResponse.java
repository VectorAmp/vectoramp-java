package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AskResponse {
    private String answer;
    private List<Map<String, Object>> sources;
    private List<Map<String, Object>> chunks;
    private String message;
    private Map<String, Object> metadata;

    public String getAnswer() { return answer; }
    public List<Map<String, Object>> getSources() { return sources; }
    public List<Map<String, Object>> getChunks() { return chunks; }
    public String getMessage() { return message; }
    public Map<String, Object> getMetadata() { return metadata; }
}
