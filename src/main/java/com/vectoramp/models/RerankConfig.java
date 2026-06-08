package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonInclude;

/** VectorAmp rerank configuration. Only enabled is required; provider defaults to vectoramp and model to VectorAmp-Rerank-v1. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RerankConfig {
    private Boolean enabled;
    private String provider;
    private String model;

    public RerankConfig() {}

    public RerankConfig(boolean enabled) { this.enabled = enabled; }

    public static RerankConfig enabled() { return new RerankConfig(true); }

    public RerankConfig provider(String provider) { this.provider = provider; return this; }
    public RerankConfig model(String model) { this.model = model; return this; }

    public Boolean getEnabled() { return enabled; }
    public String getProvider() { return provider; }
    public String getModel() { return model; }
}
