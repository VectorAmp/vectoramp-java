package com.vectoramp;

import com.vectoramp.http.RestTransport;
import com.vectoramp.http.Transport;
import com.vectoramp.models.AskRequest;
import com.vectoramp.models.AskResponse;
import com.vectoramp.models.SseEvent;
import com.vectoramp.services.DatasetsClient;
import com.vectoramp.services.IngestionClient;
import com.vectoramp.services.IntelligenceClient;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.stream.Stream;

/** Entry point for the VectorAmp Java SDK. */
public final class VectorAmpClient implements AutoCloseable {
    public static final String DEFAULT_BASE_URL = "https://api.vectoramp.com";

    private final Transport transport;
    private final DatasetsClient datasets;
    private final IngestionClient ingestion;
    private final IntelligenceClient intelligence;

    private VectorAmpClient(Builder builder) {
        this.transport = builder.transport != null
                ? builder.transport
                : new RestTransport(URI.create(builder.baseUrl), builder.apiKey, builder.timeout);
        this.ingestion = new IngestionClient(transport);
        this.intelligence = new IntelligenceClient(transport);
        this.datasets = new DatasetsClient(transport, ingestion, intelligence);
    }

    public static VectorAmpClient create(String apiKey) {
        return builder(apiKey).build();
    }

    public static Builder builder(String apiKey) {
        return new Builder(apiKey);
    }

    public DatasetsClient datasets() { return datasets; }
    public IngestionClient ingestion() { return ingestion; }
    public AskResponse ask(String query) { return intelligence.ask(query); }
    public AskResponse ask(AskRequest request) { return intelligence.ask(request); }
    public Stream<SseEvent> askStream(String query) { return intelligence.askStream(query); }
    public Stream<SseEvent> askStream(AskRequest request) { return intelligence.askStream(request); }

    @Override
    public void close() { transport.close(); }

    public static final class Builder {
        private final String apiKey;
        private String baseUrl = DEFAULT_BASE_URL;
        private Duration timeout = Duration.ofSeconds(30);
        private Transport transport;

        private Builder(String apiKey) { this.apiKey = Objects.requireNonNull(apiKey, "apiKey"); }
        public Builder baseUrl(String baseUrl) { this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl"); return this; }
        public Builder timeout(Duration timeout) { this.timeout = Objects.requireNonNull(timeout, "timeout"); return this; }
        public Builder transport(Transport transport) { this.transport = Objects.requireNonNull(transport, "transport"); return this; }
        public VectorAmpClient build() { return new VectorAmpClient(this); }
    }
}
