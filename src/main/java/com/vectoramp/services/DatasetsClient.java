package com.vectoramp.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.vectoramp.VectorAmpException;
import com.vectoramp.http.Transport;
import com.vectoramp.models.*;

import java.util.*;

/** Client for dataset lifecycle, vector search, embedding, and direct vector insertion APIs. */
public final class DatasetsClient extends ApiService {
    private final IngestionClient ingestion;
    private final IntelligenceClient intelligence;

    /**
     * Creates a dataset client backed by the supplied transport.
     *
     * @param transport HTTP transport to use for API requests
     */
    public DatasetsClient(Transport transport) { this(transport, null, null); }

    /**
     * Creates a dataset client and links helper clients onto returned dataset resources.
     *
     * @param transport HTTP transport to use for API requests
     * @param ingestion optional ingestion client attached to returned datasets for convenience methods
     * @param intelligence optional intelligence client attached to returned datasets for ask methods
     */
    public DatasetsClient(Transport transport, IngestionClient ingestion, IntelligenceClient intelligence) {
        super(transport);
        this.ingestion = ingestion;
        this.intelligence = intelligence;
    }

    /**
     * Lists datasets using API defaults for pagination.
     *
     * @return page of datasets with total, limit, and offset
     */
    public Page<Dataset> list() { return list(null, null); }

    /**
     * Lists datasets.
     *
     * @param limit optional maximum number of datasets; {@code null} uses the API default
     * @param offset optional starting offset; {@code null} uses the API default
     * @return page of datasets with total, limit, and offset
     */
    public Page<Dataset> list(Integer limit, Integer offset) {
        JsonNode root = parseTree(transport.execute(new Transport.Request("GET", "/datasets", pageQuery(limit, offset), Collections.emptyMap(), null)).getBody());
        List<Dataset> datasets = new ArrayList<>();
        for (JsonNode node : root.path("datasets")) {
            datasets.add(toDatasetResource(node));
        }
        return new Page<>(datasets, root.path("total").asInt(), root.path("limit").asInt(), root.path("offset").asInt());
    }

    /**
     * Creates a SABLE dataset with only a name.
     *
     * <p>Uses the default embedding {@code vectoramp/VectorAmp-Embedding-4B}, inferred
     * dimension {@code 2560}, and {@code cosine} metric. The SDK always sends
     * {@code index_type=sable}.</p>
     *
     * @param name dataset display name
     * @return created dataset resource
     */
    public Dataset create(String name) {
        return create(CreateDatasetRequest.builder(name).build());
    }

    /**
     * Creates a SABLE dataset with only a name, optionally enabling hybrid dense/sparse search.
     *
     * @param name dataset display name
     * @param hybrid true to enable hybrid; maps to {@code hybrid:true} in the create body
     * @return created dataset resource
     */
    public Dataset create(String name, boolean hybrid) {
        return create(CreateDatasetRequest.builder(name).hybrid(hybrid).build());
    }

    /**
     * Creates a SABLE dataset.
     *
     * @param name dataset display name
     * @param dim vector dimensionality
     * @param metric distance metric accepted by the API, for example {@code cosine}
     * @param embedding embedding provider/model config used for text embedding
     * @return created dataset resource
     */
    public Dataset create(String name, int dim, String metric, EmbeddingConfig embedding) {
        return create(CreateDatasetRequest.builder(name, dim, metric, embedding).build());
    }

    /**
     * Creates a SABLE dataset. The SDK always sends {@code index_type=sable}; no other index type is exposed.
     *
     * @param request dataset creation request
     * @return created dataset resource
     */
    public Dataset create(CreateDatasetRequest request) {
        JsonNode node = post("/datasets", request, JsonNode.class);
        return toDatasetResource(node);
    }

    /**
     * Fetches a dataset by ID.
     *
     * @param datasetId dataset ID
     * @return dataset resource
     */
    public Dataset get(String datasetId) {
        JsonNode node = get("/datasets/" + encodePath(datasetId), Collections.emptyMap(), JsonNode.class);
        return toDatasetResource(node);
    }

    /**
     * Deletes a dataset.
     *
     * @param datasetId dataset ID
     */
    public void delete(String datasetId) {
        super.delete("/datasets/" + encodePath(datasetId));
    }


    /**
     * Lists retained source documents for a dataset using cursor pagination.
     *
     * @param datasetId dataset ID
     * @param limit optional maximum documents; {@code null} uses the API default
     * @param cursor optional cursor from a previous page's {@code next_cursor}
     * @param status optional document status filter, for example {@code ready}
     * @return cursor-paginated document page
     */
    public DatasetDocumentPage listDocuments(String datasetId, Integer limit, String cursor, String status) {
        JsonNode root = parseTree(transport.execute(new Transport.Request("GET", "/datasets/" + encodePath(datasetId) + "/documents", documentListQuery(limit, cursor, status), Collections.emptyMap(), null)).getBody());
        List<DatasetDocument> documents = MAPPER.convertValue(root.path("documents"), new TypeReference<List<DatasetDocument>>() {});
        String nextCursor = root.path("next_cursor").isMissingNode() || root.path("next_cursor").isNull() ? null : root.path("next_cursor").asText();
        Integer returnedLimit = root.path("limit").isMissingNode() || root.path("limit").isNull() ? limit : root.path("limit").asInt();
        return new DatasetDocumentPage(documents, nextCursor, returnedLimit);
    }

    /**
     * Lists retained source documents using API pagination defaults.
     *
     * @param datasetId dataset ID
     * @return cursor-paginated document page
     */
    public DatasetDocumentPage listDocuments(String datasetId) {
        return listDocuments(datasetId, null, null, null);
    }

    /**
     * Downloads the retained original bytes for a dataset document.
     *
     * <p>The default transport follows redirects so callers receive the final raw object bytes.</p>
     *
     * @param datasetId dataset ID
     * @param documentId document ID from {@link #listDocuments(String, Integer, String, String)}
     * @return raw document bytes
     */
    public byte[] downloadDocument(String datasetId, String documentId) {
        return download("/datasets/" + encodePath(datasetId) + "/documents/" + encodePath(documentId) + "/download");
    }

    /**
     * Searches a dataset with text using API defaults, including {@code topK=10}.
     *
     * @param datasetId dataset ID
     * @param query text query to embed and search
     * @return search response with ranked results
     */
    public SearchResponse search(String datasetId, String query) {
        return search(datasetId, SearchRequest.text(query));
    }

    /**
     * Searches a dataset.
     *
     * @param datasetId dataset ID
     * @param request search request; optional fields omitted from JSON use API defaults
     * @return search response with ranked results
     */
    public SearchResponse search(String datasetId, SearchRequest request) {
        return post("/datasets/" + encodePath(datasetId) + "/search", request, SearchResponse.class);
    }

    /**
     * Inserts pre-computed vectors into a dataset.
     *
     * @param datasetId dataset ID
     * @param vectors vector records with IDs, values, and optional metadata
     * @return insert count response
     */
    public InsertResponse insert(String datasetId, List<VectorRecord> vectors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("vectors", vectors);
        return post("/datasets/" + encodePath(datasetId) + "/insert", body, InsertResponse.class);
    }

    /**
     * Embeds text through the dataset and inserts generated vectors with text metadata.
     *
     * @param datasetId dataset ID
     * @param request texts plus optional IDs and per-text metadata; missing IDs are generated UUIDs
     * @return insert count response
     */
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
            Object id = request.getIds() != null && i < request.getIds().size() && request.getIds().get(i) != null
                    ? request.getIds().get(i)
                    : UUID.randomUUID().toString();
            vectors.add(VectorRecord.ofId(id, embeddings.get(i), metadata));
        }
        return insert(datasetId, vectors);
    }

    /**
     * Embeds and inserts one text record with a generated vector ID.
     *
     * @param datasetId dataset ID
     * @param text text to embed and store as metadata.text
     * @return insert count response
     */
    public InsertResponse addText(String datasetId, String text) {
        return addTexts(datasetId, AddTextsRequest.of(text));
    }

    /**
     * Embeds and inserts text records with generated vector IDs.
     *
     * @param datasetId dataset ID
     * @param texts texts to embed and store as metadata.text
     * @return insert count response
     */
    public InsertResponse addTexts(String datasetId, List<String> texts) {
        return addTexts(datasetId, AddTextsRequest.of(texts));
    }

    /**
     * Embeds text with the dataset embedding configuration without inserting records.
     *
     * @param datasetId dataset ID
     * @param texts texts to embed
     * @return embeddings in the same order as {@code texts}
     */
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
}
