package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Intelligence query request. Optional fields omitted from JSON use API defaults.
 *
 * <p>The dataset scope is {@code datasetIds}, a list. Leaving it unset searches every dataset the
 * caller can see. The singular {@code dataset_id} field is retired: the API answers any request
 * carrying it with a 400 naming {@code dataset_ids} as the replacement, and the {@code "all"}
 * sentinel it carried is expressed by leaving the scope unset.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AskRequest {
    private String query;
    private List<String> datasetIds;
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
     * Adds one dataset to the query scope. Repeating this widens the scope rather than replacing it.
     *
     * @param datasetId dataset ID
     * @return this request
     */
    public AskRequest datasetId(String datasetId) {
        if (datasetIds == null) datasetIds = new ArrayList<>();
        datasetIds.add(datasetId);
        return this;
    }
    /**
     * Sets the whole query scope, replacing anything set before it.
     *
     * @param datasetIds dataset IDs
     * @return this request
     */
    public AskRequest datasetIds(List<String> datasetIds) {
        this.datasetIds = datasetIds == null ? null : new ArrayList<>(datasetIds);
        return this;
    }
    /**
     * Sets the whole query scope, replacing anything set before it.
     *
     * @param datasetIds dataset IDs
     * @return this request
     */
    public AskRequest datasetIds(String... datasetIds) {
        return datasetIds(datasetIds == null ? null : Arrays.asList(datasetIds));
    }
    /**
     * Clears the scope so the query reaches every accessible dataset.
     *
     * <p>That is what an absent {@code dataset_ids} means to the API; the old {@code "all"}
     * sentinel is retired.
     *
     * @return this request
     */
    public AskRequest allDatasets() { this.datasetIds = null; return this; }
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
     * The dataset scope as it goes on the wire.
     *
     * <p>Blank ids and the retired {@code "all"} sentinel are dropped, and an empty scope becomes
     * {@code null} so the field is omitted — sending {@code []} would be a narrower, different
     * request than "every dataset you can see".
     *
     * @return dataset IDs, or null when unscoped
     */
    public List<String> getDatasetIds() {
        if (datasetIds == null) return null;
        List<String> scope = new ArrayList<>();
        for (String id : datasetIds) {
            if (id == null) continue;
            String trimmed = id.trim();
            if (trimmed.isEmpty() || "all".equals(trimmed)) continue;
            scope.add(trimmed);
        }
        return scope.isEmpty() ? null : scope;
    }
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
