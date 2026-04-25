package com.vectoramp.models;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class WebSource implements IngestionSourceInput {
    private final String name;
    private final String description;
    private final Map<String, Object> config;
    private final Map<String, Object> metadata;

    private WebSource(Builder builder) {
        this.name = GenericSource.requireText(builder.name, "name");
        this.description = builder.description;
        this.config = Map.copyOf(builder.config);
        this.metadata = Map.copyOf(builder.metadata);
    }

    public static Builder builder(String name) { return new Builder(name); }
    public static Builder forUrl(String url) { return builder(GenericSource.defaultName(SourceType.WEB, url)).url(url); }
    public static WebSource of(String url) { return forUrl(url).build(); }
    public static WebSource of(String name, String url) { return builder(name).url(url).build(); }

    public String getSourceType() { return SourceType.WEB; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Map<String, Object> getConfig() { return config; }
    public Map<String, Object> getMetadata() { return metadata; }

    @Override public CreateSourceRequest toCreateSourceRequest() {
        return new CreateSourceRequest(SourceType.WEB, name, description, config, metadata);
    }

    public static final class Builder {
        private final String name;
        private String description;
        private final Map<String, Object> config = new LinkedHashMap<>();
        private final Map<String, Object> metadata = new LinkedHashMap<>();

        private Builder(String name) { this.name = name; }
        public Builder url(String url) { GenericSource.put(config, "url", url); return this; }
        public Builder urls(List<String> urls) { GenericSource.put(config, "urls", urls); return this; }
        public Builder crawlDepth(Integer crawlDepth) { GenericSource.put(config, "crawl_depth", crawlDepth); return this; }
        public Builder syncMode(String syncMode) { GenericSource.put(config, "sync_mode", syncMode); return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder config(String key, Object value) { GenericSource.put(config, key, value); return this; }
        public Builder metadata(String key, Object value) { GenericSource.put(metadata, key, value); return this; }
        public Builder metadata(Map<String, Object> values) { GenericSource.putAll(metadata, values); return this; }
        public WebSource build() { return new WebSource(this); }
    }
}
