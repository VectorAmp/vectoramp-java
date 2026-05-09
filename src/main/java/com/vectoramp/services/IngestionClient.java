package com.vectoramp.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.vectoramp.http.Transport;
import com.vectoramp.models.*;

import java.util.*;

/** Client for ingestion sources, ingestion jobs, and direct file-upload sessions. */
public final class IngestionClient extends ApiService {
    /**
     * Creates an ingestion client backed by the supplied transport.
     *
     * @param transport HTTP transport to use for API requests
     */
    public IngestionClient(Transport transport) { super(transport); }

    /**
     * Lists ingestion sources using API defaults for pagination.
     *
     * @return page of sources with total, limit, and offset
     */
    public Page<Source> listSources() { return listSources(null, null); }

    /**
     * Lists ingestion sources.
     *
     * @param limit optional maximum number of sources; {@code null} uses the API default
     * @param offset optional starting offset; {@code null} uses the API default
     * @return page of sources with total, limit, and offset
     */
    public Page<Source> listSources(Integer limit, Integer offset) {
        JsonNode root = parseTree(transport.execute(new Transport.Request("GET", "/ingestion/sources", pageQuery(limit, offset), Collections.emptyMap(), null)).getBody());
        List<Source> sources = MAPPER.convertValue(root.path("sources"), new TypeReference<List<Source>>() {});
        return new Page<>(sources, root.path("total").asInt(), root.path("limit").asInt(), root.path("offset").asInt());
    }

    /**
     * Creates an ingestion source from a raw request.
     *
     * @param request source type, name, optional description, config, and metadata
     * @return created source resource
     */
    public Source createSource(CreateSourceRequest request) {
        return post("/ingestion/sources", request, Source.class);
    }

    /**
     * Creates an ingestion source from a typed source input.
     *
     * @param input typed source such as web, S3, Google Drive, or file upload
     * @return created source resource
     */
    public Source createSource(IngestionSourceInput input) {
        Objects.requireNonNull(input, "input");
        return createSource(input.toCreateSourceRequest());
    }

    /**
     * Creates a web source.
     * @param source typed web source input
     * @return created source
     */
    public Source createWeb(WebSource source) { return createSource(source); }

    /**
     * Creates a web source named from the URL.
     * @param url URL to crawl
     * @return created source
     */
    public Source createWeb(String url) { return createWeb(WebSource.of(url)); }

    /**
     * Creates a named web source.
     * @param name source name
     * @param url URL to crawl
     * @return created source
     */
    public Source createWeb(String name, String url) { return createWeb(WebSource.of(name, url)); }

    /**
     * Creates an S3 source.
     * @param source typed S3 source input
     * @return created source
     */
    public Source createS3(S3Source source) { return createSource(source); }

    /**
     * Creates an S3 source named from the bucket.
     * @param bucket S3 bucket name
     * @return created source
     */
    public Source createS3(String bucket) { return createS3(S3Source.of(bucket)); }

    /**
     * Creates a named S3 source.
     * @param name source name
     * @param bucket S3 bucket
     * @param prefix optional key prefix; {@code null} is omitted
     * @return created source
     */
    public Source createS3(String name, String bucket, String prefix) { return createS3(S3Source.of(name, bucket, prefix)); }

    /**
     * Creates a Google Drive source.
     * @param source typed Google Drive source input
     * @return created source
     */
    public Source createGoogleDrive(GoogleDriveSource source) { return createSource(source); }

    /**
     * Creates a Google Drive folder source named from the folder ID.
     * @param folderId Drive folder ID
     * @return created source
     */
    public Source createGoogleDrive(String folderId) { return createGoogleDrive(GoogleDriveSource.folder(folderId)); }

    /**
     * Creates a named Google Drive folder source.
     * @param name source name
     * @param folderId Drive folder ID
     * @return created source
     */
    public Source createGoogleDrive(String name, String folderId) { return createGoogleDrive(GoogleDriveSource.folder(name, folderId)); }

    /**
     * Creates a file-upload source.
     * @param source typed file-upload source input
     * @return created source
     */
    public Source createFileUpload(FileUploadSource source) { return createSource(source); }

    /**
     * Creates a file-upload source named from the dataset ID.
     *
     * @param datasetId dataset that uploaded files will ingest into; stored in source metadata
     * @return created source
     */
    public Source createFileUpload(String datasetId) { return createFileUpload(FileUploadSource.of(datasetId)); }

    /**
     * Creates a named file-upload source for a dataset.
     *
     * @param datasetId dataset that uploaded files will ingest into; stored in source metadata
     * @param name source name
     * @return created source
     */
    public Source createFileUpload(String datasetId, String name) { return createFileUpload(FileUploadSource.of(name, datasetId)); }

    /**
     * Alias for {@link #createFileUpload(String)}.
     * @param datasetId dataset ID
     * @return created source
     */
    public Source createFileUploadSource(String datasetId) { return createFileUpload(datasetId); }

    /**
     * Alias for {@link #createFileUpload(String, String)}.
     * @param datasetId dataset ID
     * @param name source name
     * @return created source
     */
    public Source createFileUploadSource(String datasetId, String name) {
        return createFileUpload(datasetId, name);
    }

    /**
     * Fetches a source by ID.
     * @param sourceId source ID
     * @return source resource
     */
    public Source getSource(String sourceId) {
        return get("/ingestion/sources/" + encodePath(sourceId), Collections.emptyMap(), Source.class);
    }

    /**
     * Starts ingestion with the API default pipeline.
     * @param sourceId source ID
     * @param datasetId target dataset ID
     * @return started job
     */
    public IngestionJob startJob(String sourceId, String datasetId) { return startJob(sourceId, datasetId, null); }

    /**
     * Starts ingestion for a source into a dataset.
     *
     * @param sourceId source ID
     * @param datasetId target dataset ID
     * @param pipelineId optional pipeline ID; {@code null} uses the API default
     * @return started ingestion job
     */
    public IngestionJob startJob(String sourceId, String datasetId, String pipelineId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("source_id", sourceId);
        body.put("dataset_id", datasetId);
        if (pipelineId != null) body.put("pipeline_id", pipelineId);
        return post("/ingestion/jobs", body, IngestionJob.class);
    }

    /**
     * Lists ingestion jobs.
     *
     * @param datasetId optional dataset filter; {@code null} lists all accessible jobs
     * @param limit optional maximum number of jobs; {@code null} uses the API default
     * @param offset optional starting offset; {@code null} uses the API default
     * @return page of jobs with total, limit, and offset
     */
    public Page<IngestionJob> listJobs(String datasetId, Integer limit, Integer offset) {
        Map<String, String> query = pageQuery(limit, offset);
        if (datasetId != null) query.put("dataset_id", datasetId);
        JsonNode root = parseTree(transport.execute(new Transport.Request("GET", "/ingestion/jobs", query, Collections.emptyMap(), null)).getBody());
        List<IngestionJob> jobs = MAPPER.convertValue(root.path("jobs"), new TypeReference<List<IngestionJob>>() {});
        return new Page<>(jobs, root.path("total").asInt(), root.path("limit").asInt(), root.path("offset").asInt());
    }

    /**
     * Fetches an ingestion job by ID.
     * @param jobId job ID
     * @return job resource
     */
    public IngestionJob getJob(String jobId) {
        return get("/ingestion/jobs/" + encodePath(jobId), Collections.emptyMap(), IngestionJob.class);
    }

    /**
     * Queues a fresh full-rerun job from an eligible failed or cancelled ingestion job.
     * @param jobId original job ID
     * @return newly queued retry job
     */
    public IngestionJob retryJob(String jobId) {
        return post("/ingestion/jobs/" + encodePath(jobId) + "/retry", Collections.emptyMap(), IngestionJob.class);
    }

    /**
     * Cancels an ingestion job.
     * @param jobId job ID
     */
    public void cancelJob(String jobId) {
        delete("/ingestion/jobs/" + encodePath(jobId) + "/cancel");
    }

    /**
     * Initializes direct file upload for an existing file-upload source.
     *
     * @param sourceId file-upload source ID
     * @param files file descriptors with name, size, and content type
     * @return upload session containing job ID and pre-signed upload targets
     */
    public UploadSession initializeUpload(String sourceId, List<FileUpload> files) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("files", files);
        return post("/ingestion/sources/" + encodePath(sourceId) + "/upload/init", body, UploadSession.class).attachSourceId(sourceId);
    }

    /**
     * Completes a direct file upload after bytes have been PUT to every upload target.
     *
     * @param sourceId file-upload source ID
     * @param jobId upload job ID from {@link UploadSession#getJobId()}
     * @param fileIds uploaded file IDs from {@link UploadSession.UploadTarget#getFileId()}
     * @return ingestion job tracking processing of the uploaded files
     */
    public IngestionJob completeUpload(String sourceId, String jobId, List<String> fileIds) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("job_id", jobId);
        body.put("file_ids", fileIds);
        return post("/ingestion/sources/" + encodePath(sourceId) + "/upload/complete", body, IngestionJob.class);
    }

    /**
     * Lists files associated with an ingestion job.
     * @param jobId job ID
     * @return raw API file listing
     */
    public JsonNode jobFiles(String jobId) {
        return get("/ingestion/jobs/" + encodePath(jobId) + "/files", Collections.emptyMap(), JsonNode.class);
    }

    /**
     * Fetches ingestion job statistics.
     * @param jobId job ID
     * @return raw API statistics payload
     */
    public JsonNode jobStatistics(String jobId) {
        return get("/ingestion/jobs/" + encodePath(jobId) + "/statistics", Collections.emptyMap(), JsonNode.class);
    }

    private static String encodePath(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
    }
}
