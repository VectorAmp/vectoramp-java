package com.vectoramp.models;

import java.util.List;
import java.util.Map;

/**
 * Confluence ingestion source input.
 *
 * <p>Either a {@code cloud_id} (Atlassian OAuth site id) or a {@code base_url}
 * (for example {@code https://company.atlassian.net}) identifies the site. {@code auth_mode}
 * defaults to {@code basic} server-side; provide {@code username}/{@code api_token} for basic
 * auth or {@code oauth_credentials} for OAuth.</p>
 */
public final class ConfluenceSource implements IngestionSourceInput {
    private final GenericSource delegate;

    private ConfluenceSource(GenericSource delegate) { this.delegate = delegate; }

    /**
     * Starts a Confluence source builder.
     * @param name source name
     * @return builder configured for the Confluence source type
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }

    /**
     * Creates a Confluence source from an Atlassian OAuth cloud id, named from the cloud id.
     * @param cloudId Atlassian OAuth cloud/site id
     * @return source input
     */
    public static ConfluenceSource of(String cloudId) {
        return of(GenericSource.defaultName(SourceType.CONFLUENCE, cloudId), cloudId);
    }

    /**
     * Creates a named Confluence source from an Atlassian OAuth cloud id.
     * @param name source name
     * @param cloudId Atlassian OAuth cloud/site id
     * @return source input
     */
    public static ConfluenceSource of(String name, String cloudId) {
        return builder(name).cloudId(cloudId).build();
    }

    /**
     * Creates a named Confluence source from an Atlassian OAuth cloud id, restricted to spaces.
     * @param name source name
     * @param cloudId Atlassian OAuth cloud/site id
     * @param spaces space keys to ingest; empty/null ingests all accessible spaces
     * @return source input
     */
    public static ConfluenceSource of(String name, String cloudId, List<String> spaces) {
        return builder(name).cloudId(cloudId).spaces(spaces).build();
    }

    /**
     * Creates a basic-auth Confluence source from a base URL and credentials.
     * @param baseUrl site URL, for example {@code https://company.atlassian.net}
     * @param username basic-auth username (typically an email)
     * @param apiToken Atlassian API token
     * @return source input
     */
    public static ConfluenceSource basicAuth(String baseUrl, String username, String apiToken) {
        return builder(GenericSource.defaultName(SourceType.CONFLUENCE, baseUrl))
                .baseUrl(baseUrl)
                .basicAuth(username, apiToken)
                .build();
    }

    /**
     * @return SourceType.CONFLUENCE
     */
    public String getSourceType() { return SourceType.CONFLUENCE; }

    @Override public CreateSourceRequest toCreateSourceRequest() { return delegate.toCreateSourceRequest(); }

    /** Builder for ConfluenceSource inputs. */
    public static final class Builder {
        private final GenericSource.Builder delegate;

        private Builder(String name) {
            this.delegate = GenericSource.builder(SourceType.CONFLUENCE, name);
        }

        /**
         * Sets the Atlassian OAuth cloud/site id.
         * @param cloudId cloud id
         * @return this builder
         */
        public Builder cloudId(String cloudId) { delegate.config("cloud_id", cloudId); return this; }
        /**
         * Sets the Confluence base URL, for example {@code https://company.atlassian.net}.
         * @param baseUrl base URL
         * @return this builder
         */
        public Builder baseUrl(String baseUrl) { delegate.config("base_url", baseUrl); return this; }
        /**
         * Configures basic auth with a username and API token (sets {@code auth_mode=basic}).
         * @param username basic-auth username
         * @param apiToken Atlassian API token
         * @return this builder
         */
        public Builder basicAuth(String username, String apiToken) {
            delegate.config("auth_mode", "basic").config("username", username).config("api_token", apiToken);
            return this;
        }
        /**
         * Configures OAuth credentials (sets {@code auth_mode=oauth}).
         * @param oauthCredentials Atlassian OAuth credential map
         * @return this builder
         */
        public Builder oauth(Map<String, Object> oauthCredentials) {
            delegate.config("auth_mode", "oauth").config("oauth_credentials", oauthCredentials);
            return this;
        }
        /**
         * Restricts ingestion to the given space keys; empty/null ingests all accessible spaces.
         * @param spaces space keys
         * @return this builder
         */
        public Builder spaces(List<String> spaces) { delegate.config("spaces", spaces); return this; }
        /**
         * Includes page attachments. Defaults to false server-side.
         * @param includeAttachments true to ingest attachments
         * @return this builder
         */
        public Builder includeAttachments(boolean includeAttachments) { delegate.config("include_attachments", includeAttachments); return this; }
        /**
         * Sets the sync mode; API default ({@code incremental}) applies when omitted.
         * @param syncMode sync mode, {@code full} or {@code incremental}
         * @return this builder
         */
        public Builder syncMode(String syncMode) { delegate.config("sync_mode", syncMode); return this; }
        /**
         * Sets an optional source description.
         * @param description description text
         * @return this builder
         */
        public Builder description(String description) { delegate.description(description); return this; }
        /**
         * Adds an optional config value; null values are omitted.
         * @param key config key
         * @param value config value
         * @return this builder
         */
        public Builder config(String key, Object value) { delegate.config(key, value); return this; }
        /**
         * Adds optional metadata; null values are omitted.
         * @param key metadata key
         * @param value metadata value
         * @return this builder
         */
        public Builder metadata(String key, Object value) { delegate.metadata(key, value); return this; }
        /**
         * Adds optional metadata values; null values are omitted.
         * @param values metadata values
         * @return this builder
         */
        public Builder metadata(Map<String, Object> values) { delegate.metadata(values); return this; }
        /**
         * @return immutable Confluence source input
         */
        public ConfluenceSource build() { return new ConfluenceSource(delegate.build()); }
    }
}
