package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

/** Server-sent event emitted by streaming intelligence queries. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SseEvent {
    private String chunkType;
    private String content;
    private Map<String, Object> metadata;

    /**
     * @return chunkType
     */
    public String getChunkType() { return chunkType; }
    /**
     * @return content
     */
    public String getContent() { return content; }
    /**
     * @return metadata
     */
    public Map<String, Object> getMetadata() { return metadata; }
}
