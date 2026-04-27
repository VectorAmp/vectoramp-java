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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Dataset resource returned by the API, with attached convenience methods when loaded through the SDK client. */
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

    /**
     * @return id
     */
    public String getId() { return id; }
    /**
     * @return name
     */
    public String getName() { return name; }
    /**
     * @return orgId
     */
    public String getOrgId() { return orgId; }
    /**
     * @return dim
     */
    public Integer getDim() { return dim; }
    /**
     * @return metric
     */
    public String getMetric() { return metric; }
    /**
     * @return tuning
     */
    public Map<String, Object> getTuning() { return tuning; }
    /**
     * @return embedding
     */
    public EmbeddingConfig getEmbedding() { return embedding; }
    /**
     * @return indexType
     */
    public String getIndexType() { return indexType; }
    /**
     * @return filters
     */
    public JsonNode getFilters() { return filters; }
    /**
     * @return metadataSchema
     */
    public JsonNode getMetadataSchema() { return metadataSchema; }
    /**
     * @return createdAt
     */
    public OffsetDateTime getCreatedAt() { return createdAt; }
    /**
     * @return updatedAt
     */
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    /** Raw dataset JSON returned by the API before SDK response binding. */
    /**
     * @return rawData
     */
    @JsonIgnore public JsonNode getRawData() { return rawData == null ? null : rawData.deepCopy(); }
    /**
     * @return datasets
     */
    @JsonIgnore public DatasetsClient getDatasetsClient() { return datasets; }
    /**
     * @return ingestion
     */
    @JsonIgnore public IngestionClient getIngestionClient() { return ingestion; }
    /**
     * @return intelligence
     */
    @JsonIgnore public IntelligenceClient getIntelligenceClient() { return intelligence; }

    /**
     * Attaches SDK clients used by resource convenience methods.
     *
     * @param datasets dataset client
     * @param ingestion optional ingestion client
     * @param intelligence optional intelligence client
     * @param rawData raw API payload
     * @return this dataset
     */
    public Dataset attach(DatasetsClient datasets, IngestionClient ingestion, IntelligenceClient intelligence, JsonNode rawData) {
        this.datasets = datasets;
        this.ingestion = ingestion;
        this.intelligence = intelligence;
        this.rawData = rawData;
        return this;
    }

    /**
     * Searches this dataset with text using API defaults, including {@code topK=10}.
     *
     * @param query text query to embed and search
     * @return ranked search results
     */
    public SearchResponse search(String query) {
        return requireDatasets().search(requireId(), query);
    }

    /**
     * Searches this dataset.
     *
     * @param request search request; optional fields omitted from JSON use API defaults
     * @return ranked search results
     */
    public SearchResponse search(SearchRequest request) {
        return requireDatasets().search(requireId(), request);
    }


    /**
     * Lists retained source documents for this dataset using cursor pagination.
     *
     * @param limit optional maximum documents; null uses the API default
     * @param cursor optional cursor from a previous page's nextCursor
     * @param status optional document status filter
     * @return cursor-paginated document page
     */
    public DatasetDocumentPage listDocuments(Integer limit, String cursor, String status) {
        return requireDatasets().listDocuments(requireId(), limit, cursor, status);
    }

    /**
     * Lists retained source documents for this dataset using API pagination defaults.
     * @return cursor-paginated document page
     */
    public DatasetDocumentPage listDocuments() {
        return requireDatasets().listDocuments(requireId());
    }

    /**
     * Downloads retained original bytes for a source document in this dataset.
     * @param documentId document ID returned by {@link #listDocuments()}
     * @return raw document bytes
     */
    public byte[] downloadDocument(String documentId) {
        return requireDatasets().downloadDocument(requireId(), documentId);
    }

    /**
     * Inserts pre-computed vectors into this dataset.
     *
     * @param vectors vector records with IDs, values, and optional metadata
     * @return insert count response
     */
    public InsertResponse insert(List<VectorRecord> vectors) {
        return requireDatasets().insert(requireId(), vectors);
    }

    /**
     * Embeds and inserts one text record with a generated vector ID.
     *
     * @param text text to embed and store as metadata.text
     * @return insert count response
     */
    public InsertResponse addText(String text) {
        return requireDatasets().addText(requireId(), text);
    }

    /**
     * Embeds and inserts text records with generated vector IDs.
     *
     * @param texts texts to embed and store as metadata.text
     * @return insert count response
     */
    public InsertResponse addTexts(List<String> texts) {
        return requireDatasets().addTexts(requireId(), texts);
    }

    /**
     * Embeds and inserts text records with optional IDs and metadata.
     *
     * @param request texts plus optional IDs and per-text metadata; missing IDs are generated UUIDs
     * @return insert count response
     */
    public InsertResponse addTexts(AddTextsRequest request) {
        return requireDatasets().addTexts(requireId(), request);
    }

    /**
     * Embeds text with this dataset embedding configuration without inserting records.
     *
     * @param texts texts to embed
     * @return embeddings in the same order as {@code texts}
     */
    public List<List<Double>> embed(List<String> texts) {
        return requireDatasets().embed(requireId(), texts);
    }

    /** Deletes this dataset. */
    public void delete() {
        requireDatasets().delete(requireId());
    }

    /**
     * Runs a non-streaming intelligence query scoped to this dataset.
     *
     * @param query question or prompt text
     * @return answer and optional source/chunk metadata
     */
    public AskResponse ask(String query) {
        return ask(AskRequest.of(query));
    }

    /**
     * Runs a non-streaming intelligence query scoped to this dataset.
     *
     * @param request query request; datasetId is overwritten with this dataset ID
     * @return answer and optional source/chunk metadata
     */
    public AskResponse ask(AskRequest request) {
        Objects.requireNonNull(request, "request").datasetId(requireId());
        return requireIntelligence().ask(request);
    }

    /**
     * Runs an intelligence query scoped to this dataset as server-sent events.
     *
     * @param query question or prompt text
     * @return ordered event stream; close it if not fully consumed
     */
    public Stream<SseEvent> askStream(String query) {
        return askStream(AskRequest.of(query));
    }

    /**
     * Runs an intelligence query scoped to this dataset as server-sent events.
     *
     * @param request query request; datasetId is overwritten with this dataset ID
     * @return ordered event stream; close it if not fully consumed
     */
    public Stream<SseEvent> askStream(AskRequest request) {
        Objects.requireNonNull(request, "request").datasetId(requireId());
        return requireIntelligence().askStream(request);
    }

    /**
     * Creates a source from a raw request. Start it with {@link #ingestSource(Source)}.
     *
     * @param request raw source request
     * @return created source
     */
    public Source ingestSource(CreateSourceRequest request) {
        return requireIngestion().createSource(request);
    }

    /**
     * Creates a typed source. Start it with {@link #ingestSource(Source)}.
     *
     * @param input typed source input
     * @return created source
     */
    public Source ingestSource(IngestionSourceInput input) {
        return requireIngestion().createSource(input);
    }

    /**
     * Starts ingestion from an existing source into this dataset.
     * @param sourceId source ID
     * @return started ingestion job
     */
    public IngestionJob ingestSource(String sourceId) {
        return requireIngestion().startJob(sourceId, requireId());
    }

    /**
     * Starts ingestion from an existing source into this dataset.
     * @param source source resource
     * @return started ingestion job
     */
    public IngestionJob ingestSource(Source source) {
        return ingestSource(Objects.requireNonNull(source, "source").getId());
    }

    /**
     * Creates a file-upload source for this dataset and initializes an upload session.
     * Upload bytes to the returned targets, then call {@link IngestionClient#completeUpload(String, String, List)}.
     *
     * @param files file descriptors with name, size, and content type
     * @return upload session containing job ID and pre-signed upload targets
     */
    public UploadSession ingestFiles(List<FileUpload> files) {
        Source source = requireIngestion().createFileUploadSource(requireId());
        return requireIngestion().initializeUpload(source.getId(), files);
    }

    /**
     * Creates a named file-upload source for this dataset and initializes an upload session.
     *
     * @param sourceName source name; use this to avoid the generated file-upload source name
     * @param files file descriptors with name, size, and content type
     * @return upload session containing job ID and pre-signed upload targets
     */
    public UploadSession ingestFiles(String sourceName, List<FileUpload> files) {
        Source source = requireIngestion().createFileUploadSource(requireId(), sourceName);
        return requireIngestion().initializeUpload(source.getId(), files);
    }

    /**
     * Completes a direct file-upload session after bytes have been uploaded to every target.
     *
     * @param session upload session returned by {@link #ingestFiles(List)} or {@link #ingestFiles(String, List)}
     * @return ingestion job tracking processing of the uploaded files
     */
    public IngestionJob completeUpload(UploadSession session) {
        Objects.requireNonNull(session, "session");
        return requireIngestion().completeUpload(session.getSourceId(), session.getJobId(), session.getUploads().stream()
                .map(UploadSession.UploadTarget::getFileId)
                .collect(Collectors.toList()));
    }

    /**
     * Creates a file-upload source for this dataset using the default generated name.
     * @return created source
     */
    public Source createFileUploadSource() {
        return requireIngestion().createFileUploadSource(requireId());
    }

    /**
     * Creates a named file-upload source for this dataset.
     * @param name source name
     * @return created source
     */
    public Source createFileUploadSource(String name) {
        return requireIngestion().createFileUploadSource(requireId(), name);
    }

    /**
     * Creates a web source named from the URL.
     * @param url URL to crawl
     * @return created source
     */
    public Source createWebSource(String url) { return requireIngestion().createWeb(url); }

    /**
     * Creates a web source for this dataset context.
     * @param source typed web source input
     * @return created source
     */
    public Source createWebSource(WebSource source) { return requireIngestion().createWeb(source); }

    /**
     * Creates an S3 source named from the bucket.
     * @param bucket S3 bucket
     * @return created source
     */
    public Source createS3Source(String bucket) { return requireIngestion().createS3(bucket); }

    /**
     * Creates an S3 source.
     * @param source typed S3 source input
     * @return created source
     */
    public Source createS3Source(S3Source source) { return requireIngestion().createS3(source); }

    /**
     * Creates a Google Drive folder source named from the folder ID.
     * @param folderId Drive folder ID
     * @return created source
     */
    public Source createGoogleDriveSource(String folderId) { return requireIngestion().createGoogleDrive(folderId); }

    /**
     * Creates a Google Drive source.
     * @param source typed Google Drive source input
     * @return created source
     */
    public Source createGoogleDriveSource(GoogleDriveSource source) { return requireIngestion().createGoogleDrive(source); }

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
