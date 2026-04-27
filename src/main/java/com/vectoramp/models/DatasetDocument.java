package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.OffsetDateTime;
import java.util.Map;

/** Source/original document metadata retained for a dataset. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetDocument {
    private String id;
    private String datasetId;
    private String sourceId;
    private String sourceType;
    private String externalId;
    private String fileName;
    private String mimeType;
    private Long sizeBytes;
    private String contentHash;
    private String status;
    private Integer version;
    private Integer chunkCount;
    private Integer embeddingsCount;
    private Boolean downloadAvailable;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Map<String, Object> metadata;

    /** @return document identifier used for download */
    public String getId() { return id; }
    /** @return dataset id */
    public String getDatasetId() { return datasetId; }
    /** @return ingestion source id, when present */
    public String getSourceId() { return sourceId; }
    /** @return source type such as s3, gdrive, or file_upload */
    public String getSourceType() { return sourceType; }
    /** @return external source identifier, when present */
    public String getExternalId() { return externalId; }
    /** @return original file name or title */
    public String getFileName() { return fileName; }
    /** @return MIME type, when known */
    public String getMimeType() { return mimeType; }
    /** @return original byte size, when known */
    public Long getSizeBytes() { return sizeBytes; }
    /** @return content hash, when recorded */
    public String getContentHash() { return contentHash; }
    /** @return document processing status */
    public String getStatus() { return status; }
    /** @return current document version */
    public Integer getVersion() { return version; }
    /** @return indexed chunk count */
    public Integer getChunkCount() { return chunkCount; }
    /** @return inserted embedding count */
    public Integer getEmbeddingsCount() { return embeddingsCount; }
    /** @return true when retained original bytes can be downloaded */
    public Boolean getDownloadAvailable() { return downloadAvailable; }
    /** @return created timestamp */
    public OffsetDateTime getCreatedAt() { return createdAt; }
    /** @return updated timestamp */
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    /** @return additional document metadata */
    public Map<String, Object> getMetadata() { return metadata; }
}
