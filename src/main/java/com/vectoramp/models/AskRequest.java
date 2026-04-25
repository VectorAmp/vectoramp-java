package com.vectoramp.models;

import java.util.ArrayList;
import java.util.List;

public class AskRequest {
    private String query;
    private String datasetId;
    private Integer topK;
    private List<Message> conversationHistory;
    private Boolean stream;
    private Boolean includeSources;

    public AskRequest() {}

    public AskRequest(String query) { this.query = query; }

    public static AskRequest of(String query) { return new AskRequest(query); }
    public AskRequest datasetId(String datasetId) { this.datasetId = datasetId; return this; }
    public AskRequest allDatasets() { this.datasetId = "all"; return this; }
    public AskRequest topK(int topK) { this.topK = topK; return this; }
    public AskRequest includeSources(boolean includeSources) { this.includeSources = includeSources; return this; }
    public AskRequest message(String role, String content) {
        if (conversationHistory == null) conversationHistory = new ArrayList<>();
        conversationHistory.add(new Message(role, content));
        return this;
    }
    public AskRequest stream(boolean stream) { this.stream = stream; return this; }

    public String getQuery() { return query; }
    public String getDatasetId() { return datasetId; }
    public Integer getTopK() { return topK; }
    public List<Message> getConversationHistory() { return conversationHistory; }
    public Boolean getStream() { return stream; }
    public Boolean getIncludeSources() { return includeSources; }

    public static class Message {
        private final String role;
        private final String content;
        public Message(String role, String content) { this.role = role; this.content = content; }
        public String getRole() { return role; }
        public String getContent() { return content; }
    }
}
