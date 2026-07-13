package com.vectoramp;

import com.vectoramp.http.RestTransport;
import com.vectoramp.http.Transport;
import com.vectoramp.models.AskRequest;
import com.vectoramp.models.AskResponse;
import com.vectoramp.models.SseEvent;
import com.vectoramp.services.ConnectionsClient;
import com.vectoramp.services.DatasetsClient;
import com.vectoramp.services.IngestionClient;
import com.vectoramp.services.IntelligenceClient;
import com.vectoramp.services.OrgSecretsClient;
import com.vectoramp.services.SchedulesClient;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Entry point for the VectorAmp Java SDK.
 *
 * <p>Use {@link #create(String)} for the default hosted API or {@link #builder(String)} to
 * customize transport, timeout, or base URL. Instances are thread-safe as long as the supplied
 * {@link Transport} is thread-safe. Close the client when a custom transport owns resources.</p>
 */
public final class VectorAmpClient implements AutoCloseable {
    /** Default production VectorAmp REST API base URL. */
    public static final String DEFAULT_BASE_URL = "https://api.vectoramp.com";

    private final Transport transport;
    private final DatasetsClient datasets;
    private final IngestionClient ingestion;
    private final IntelligenceClient intelligence;
    private final SchedulesClient schedules;
    private final ConnectionsClient connections;
    private final OrgSecretsClient orgSecrets;

    private VectorAmpClient(Builder builder) {
        this.transport = builder.transport != null
                ? builder.transport
                : new RestTransport(URI.create(builder.baseUrl), builder.apiKey, builder.timeout);
        this.ingestion = new IngestionClient(transport);
        this.intelligence = new IntelligenceClient(transport);
        this.schedules = new SchedulesClient(transport);
        this.connections = new ConnectionsClient(transport);
        this.orgSecrets = new OrgSecretsClient(transport);
        this.datasets = new DatasetsClient(transport, ingestion, intelligence);
    }

    /**
     * Creates a client using {@link #DEFAULT_BASE_URL} and a 30 second request timeout.
     *
     * @param apiKey VectorAmp API key used as a bearer token
     * @return configured SDK client
     */
    public static VectorAmpClient create(String apiKey) {
        return builder(apiKey).build();
    }

    /**
     * Starts a client builder with default base URL and 30 second timeout.
     *
     * @param apiKey VectorAmp API key used as a bearer token
     * @return mutable client builder
     */
    public static Builder builder(String apiKey) {
        return new Builder(apiKey);
    }

    /**
     * @return dataset operations client
     */
    public DatasetsClient datasets() { return datasets; }

    /**
     * @return ingestion source, job, and file-upload operations client
     */
    public IngestionClient ingestion() { return ingestion; }

    /**
     * @return recurring ingestion schedule operations client
     */
    public SchedulesClient schedules() { return schedules; }

    /**
     * Connections client for OAuth connection management ({@code /connections}).
     *
     * <p>Exposes {@code list}/{@code create}/{@code get}/{@code delete} for brokering third-party
     * OAuth credentials that ingestion sources reference by {@code connection_id}.</p>
     *
     * @return OAuth connection operations client
     */
    public ConnectionsClient connections() { return connections; }

    /**
     * @return organization-scoped secret helpers, including OpenAI API key put/update support
     */
    public OrgSecretsClient orgSecrets() { return orgSecrets; }

    /**
     * Intelligence client for RAG queries and session management.
     *
     * <p>Exposes {@code ask}/{@code query}/{@code stream} plus session helpers
     * ({@code createSession}, {@code listSessions}, {@code getSession}, {@code appendMessage},
     * {@code listMessages}). The top-level {@link #ask(String)} and {@link #askStream(String)}
     * convenience methods delegate here.</p>
     *
     * @return intelligence operations client
     */
    public IntelligenceClient intelligence() { return intelligence; }

    /**
     * Runs a non-streaming intelligence query across the API default dataset scope.
     *
     * @param query question or prompt text
     * @return answer and optional source/chunk metadata
     */
    public AskResponse ask(String query) { return intelligence.ask(query); }

    /**
     * Runs a non-streaming intelligence query.
     *
     * @param request query request; optional dataset, topK, source, and conversation settings are honored
     * @return answer and optional source/chunk metadata
     */
    public AskResponse ask(AskRequest request) { return intelligence.ask(request); }

    /**
     * Runs an intelligence query as server-sent events.
     *
     * @param query question or prompt text
     * @return ordered event stream; close it if not fully consumed
     */
    public Stream<SseEvent> askStream(String query) { return intelligence.askStream(query); }

    /**
     * Runs an intelligence query as server-sent events.
     *
     * @param request query request; {@code stream=true} is set automatically
     * @return ordered event stream; close it if not fully consumed
     */
    public Stream<SseEvent> askStream(AskRequest request) { return intelligence.askStream(request); }

    /** Closes the underlying transport if it owns resources. */
    @Override
    public void close() { transport.close(); }

    /** Builder for {@link VectorAmpClient}. Defaults: production base URL and 30 second timeout. */
    public static final class Builder {
        private final String apiKey;
        private String baseUrl = DEFAULT_BASE_URL;
        private Duration timeout = Duration.ofSeconds(30);
        private Transport transport;

        private Builder(String apiKey) { this.apiKey = Objects.requireNonNull(apiKey, "apiKey"); }

        /**
         * Overrides the API base URL.
         *
         * @param baseUrl absolute API base URL, for example a development endpoint
         * @return this builder
         */
        public Builder baseUrl(String baseUrl) { this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl"); return this; }

        /**
         * Overrides the request timeout.
         *
         * @param timeout per-request timeout; default is 30 seconds
         * @return this builder
         */
        public Builder timeout(Duration timeout) { this.timeout = Objects.requireNonNull(timeout, "timeout"); return this; }

        /**
         * Uses a caller-provided transport instead of the default REST transport.
         *
         * @param transport transport implementation, usually for tests or custom HTTP stacks
         * @return this builder
         */
        public Builder transport(Transport transport) { this.transport = Objects.requireNonNull(transport, "transport"); return this; }

        /**
     * @return configured SDK client
     */
        public VectorAmpClient build() { return new VectorAmpClient(this); }
    }
}
