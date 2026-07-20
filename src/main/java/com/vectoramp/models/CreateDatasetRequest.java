package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

/**
 * Request for creating a SABLE dataset. The SDK always sends {@code index_type=sable}.
 *
 * <p>Only a {@code name} is required. When omitted, the embedding defaults to
 * {@code vectoramp/VectorAmp-Embedding-4B}, {@code dim} is inferred from the embedding
 * model ({@code 2560} for the default), and {@code metric} defaults to {@code cosine}.
 * Provide {@code dim} explicitly only for a custom/unknown embedding model.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateDatasetRequest {
    private final String name;
    private final Integer dim;
    private final String metric;
    private final EmbeddingConfig embedding;
    private final Boolean hybrid;
    private final Map<String, Object> metadata;
    private final JsonNode filters;
    private final JsonNode metadataSchema;
    private final List<MetadataSchemaField> schema;

    private CreateDatasetRequest(Builder builder) {
        this.name = GenericSource.requireText(builder.name, "name");
        this.embedding = builder.embedding != null ? builder.embedding : EmbeddingConfig.vectoramp();
        Integer resolvedDim = builder.dim != null ? builder.dim : this.embedding.inferDimension();
        if (resolvedDim == null) {
            throw new IllegalArgumentException(
                    "dim could not be inferred for embedding " + this.embedding.getProvider() + "/" + this.embedding.getModel()
                            + "; set dim explicitly via builder.dim(...)");
        }
        this.dim = resolvedDim;
        this.metric = builder.metric != null ? builder.metric : "cosine";
        this.hybrid = builder.hybrid;
        this.metadata = builder.metadata;
        this.filters = builder.filters;
        this.metadataSchema = builder.metadataSchema;
        this.schema = builder.schema;
    }

    /**
     * Starts a minimal dataset creation request with sensible defaults.
     *
     * <p>Defaults: embedding {@code vectoramp/VectorAmp-Embedding-4B}, {@code dim} inferred
     * (2560), and {@code metric} {@code cosine}. Override any of them on the returned builder.</p>
     *
     * @param name dataset display name
     * @return dataset request builder
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }

    /**
     * Starts a SABLE dataset creation request with explicit dimension, metric, and embedding.
     *
     * @param name dataset display name
     * @param dim vector dimensionality
     * @param metric distance metric accepted by the API, for example {@code cosine}
     * @param embedding embedding provider/model config used for text embedding
     * @return dataset request builder
     */
    public static Builder builder(String name, int dim, String metric, EmbeddingConfig embedding) {
        return new Builder(name).dim(dim).metric(metric).embedding(embedding);
    }

    /**
     * @return name
     */
    public String getName() { return name; }
    /**
     * @return dim
     */
    public Integer getDim() { return dim; }
    /**
     * @return metric
     */
    public String getMetric() { return metric; }
    /**
     * @return embedding
     */
    public EmbeddingConfig getEmbedding() { return embedding; }
    /**
     * @return hybrid flag, or {@code null} to use the API default
     */
    public Boolean getHybrid() { return hybrid; }
    /**
     * @return metadata
     */
    public Map<String, Object> getMetadata() { return metadata; }
    /**
     * @return "sable"
     */
    @JsonProperty("index_type") public String getIndexType() { return "sable"; }
    /**
     * @return filters
     */
    public JsonNode getFilters() { return filters; }
    /**
     * @return legacy raw metadata schema
     */
    public JsonNode getMetadataSchema() { return metadataSchema; }
    /**
     * @return canonical typed metadata schema
     */
    public List<MetadataSchemaField> getSchema() { return schema; }

    /** Builder for CreateDatasetRequest inputs. */
    public static final class Builder {
        private final String name;
        private Integer dim;
        private String metric;
        private EmbeddingConfig embedding;
        private Boolean hybrid;
        private Map<String, Object> metadata;
        private JsonNode filters;
        private JsonNode metadataSchema;
        private List<MetadataSchemaField> schema;

        private Builder(String name) {
            this.name = name;
        }

        /**
         * Sets the vector dimensionality. Optional when the embedding model has a known dimension.
         * @param dim vector dimensionality
         * @return this builder
         */
        public Builder dim(int dim) { this.dim = dim; return this; }
        /**
         * Sets the distance metric. Defaults to {@code cosine} when omitted.
         * @param metric distance metric accepted by the API
         * @return this builder
         */
        public Builder metric(String metric) { this.metric = metric; return this; }
        /**
         * Sets the embedding provider/model config. Defaults to {@code vectoramp/VectorAmp-Embedding-4B}.
         * @param embedding embedding config
         * @return this builder
         */
        public Builder embedding(EmbeddingConfig embedding) { this.embedding = embedding; return this; }
        /**
         * Enables a hybrid dense/sparse dataset; maps to {@code hybrid:true} in the create body.
         * @param hybrid true to enable hybrid
         * @return this builder
         */
        public Builder hybrid(boolean hybrid) { this.hybrid = hybrid; return this; }
        /**
         * Sets optional dataset metadata.
         * @param metadata metadata map
         * @return this builder
         */
        public Builder metadata(Map<String, Object> metadata) { this.metadata = metadata; return this; }
        /**
         * Sets optional filter schema/configuration.
         * @param filters raw filter JSON
         * @return this builder
         */
        public Builder filters(JsonNode filters) { this.filters = filters; return this; }
        /**
         * Sets optional metadata schema using the legacy raw JSON input.
         * @param metadataSchema raw schema JSON
         * @return this builder
         */
        public Builder metadataSchema(JsonNode metadataSchema) { this.metadataSchema = metadataSchema; return this; }
        /**
         * Sets the canonical typed metadata schema.
         * @param schema typed schema fields
         * @return this builder
         */
        public Builder metadataSchemaFields(List<MetadataSchemaField> schema) { this.schema = schema; return this; }
        /**
         * @return immutable create-dataset request
         */
        public CreateDatasetRequest build() { return new CreateDatasetRequest(this); }
    }
}
