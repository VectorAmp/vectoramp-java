package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * OAuth connection resource returned by the gateway {@code /connections} endpoints.
 *
 * <p>A connection brokers OAuth credentials for a third-party provider (for example Google or
 * Atlassian) so ingestion sources can reference it via {@code connection_id} instead of embedding
 * raw secrets. When a connection is first created its {@link #getStatus() status} is typically
 * pending and {@link #getAuthorizationUrl() authorizationUrl} points at the provider consent screen
 * the user must visit to finish authorizing.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Connection {
    private String id;
    private String provider;
    private String status;
    private String authorizationUrl;

    /**
     * @return connection id
     */
    public String getId() { return id; }
    /**
     * @return provider key, for example {@code google} or {@code atlassian}
     */
    public String getProvider() { return provider; }
    /**
     * @return connection status, for example {@code pending} or {@code connected}
     */
    public String getStatus() { return status; }
    /**
     * @return provider consent URL to finish authorization, or {@code null} once connected
     */
    public String getAuthorizationUrl() { return authorizationUrl; }
}
