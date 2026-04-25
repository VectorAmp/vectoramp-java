package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Direct file-upload session with job ID and upload targets. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadSession {
    private String jobId;
    private String sourceId;
    private List<UploadTarget> uploads;

    /**
     * @return jobId
     */
    public String getJobId() { return jobId; }
    /**
     * @return sourceId
     */
    public String getSourceId() { return sourceId; }
    /**
     * @return uploads
     */
    public List<UploadTarget> getUploads() { return uploads; }

    /**
     * Attaches the source ID used to initialize this upload.
     *
     * @param sourceId source ID
     * @return this session
     */
    public UploadSession attachSourceId(String sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    /** Pre-signed target for uploading one file. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UploadTarget {
        private String fileId;
        private String fileName;
        private String uploadUrl;
        /**
         * @return fileId
         */
        public String getFileId() { return fileId; }
        /**
         * @return fileName
         */
        public String getFileName() { return fileName; }
        /**
         * @return uploadUrl
         */
        public String getUploadUrl() { return uploadUrl; }
    }
}
