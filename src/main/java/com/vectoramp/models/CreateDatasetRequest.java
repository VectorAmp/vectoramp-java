package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/** Request for creating a dataset. The SDK always sends index_type=sable and exposes no index-type option. */
public class CreateDatasetRequest {
    private final String name;
    private final int dim;
    private final String metric;
    private final EmbeddingConfig embedding;
    private final JsonNode filters;
    private final JsonNode metadataSchema;

    private CreateDatasetRequest(Builder builder) {
        this.name = builder.name;
        this.dim = builder.dim;
        this.metric = builder.metric;
        this.embedding = builder.embedding;
        this.filters = builder.filters;
        this.metadataSchema = builder.metadataSchema;
    }

    public static Builder builder(String name, int dim, String metric, EmbeddingConfig embedding) {
        return new Builder(name, dim, metric, embedding);
    }

    public String getName() { return name; }
    public int getDim() { return dim; }
    public String getMetric() { return metric; }
    public EmbeddingConfig getEmbedding() { return embedding; }
    @JsonProperty("index_type") public String getIndexType() { return "sable"; }
    public JsonNode getFilters() { return filters; }
    public JsonNode getMetadataSchema() { return metadataSchema; }

    public static final class Builder {
        private final String name;
        private final int dim;
        private final String metric;
        private final EmbeddingConfig embedding;
        private JsonNode filters;
        private JsonNode metadataSchema;

        private Builder(String name, int dim, String metric, EmbeddingConfig embedding) {
            this.name = name;
            this.dim = dim;
            this.metric = metric;
            this.embedding = embedding;
        }
        public Builder filters(JsonNode filters) { this.filters = filters; return this; }
        public Builder metadataSchema(JsonNode metadataSchema) { this.metadataSchema = metadataSchema; return this; }
        public CreateDatasetRequest build() { return new CreateDatasetRequest(this); }
    }
}
