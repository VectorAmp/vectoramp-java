package com.vectoramp.models;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Web ingestion source input. Optional builder fields omitted from JSON use API defaults. */
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

    /**
     * Starts a web source builder with an explicit source name.
     * @param name source name
     * @return source builder
     */
    public static Builder builder(String name) { return new Builder(name); }
    /**
     * Starts a web source builder named from the URL.
     * @param url URL to crawl
     * @return source builder
     */
    public static Builder forUrl(String url) { return builder(GenericSource.defaultName(SourceType.WEB, url)).url(url); }
    /**
     * Creates a web source named from the URL.
     * @param url URL to crawl
     * @return source input
     */
    public static WebSource of(String url) { return forUrl(url).build(); }
    /**
     * Creates a named web source.
     * @param name source name
     * @param url URL to crawl
     * @return source input
     */
    public static WebSource of(String name, String url) { return builder(name).url(url).build(); }

    /**
     * @return SourceType.WEB
     */
    public String getSourceType() { return SourceType.WEB; }
    /**
     * @return name
     */
    public String getName() { return name; }
    /**
     * @return description
     */
    public String getDescription() { return description; }
    /**
     * @return config
     */
    public Map<String, Object> getConfig() { return config; }
    /**
     * @return metadata
     */
    public Map<String, Object> getMetadata() { return metadata; }

    @Override public CreateSourceRequest toCreateSourceRequest() {
        return new CreateSourceRequest(SourceType.WEB, name, description, config, metadata);
    }

    /** Builder for WebSource inputs. */
    public static final class Builder {
        private final String name;
        private String description;
        private final Map<String, Object> config = new LinkedHashMap<>();
        private final Map<String, Object> metadata = new LinkedHashMap<>();

        private Builder(String name) { this.name = name; }
        /**
     * Sets the URL to crawl.
     * @param url URL to crawl
     * @return this builder
     */
        public Builder url(String url) { GenericSource.put(config, "url", url); return this; }
        /**
     * Sets multiple URLs to crawl.
     * @param urls URLs to crawl
     * @return this builder
     */
        public Builder urls(List<String> urls) { GenericSource.put(config, "urls", urls); return this; }
        /**
     * Sets optional crawl depth; null is omitted.
     * @param crawlDepth crawl depth
     * @return this builder
     */
        public Builder crawlDepth(Integer crawlDepth) { GenericSource.put(config, "crawl_depth", crawlDepth); return this; }
        /**
     * Sets optional sync mode; API default applies when omitted.
     * @param syncMode sync mode
     * @return this builder
     */
        public Builder syncMode(String syncMode) { GenericSource.put(config, "sync_mode", syncMode); return this; }
        /**
     * Sets optional source description.
     * @param description description text
     * @return this builder
     */
        public Builder description(String description) { this.description = description; return this; }
        /**
     * Adds an optional config value; null values are omitted.
     * @param key config key
     * @param value config value
     * @return this builder
     */
        public Builder config(String key, Object value) { GenericSource.put(config, key, value); return this; }
        /**
     * Adds optional metadata; null values are omitted.
     * @param key metadata key
     * @param value metadata value
     * @return this builder
     */
        public Builder metadata(String key, Object value) { GenericSource.put(metadata, key, value); return this; }
        /**
     * Adds optional metadata values; null values are omitted.
     * @param values metadata values
     * @return this builder
     */
        public Builder metadata(Map<String, Object> values) { GenericSource.putAll(metadata, values); return this; }
        /**
     * @return immutable source input
     */
        public WebSource build() { return new WebSource(this); }
    }
}
