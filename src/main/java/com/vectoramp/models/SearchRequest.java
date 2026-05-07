package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;

/** Search request for text or vector dataset search. Optional fields omitted from JSON use API defaults. */
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    /**
     * Creates a text search request with {@code topK=10}.
     * For hybrid indexes, this single text field lets the API generate the dense embedding
     * and reuse the same text as the sparse query when the dataset embedding model is configured.
     * @param queryText text/search_text to embed and search
     * @return search request
     */
    public static SearchRequest text(String queryText) {
        return text(queryText, 10);
    }

    /**
     * Alias for {@link #text(String)} matching the public API's search_text terminology.
     * @param searchText text/search_text to embed and search
     * @return search request
     */
    public static SearchRequest searchText(String searchText) {
        return text(searchText);
    }

    /**
     * Alias for {@link #text(String, int)} matching the public API's search_text terminology.
     * @param searchText text/search_text to embed and search
     * @param topK maximum result count
     * @return search request
     */
    public static SearchRequest searchText(String searchText, int topK) {
        return text(searchText, topK);
    }

    /**
     * Creates a text search request.
     * @param queryText text to embed and search
     * @param topK maximum result count
     * @return search request
     */
    public static SearchRequest text(String queryText, int topK) {
        SearchRequest request = new SearchRequest();
        request.queryText = queryText;
        request.topK = topK;
        return request;
    }

    /**
     * Creates a vector search request with {@code topK=10}.
     * @param query query vector
     * @return search request
     */
    public static SearchRequest vector(List<Double> query) {
        return vector(query, 10);
    }

    /**
     * Creates a vector search request.
     * @param query query vector
     * @param topK maximum result count
     * @return search request
     */
    public static SearchRequest vector(List<Double> query, int topK) {
        SearchRequest request = new SearchRequest();
        request.query = query;
        request.topK = topK;
        return request;
    }

    /**
     * Sets exact-match metadata filters.
     * @param filters filter key/value pairs
     * @return this request
     */
    public SearchRequest filters(Map<String, String> filters) { this.filters = filters; return this; }
    /**
     * Sets advanced filter JSON.
     * @param advancedFilters raw advanced-filter expression
     * @return this request
     */
    public SearchRequest advancedFilters(JsonNode advancedFilters) { this.advancedFilters = advancedFilters; return this; }
    /**
     * Controls document fields in results; API default is false.
     * @param includeDocuments true to include doc_kind/doc_value
     * @return this request
     */
    public SearchRequest includeDocuments(boolean includeDocuments) { this.includeDocuments = includeDocuments; return this; }
    /**
     * Controls result metadata in responses; API default is true and filtering still works when false.
     * @param includeMetadata true to include metadata
     * @return this request
     */
    public SearchRequest includeMetadata(boolean includeMetadata) { this.includeMetadata = includeMetadata; return this; }
    /**
     * Controls embedding vectors in results; API default is false.
     * @param includeEmbeddings true to include embeddings
     * @return this request
     */
    public SearchRequest includeEmbeddings(boolean includeEmbeddings) { this.includeEmbeddings = includeEmbeddings; return this; }
    /**
     * Enables hybrid dense/sparse search.
     * @param sparseQuery sparse text query
     * @param alpha dense/sparse blending weight
     * @return this request
     */
    public SearchRequest hybrid(String sparseQuery, double alpha) { this.hybrid = true; this.sparseQuery = sparseQuery; this.alpha = alpha; return this; }
    /**
     * Overrides text embedding provider/model for this search.
     * @param provider embedding provider
     * @param model embedding model
     * @return this request
     */
    public SearchRequest embeddingOverride(String provider, String model) { this.embeddingProvider = provider; this.embeddingModel = model; return this; }
    /**
     * Overrides SABLE nprobe for this search.
     * @param value nprobe value
     * @return this request
     */
    public SearchRequest nprobeOverride(int value) { this.nprobeOverride = value; return this; }
    /**
     * Overrides rerank depth for this search.
     * @param value rerank depth
     * @return this request
     */
    public SearchRequest rerankDepthOverride(int value) { this.rerankDepthOverride = value; return this; }

    /**
     * @return query
     */
    public List<Double> getQuery() { return query; }
    /**
     * @return queryText
     */
    public String getQueryText() { return queryText; }
    /**
     * @return embeddingModel
     */
    public String getEmbeddingModel() { return embeddingModel; }
    /**
     * @return embeddingProvider
     */
    public String getEmbeddingProvider() { return embeddingProvider; }
    /**
     * @return topK
     */
    public Integer getTopK() { return topK; }
    /**
     * @return filters
     */
    public Map<String, String> getFilters() { return filters; }
    /**
     * @return advancedFilters
     */
    public JsonNode getAdvancedFilters() { return advancedFilters; }
    /**
     * @return nprobeOverride
     */
    public Integer getNprobeOverride() { return nprobeOverride; }
    /**
     * @return rerankDepthOverride
     */
    public Integer getRerankDepthOverride() { return rerankDepthOverride; }
    /**
     * @return hybrid
     */
    public Boolean getHybrid() { return hybrid; }
    /**
     * @return sparseQuery
     */
    public String getSparseQuery() { return sparseQuery; }
    /**
     * @return alpha
     */
    public Double getAlpha() { return alpha; }
    /**
     * @return includeEmbeddings
     */
    public Boolean getIncludeEmbeddings() { return includeEmbeddings; }
    /**
     * @return includeDocuments
     */
    public Boolean getIncludeDocuments() { return includeDocuments; }
    /**
     * @return includeMetadata
     */
    public Boolean getIncludeMetadata() { return includeMetadata; }
}
