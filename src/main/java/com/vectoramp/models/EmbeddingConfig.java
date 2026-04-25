package com.vectoramp.models;

public class EmbeddingConfig {
    private String provider;
    private String model;

    public EmbeddingConfig() {}

    public EmbeddingConfig(String provider, String model) {
        this.provider = provider;
        this.model = model;
    }

    public static EmbeddingConfig of(String provider, String model) {
        return new EmbeddingConfig(provider, model);
    }

    public String getProvider() { return provider; }
    public String getModel() { return model; }
}
