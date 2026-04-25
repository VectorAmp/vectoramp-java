package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/** Request for creating a SABLE dataset. The SDK always sends {@code index_type=sable}. */
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

    /**
     * Starts a SABLE dataset creation request.
     *
     * @param name dataset display name
     * @param dim vector dimensionality
     * @param metric distance metric accepted by the API, for example {@code cosine}
     * @param embedding embedding provider/model config used for text embedding
     * @return dataset request builder
     */
    public static Builder builder(String name, int dim, String metric, EmbeddingConfig embedding) {
        return new Builder(name, dim, metric, embedding);
    }

    /**
     * @return name
     */
    public String getName() { return name; }
    /**
     * @return dim
     */
    public int getDim() { return dim; }
    /**
     * @return metric
     */
    public String getMetric() { return metric; }
    /**
     * @return embedding
     */
    public EmbeddingConfig getEmbedding() { return embedding; }
    /**
     * @return "sable"
     */
    @JsonProperty("index_type") public String getIndexType() { return "sable"; }
    /**
     * @return filters
     */
    public JsonNode getFilters() { return filters; }
    /**
     * @return metadataSchema
     */
    public JsonNode getMetadataSchema() { return metadataSchema; }

    /** Builder for CreateDatasetRequest inputs. */
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
        /**
     * Sets optional filter schema/configuration.
     * @param filters raw filter JSON
     * @return this builder
     */
        public Builder filters(JsonNode filters) { this.filters = filters; return this; }
        /**
     * Sets optional metadata schema.
     * @param metadataSchema raw schema JSON
     * @return this builder
     */
        public Builder metadataSchema(JsonNode metadataSchema) { this.metadataSchema = metadataSchema; return this; }
        /**
     * @return immutable create-dataset request
     */
        public CreateDatasetRequest build() { return new CreateDatasetRequest(this); }
    }
}
