package com.vectoramp.models;

/** Embedding provider/model configuration used by dataset creation and text embedding. */
public class EmbeddingConfig {
    private String provider;
    private String model;

    /** Creates an empty config for Jackson or manual population. */
    public EmbeddingConfig() {}

    /**
     * Creates an embedding config.
     * @param provider embedding provider
     * @param model embedding model
     */
    public EmbeddingConfig(String provider, String model) {
        this.provider = provider;
        this.model = model;
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
     * @return provider
     */
    public String getProvider() { return provider; }
    /**
     * @return model
     */
    public String getModel() { return model; }
}
