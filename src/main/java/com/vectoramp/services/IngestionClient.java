package com.vectoramp.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.vectoramp.http.Transport;
import com.vectoramp.models.*;

import java.util.*;

public final class IngestionClient extends ApiService {
    public IngestionClient(Transport transport) { super(transport); }

    public Page<Source> listSources() { return listSources(null, null); }

    public Page<Source> listSources(Integer limit, Integer offset) {
        JsonNode root = parseTree(transport.execute(new Transport.Request("GET", "/ingestion/sources", pageQuery(limit, offset), Collections.emptyMap(), null)).getBody());
        List<Source> sources = MAPPER.convertValue(root.path("sources"), new TypeReference<List<Source>>() {});
        return new Page<>(sources, root.path("total").asInt(), root.path("limit").asInt(), root.path("offset").asInt());
    }

    public Source createSource(CreateSourceRequest request) {
        return post("/ingestion/sources", request, Source.class);
    }

    public Source createSource(IngestionSourceInput input) {
        Objects.requireNonNull(input, "input");
        return createSource(input.toCreateSourceRequest());
    }

    public Source createWeb(WebSource source) { return createSource(source); }

    public Source createWeb(String url) { return createWeb(WebSource.of(url)); }

    public Source createWeb(String name, String url) { return createWeb(WebSource.of(name, url)); }

    public Source createS3(S3Source source) { return createSource(source); }

    public Source createS3(String bucket) { return createS3(S3Source.of(bucket)); }

    public Source createS3(String name, String bucket, String prefix) { return createS3(S3Source.of(name, bucket, prefix)); }

    public Source createGoogleDrive(GoogleDriveSource source) { return createSource(source); }

    public Source createGoogleDrive(String folderId) { return createGoogleDrive(GoogleDriveSource.folder(folderId)); }

    public Source createGoogleDrive(String name, String folderId) { return createGoogleDrive(GoogleDriveSource.folder(name, folderId)); }

    public Source createFileUpload(FileUploadSource source) { return createSource(source); }

    public Source createFileUpload(String datasetId) { return createFileUpload(FileUploadSource.of(datasetId)); }

    public Source createFileUpload(String datasetId, String name) { return createFileUpload(FileUploadSource.of(name, datasetId)); }

    public Source createFileUploadSource(String datasetId) { return createFileUpload(datasetId); }

    public Source createFileUploadSource(String datasetId, String name) {
        return createFileUpload(datasetId, name);
    }

    public Source getSource(String sourceId) {
        return get("/ingestion/sources/" + encodePath(sourceId), Collections.emptyMap(), Source.class);
    }

    public IngestionJob startJob(String sourceId, String datasetId) { return startJob(sourceId, datasetId, null); }

    public IngestionJob startJob(String sourceId, String datasetId, String pipelineId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("source_id", sourceId);
        body.put("dataset_id", datasetId);
        if (pipelineId != null) body.put("pipeline_id", pipelineId);
        return post("/ingestion/jobs", body, IngestionJob.class);
    }

    public Page<IngestionJob> listJobs(String datasetId, Integer limit, Integer offset) {
        Map<String, String> query = pageQuery(limit, offset);
        if (datasetId != null) query.put("dataset_id", datasetId);
        JsonNode root = parseTree(transport.execute(new Transport.Request("GET", "/ingestion/jobs", query, Collections.emptyMap(), null)).getBody());
        List<IngestionJob> jobs = MAPPER.convertValue(root.path("jobs"), new TypeReference<List<IngestionJob>>() {});
        return new Page<>(jobs, root.path("total").asInt(), root.path("limit").asInt(), root.path("offset").asInt());
    }

    public IngestionJob getJob(String jobId) {
        return get("/ingestion/jobs/" + encodePath(jobId), Collections.emptyMap(), IngestionJob.class);
    }

    public void cancelJob(String jobId) {
        delete("/ingestion/jobs/" + encodePath(jobId) + "/cancel");
    }

    public UploadSession initializeUpload(String sourceId, List<FileUpload> files) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("files", files);
        return post("/ingestion/sources/" + encodePath(sourceId) + "/upload/init", body, UploadSession.class).attachSourceId(sourceId);
    }

    public IngestionJob completeUpload(String sourceId, String jobId, List<String> fileIds) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("job_id", jobId);
        body.put("file_ids", fileIds);
        return post("/ingestion/sources/" + encodePath(sourceId) + "/upload/complete", body, IngestionJob.class);
    }

    public JsonNode jobFiles(String jobId) {
        return get("/ingestion/jobs/" + encodePath(jobId) + "/files", Collections.emptyMap(), JsonNode.class);
    }

    public JsonNode jobStatistics(String jobId) {
        return get("/ingestion/jobs/" + encodePath(jobId) + "/statistics", Collections.emptyMap(), JsonNode.class);
    }

    private static String encodePath(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
    }
}
