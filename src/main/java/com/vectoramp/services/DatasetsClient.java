package com.vectoramp.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.vectoramp.VectorAmpException;
import com.vectoramp.http.Transport;
import com.vectoramp.models.*;

import java.util.*;

public final class DatasetsClient extends ApiService {
    private final IngestionClient ingestion;
    private final IntelligenceClient intelligence;

    public DatasetsClient(Transport transport) { this(transport, null, null); }

    public DatasetsClient(Transport transport, IngestionClient ingestion, IntelligenceClient intelligence) {
        super(transport);
        this.ingestion = ingestion;
        this.intelligence = intelligence;
    }

    public Page<Dataset> list() { return list(null, null); }

    public Page<Dataset> list(Integer limit, Integer offset) {
        JsonNode root = parseTree(transport.execute(new Transport.Request("GET", "/datasets", pageQuery(limit, offset), Collections.emptyMap(), null)).getBody());
        List<Dataset> datasets = new ArrayList<>();
        for (JsonNode node : root.path("datasets")) {
            datasets.add(toDatasetResource(node));
        }
        return new Page<>(datasets, root.path("total").asInt(), root.path("limit").asInt(), root.path("offset").asInt());
    }

    public Dataset create(String name, int dim, String metric, EmbeddingConfig embedding) {
        return create(CreateDatasetRequest.builder(name, dim, metric, embedding).build());
    }

    public Dataset create(CreateDatasetRequest request) {
        JsonNode node = post("/datasets", request, JsonNode.class);
        return toDatasetResource(node);
    }

    public Dataset get(String datasetId) {
        JsonNode node = get("/datasets/" + encodePath(datasetId), Collections.emptyMap(), JsonNode.class);
        return toDatasetResource(node);
    }

    public void delete(String datasetId) {
        super.delete("/datasets/" + encodePath(datasetId));
    }

    public SearchResponse search(String datasetId, String query) {
        return search(datasetId, SearchRequest.text(query));
    }

    public SearchResponse search(String datasetId, SearchRequest request) {
        return post("/datasets/" + encodePath(datasetId) + "/search", request, SearchResponse.class);
    }

    public InsertResponse insert(String datasetId, List<VectorRecord> vectors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("vectors", vectors);
        return post("/datasets/" + encodePath(datasetId) + "/insert", body, InsertResponse.class);
    }

    /** Embeds text through the dataset then inserts generated vectors with text metadata. */
    public InsertResponse addTexts(String datasetId, AddTextsRequest request) {
        Map<String, Object> embedBody = new LinkedHashMap<>();
        embedBody.put("texts", request.getTexts());
        JsonNode embedResponse = post("/datasets/" + encodePath(datasetId) + "/embed", embedBody, JsonNode.class);
        List<List<Double>> embeddings = MAPPER.convertValue(embedResponse.path("embeddings"), new TypeReference<List<List<Double>>>() {});
        List<VectorRecord> vectors = new ArrayList<>();
        for (int i = 0; i < request.getTexts().size(); i++) {
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("text", request.getTexts().get(i));
            if (request.getMetadata() != null && i < request.getMetadata().size() && request.getMetadata().get(i) != null) {
                metadata.putAll(request.getMetadata().get(i));
            }
            String id = request.getIds() != null && i < request.getIds().size() ? request.getIds().get(i) : UUID.randomUUID().toString();
            vectors.add(VectorRecord.of(id, embeddings.get(i), metadata));
        }
        return insert(datasetId, vectors);
    }

    public InsertResponse addText(String datasetId, String text) {
        return addTexts(datasetId, AddTextsRequest.of(text));
    }

    public InsertResponse addTexts(String datasetId, List<String> texts) {
        return addTexts(datasetId, AddTextsRequest.of(texts));
    }

    public List<List<Double>> embed(String datasetId, List<String> texts) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("texts", texts);
        JsonNode response = post("/datasets/" + encodePath(datasetId) + "/embed", body, JsonNode.class);
        return MAPPER.convertValue(response.path("embeddings"), new TypeReference<List<List<Double>>>() {});
    }

    private Dataset toDatasetResource(JsonNode node) {
        try {
            Dataset dataset = MAPPER.treeToValue(node, Dataset.class);
            return dataset.attach(this, ingestion, intelligence, node.deepCopy());
        } catch (JsonProcessingException e) {
            throw new VectorAmpException("Failed to parse VectorAmp dataset response", e);
        }
    }

    private static String encodePath(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
    }
}
