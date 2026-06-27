package com.vectoramp.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.vectoramp.http.Transport;
import com.vectoramp.models.Connection;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Client for OAuth connections, served from the gateway root {@code /connections} namespace
 * (not under {@code /ingestion}).
 *
 * <p>Connections broker third-party OAuth credentials (Google, Atlassian, ...) that ingestion
 * sources reference by {@code connection_id}. The typical flow is {@link #create(String)} to begin
 * authorization, send the user to {@link Connection#getAuthorizationUrl()}, then poll
 * {@link #get(String)} until the status is connected.</p>
 */
public final class ConnectionsClient extends ApiService {
    /**
     * Creates a connections client backed by the supplied transport.
     *
     * @param transport HTTP transport to use for API requests
     */
    public ConnectionsClient(Transport transport) { super(transport); }

    /**
     * Lists all OAuth connections.
     *
     * @return connections for the authenticated organization
     */
    public List<Connection> list() { return list(null); }

    /**
     * Lists OAuth connections, optionally filtered by provider.
     *
     * @param provider optional provider filter (for example {@code google}); {@code null} lists all
     * @return matching connections
     */
    public List<Connection> list(String provider) {
        Map<String, String> query = new LinkedHashMap<>();
        if (provider != null && !provider.isBlank()) query.put("provider", provider);
        JsonNode root = parseTree(transport.execute(new Transport.Request(
                "GET", "/connections", query, Collections.emptyMap(), null)).getBody());
        JsonNode node = root.isArray() ? root : root.path("connections");
        if (node.isMissingNode() || node.isNull()) return Collections.emptyList();
        return MAPPER.convertValue(node, new TypeReference<List<Connection>>() {});
    }

    /**
     * Begins an OAuth connection for a provider.
     *
     * @param provider provider key, for example {@code google} or {@code atlassian}
     * @return created connection, usually pending with an authorization URL
     */
    public Connection create(String provider) { return create(provider, null); }

    /**
     * Begins an OAuth connection for a provider, scoped to a source type.
     *
     * @param provider provider key, for example {@code google} or {@code atlassian}
     * @param sourceType optional source type the connection is for (for example {@code gdrive}); {@code null} is omitted
     * @return created connection, usually pending with an authorization URL
     */
    public Connection create(String provider, String sourceType) {
        Objects.requireNonNull(provider, "provider");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("provider", provider);
        if (sourceType != null) body.put("source_type", sourceType);
        return post("/connections", body, Connection.class);
    }

    /**
     * Fetches a connection by id.
     *
     * @param id connection id
     * @return connection resource
     */
    public Connection get(String id) {
        Objects.requireNonNull(id, "id");
        return get("/connections/" + encodePath(id), Collections.emptyMap(), Connection.class);
    }

    /**
     * Deletes a connection.
     *
     * @param id connection id
     */
    @Override
    public void delete(String id) {
        Objects.requireNonNull(id, "id");
        super.delete("/connections/" + encodePath(id));
    }
}
