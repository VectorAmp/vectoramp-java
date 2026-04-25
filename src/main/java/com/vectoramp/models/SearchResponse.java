package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResponse {
    private List<Result> results;
    private String datasetId;
    private Double queryTimeMs;

    public List<Result> getResults() { return results; }
    public String getDatasetId() { return datasetId; }
    public Double getQueryTimeMs() { return queryTimeMs; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private JsonNode id;
        private Double score;
        private Map<String, Object> metadata;
        private List<Double> embedding;
        private String docKind;
        private String docValue;

        public JsonNode getId() { return id; }
        public Double getScore() { return score; }
        public Map<String, Object> getMetadata() { return metadata; }
        public List<Double> getEmbedding() { return embedding; }
        public String getDocKind() { return docKind; }
        public String getDocValue() { return docValue; }
    }
}
