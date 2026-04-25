package com.vectoramp.models;

import java.util.ArrayList;
import java.util.List;

/** Intelligence query request. Optional fields omitted from JSON use API defaults. */
public class AskRequest {
    private String query;
    private String datasetId;
    private Integer topK;
    private List<Message> conversationHistory;
    private Boolean stream;
    private Boolean includeSources;

    /** Creates an empty request for Jackson or manual population. */
    public AskRequest() {}

    /**
     * Creates a request for a question or prompt.
     *
     * @param query query text
     */
    public AskRequest(String query) { this.query = query; }

    /**
     * Creates a request for a question or prompt.
     * @param query query text
     * @return ask request
     */
    public static AskRequest of(String query) { return new AskRequest(query); }
    /**
     * Scopes the query to one dataset.
     * @param datasetId dataset ID
     * @return this request
     */
    public AskRequest datasetId(String datasetId) { this.datasetId = datasetId; return this; }
    /**
     * Scopes the query to all accessible datasets.
     * @return this request
     */
    public AskRequest allDatasets() { this.datasetId = "all"; return this; }
    /**
     * Sets maximum retrieval result count.
     * @param topK result count
     * @return this request
     */
    public AskRequest topK(int topK) { this.topK = topK; return this; }
    /**
     * Controls source/chunk context in the response; API default applies when omitted.
     * @param includeSources true to include sources
     * @return this request
     */
    public AskRequest includeSources(boolean includeSources) { this.includeSources = includeSources; return this; }
    /**
     * Adds a conversation-history message.
     * @param role message role such as user or assistant
     * @param content message content
     * @return this request
     */
    public AskRequest message(String role, String content) {
        if (conversationHistory == null) conversationHistory = new ArrayList<>();
        conversationHistory.add(new Message(role, content));
        return this;
    }
    /**
     * Sets streaming mode; client ask helpers set this automatically.
     * @param stream true for SSE streaming
     * @return this request
     */
    public AskRequest stream(boolean stream) { this.stream = stream; return this; }

    /**
     * @return query
     */
    public String getQuery() { return query; }
    /**
     * @return datasetId
     */
    public String getDatasetId() { return datasetId; }
    /**
     * @return topK
     */
    public Integer getTopK() { return topK; }
    /**
     * @return conversationHistory
     */
    public List<Message> getConversationHistory() { return conversationHistory; }
    /**
     * @return stream
     */
    public Boolean getStream() { return stream; }
    /**
     * @return includeSources
     */
    public Boolean getIncludeSources() { return includeSources; }

    /** Conversation-history message. */
    public static class Message {
        private final String role;
        private final String content;
        /**
         * Creates a conversation-history message.
         *
         * @param role message role such as user or assistant
         * @param content message content
         */
        public Message(String role, String content) { this.role = role; this.content = content; }
        /**
         * @return role
         */
        public String getRole() { return role; }
        /**
         * @return content
         */
        public String getContent() { return content; }
    }
}
