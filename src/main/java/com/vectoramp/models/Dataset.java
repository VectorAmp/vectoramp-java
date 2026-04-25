package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.vectoramp.VectorAmpException;
import com.vectoramp.services.DatasetsClient;
import com.vectoramp.services.IngestionClient;
import com.vectoramp.services.IntelligenceClient;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Dataset {
    private String id;
    private String name;
    private String orgId;
    private Integer dim;
    private String metric;
    private Map<String, Object> tuning;
    private EmbeddingConfig embedding;
    private String indexType;
    private JsonNode filters;
    private JsonNode metadataSchema;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @JsonIgnore private DatasetsClient datasets;
    @JsonIgnore private IngestionClient ingestion;
    @JsonIgnore private IntelligenceClient intelligence;
    @JsonIgnore private JsonNode rawData;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getOrgId() { return orgId; }
    public Integer getDim() { return dim; }
    public String getMetric() { return metric; }
    public Map<String, Object> getTuning() { return tuning; }
    public EmbeddingConfig getEmbedding() { return embedding; }
    public String getIndexType() { return indexType; }
    public JsonNode getFilters() { return filters; }
    public JsonNode getMetadataSchema() { return metadataSchema; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    /** Raw dataset JSON returned by the API before SDK response binding. */
    @JsonIgnore public JsonNode getRawData() { return rawData == null ? null : rawData.deepCopy(); }
    @JsonIgnore public DatasetsClient getDatasetsClient() { return datasets; }
    @JsonIgnore public IngestionClient getIngestionClient() { return ingestion; }
    @JsonIgnore public IntelligenceClient getIntelligenceClient() { return intelligence; }

    public Dataset attach(DatasetsClient datasets, IngestionClient ingestion, IntelligenceClient intelligence, JsonNode rawData) {
        this.datasets = datasets;
        this.ingestion = ingestion;
        this.intelligence = intelligence;
        this.rawData = rawData;
        return this;
    }

    public SearchResponse search(SearchRequest request) {
        return requireDatasets().search(requireId(), request);
    }

    public InsertResponse insert(List<VectorRecord> vectors) {
        return requireDatasets().insert(requireId(), vectors);
    }

    public InsertResponse addTexts(List<String> texts) {
        return requireDatasets().addTexts(requireId(), texts);
    }

    public InsertResponse addTexts(AddTextsRequest request) {
        return requireDatasets().addTexts(requireId(), request);
    }

    public List<List<Double>> embed(List<String> texts) {
        return requireDatasets().embed(requireId(), texts);
    }

    public void delete() {
        requireDatasets().delete(requireId());
    }

    public AskResponse ask(String query) {
        return ask(AskRequest.of(query));
    }

    public AskResponse ask(AskRequest request) {
        Objects.requireNonNull(request, "request").datasetId(requireId());
        return requireIntelligence().ask(request);
    }

    public Stream<SseEvent> askStream(String query) {
        return askStream(AskRequest.of(query));
    }

    public Stream<SseEvent> askStream(AskRequest request) {
        Objects.requireNonNull(request, "request").datasetId(requireId());
        return requireIntelligence().askStream(request);
    }

    /** Create a source configured for this dataset. Start it with {@link #ingestSource(Source)}. */
    public Source ingestSource(CreateSourceRequest request) {
        return requireIngestion().createSource(request);
    }

    public IngestionJob ingestSource(String sourceId) {
        return requireIngestion().startJob(sourceId, requireId());
    }

    public IngestionJob ingestSource(Source source) {
        return ingestSource(Objects.requireNonNull(source, "source").getId());
    }

    /**
     * Creates a file-upload source for this dataset and initializes an upload session.
     * Upload bytes to the returned targets, then call {@link IngestionClient#completeUpload(String, String, List)}.
     */
    public UploadSession ingestFiles(String sourceName, List<FileUpload> files) {
        Source source = requireIngestion().createFileUploadSource(requireId(), sourceName);
        return requireIngestion().initializeUpload(source.getId(), files);
    }

    public Source createFileUploadSource(String name) {
        return requireIngestion().createFileUploadSource(requireId(), name);
    }

    private String requireId() {
        if (id == null || id.isBlank()) throw new VectorAmpException("Dataset resource is missing an id");
        return id;
    }

    private DatasetsClient requireDatasets() {
        if (datasets == null) throw detachedResourceException();
        return datasets;
    }

    private IngestionClient requireIngestion() {
        if (ingestion == null) throw detachedResourceException();
        return ingestion;
    }

    private IntelligenceClient requireIntelligence() {
        if (intelligence == null) throw detachedResourceException();
        return intelligence;
    }

    private VectorAmpException detachedResourceException() {
        return new VectorAmpException("Dataset resource is detached; load it through VectorAmpClient.datasets() to use instance methods");
    }
}
