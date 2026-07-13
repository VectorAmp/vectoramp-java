package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/** Embedding provider/model configuration used by dataset creation and text embedding. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmbeddingConfig {
    /** Default VectorAmp embedding provider. */
    public static final String DEFAULT_PROVIDER = "vectoramp";
    /** Default VectorAmp embedding model ({@code 2560} dimensions). */
    public static final String DEFAULT_MODEL = "VectorAmp-Embedding-4B";

    private static final Map<String, Integer> KNOWN_DIMENSIONS = Map.of(
            "vectoramp/VectorAmp-Embedding-4B", 2560,
            "openai/text-embedding-3-small", 1536,
            "openai/text-embedding-3-large", 3072);

    private String provider;
    private String model;
    private String secretRef;

    /** Creates an empty config for Jackson or manual population. */
    public EmbeddingConfig() {}

    /**
     * Creates an embedding config.
     * @param provider embedding provider
     * @param model embedding model
     */
    public EmbeddingConfig(String provider, String model) {
        this(provider, model, null);
    }

    /**
     * Creates an embedding config with an optional secret reference.
     * @param provider embedding provider
     * @param model embedding model
     * @param secretRef optional reference to a stored provider credential
     */
    public EmbeddingConfig(String provider, String model, String secretRef) {
        this.provider = provider;
        this.model = model;
        this.secretRef = secretRef;
    }

    /**
     * Creates an embedding config.
     * @param provider embedding provider
     * @param model embedding model
     * @return embedding config
     */
    public static EmbeddingConfig of(String provider, String model) {
        return new EmbeddingConfig(provider, model);
    }

    /**
     * Returns the default VectorAmp embedding ({@code vectoramp/VectorAmp-Embedding-4B}, dim 2560).
     * @return default embedding config
     */
    public static EmbeddingConfig vectoramp() {
        return new EmbeddingConfig(DEFAULT_PROVIDER, DEFAULT_MODEL);
    }

    /**
     * Returns an OpenAI embedding config.
     * @param size {@code "small"} for text-embedding-3-small (dim 1536) or {@code "large"} for
     *             text-embedding-3-large (dim 3072)
     * @return OpenAI embedding config
     */
    public static EmbeddingConfig openai(String size) {
        return openai(size, "emb:openai:api_key");
    }

    /**
     * Returns an OpenAI embedding config with an explicit secret reference.
     * @param size {@code "small"} for text-embedding-3-small (dim 1536) or {@code "large"} for
     *             text-embedding-3-large (dim 3072)
     * @param secretRef organization secret reference containing the OpenAI API key
     * @return OpenAI embedding config
     */
    public static EmbeddingConfig openai(String size, String secretRef) {
        String model;
        if ("small".equalsIgnoreCase(size)) {
            model = "text-embedding-3-small";
        } else if ("large".equalsIgnoreCase(size)) {
            model = "text-embedding-3-large";
        } else {
            throw new IllegalArgumentException("Unknown OpenAI embedding size: " + size + " (expected \"small\" or \"large\")");
        }
        return new EmbeddingConfig("openai", model, secretRef);
    }

    /**
     * Returns a copy of this embedding config using the supplied organization secret reference.
     * @param secretRef organization secret reference
     * @return embedding config with secret reference
     */
    public EmbeddingConfig withSecretRef(String secretRef) {
        return new EmbeddingConfig(provider, model, secretRef);
    }

    /**
     * Infers the vector dimension for this embedding from the built-in model table.
     * @return inferred dimension, or {@code null} for a custom/unknown model
     */
    public Integer inferDimension() {
        if (provider == null || model == null) return null;
        return KNOWN_DIMENSIONS.get(provider + "/" + model);
    }

    /**
     * @return provider
     */
    public String getProvider() { return provider; }
    /**
     * @return model
     */
    public String getModel() { return model; }
    /**
     * @return optional provider secret reference
     */
    public String getSecretRef() { return secretRef; }
}
