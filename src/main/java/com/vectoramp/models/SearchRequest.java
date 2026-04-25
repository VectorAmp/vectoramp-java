package com.vectoramp.models;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;

public class SearchRequest {
    private List<Double> query;
    private String queryText;
    private String embeddingModel;
    private String embeddingProvider;
    private Integer topK;
    private Map<String, String> filters;
    private JsonNode advancedFilters;
    private Integer nprobeOverride;
    private Integer rerankDepthOverride;
    private Boolean hybrid;
    private String sparseQuery;
    private Double alpha;
    private Boolean includeEmbeddings;
    private Boolean includeDocuments;
    private Boolean includeMetadata;

    public static SearchRequest text(String queryText) {
        return text(queryText, 10);
    }

    public static SearchRequest text(String queryText, int topK) {
        SearchRequest request = new SearchRequest();
        request.queryText = queryText;
        request.topK = topK;
        return request;
    }

    public static SearchRequest vector(List<Double> query) {
        return vector(query, 10);
    }

    public static SearchRequest vector(List<Double> query, int topK) {
        SearchRequest request = new SearchRequest();
        request.query = query;
        request.topK = topK;
        return request;
    }

    public SearchRequest filters(Map<String, String> filters) { this.filters = filters; return this; }
    public SearchRequest advancedFilters(JsonNode advancedFilters) { this.advancedFilters = advancedFilters; return this; }
    public SearchRequest includeDocuments(boolean includeDocuments) { this.includeDocuments = includeDocuments; return this; }
    public SearchRequest includeMetadata(boolean includeMetadata) { this.includeMetadata = includeMetadata; return this; }
    public SearchRequest includeEmbeddings(boolean includeEmbeddings) { this.includeEmbeddings = includeEmbeddings; return this; }
    public SearchRequest hybrid(String sparseQuery, double alpha) { this.hybrid = true; this.sparseQuery = sparseQuery; this.alpha = alpha; return this; }
    public SearchRequest embeddingOverride(String provider, String model) { this.embeddingProvider = provider; this.embeddingModel = model; return this; }
    public SearchRequest nprobeOverride(int value) { this.nprobeOverride = value; return this; }
    public SearchRequest rerankDepthOverride(int value) { this.rerankDepthOverride = value; return this; }

    public List<Double> getQuery() { return query; }
    public String getQueryText() { return queryText; }
    public String getEmbeddingModel() { return embeddingModel; }
    public String getEmbeddingProvider() { return embeddingProvider; }
    public Integer getTopK() { return topK; }
    public Map<String, String> getFilters() { return filters; }
    public JsonNode getAdvancedFilters() { return advancedFilters; }
    public Integer getNprobeOverride() { return nprobeOverride; }
    public Integer getRerankDepthOverride() { return rerankDepthOverride; }
    public Boolean getHybrid() { return hybrid; }
    public String getSparseQuery() { return sparseQuery; }
    public Double getAlpha() { return alpha; }
    public Boolean getIncludeEmbeddings() { return includeEmbeddings; }
    public Boolean getIncludeDocuments() { return includeDocuments; }
    public Boolean getIncludeMetadata() { return includeMetadata; }
}
