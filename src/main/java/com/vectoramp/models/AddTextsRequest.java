package com.vectoramp.models;

import java.util.List;
import java.util.Map;

public class AddTextsRequest {
    private final List<String> texts;
    private final List<String> ids;
    private final List<Map<String, Object>> metadata;

    public AddTextsRequest(List<String> texts, List<String> ids, List<Map<String, Object>> metadata) {
        this.texts = texts;
        this.ids = ids;
        this.metadata = metadata;
    }

    public static AddTextsRequest of(List<String> texts) {
        return new AddTextsRequest(texts, null, null);
    }

    public static AddTextsRequest of(List<String> texts, List<String> ids, List<Map<String, Object>> metadata) {
        return new AddTextsRequest(texts, ids, metadata);
    }

    public List<String> getTexts() { return texts; }
    public List<String> getIds() { return ids; }
    public List<Map<String, Object>> getMetadata() { return metadata; }
}
