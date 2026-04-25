package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;

/** Search response containing ranked result records. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResponse {
    private List<Result> results;
    private String datasetId;
    private Double queryTimeMs;

    /**
     * @return results
     */
    public List<Result> getResults() { return results; }
    /**
     * @return datasetId
     */
    public String getDatasetId() { return datasetId; }
    /**
     * @return queryTimeMs
     */
    public Double getQueryTimeMs() { return queryTimeMs; }

    /** One ranked search result. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private JsonNode id;
        private Double score;
        private Map<String, Object> metadata;
        private List<Double> embedding;
        private String docKind;
        private String docValue;

        /**
         * @return id
         */
        public JsonNode getId() { return id; }
        /**
         * @return score
         */
        public Double getScore() { return score; }
        /**
         * @return metadata
         */
        public Map<String, Object> getMetadata() { return metadata; }
        /**
         * @return embedding
         */
        public List<Double> getEmbedding() { return embedding; }
        /**
         * @return docKind
         */
        public String getDocKind() { return docKind; }
        /**
         * @return docValue
         */
        public String getDocValue() { return docValue; }
    }
}
