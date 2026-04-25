package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SseEvent {
    private String chunkType;
    private String content;
    private Map<String, Object> metadata;

    public String getChunkType() { return chunkType; }
    public String getContent() { return content; }
    public Map<String, Object> getMetadata() { return metadata; }
}
