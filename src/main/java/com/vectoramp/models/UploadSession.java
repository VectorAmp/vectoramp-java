package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadSession {
    private String jobId;
    private String sourceId;
    private List<UploadTarget> uploads;

    public String getJobId() { return jobId; }
    public String getSourceId() { return sourceId; }
    public List<UploadTarget> getUploads() { return uploads; }

    public UploadSession attachSourceId(String sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UploadTarget {
        private String fileId;
        private String fileName;
        private String uploadUrl;
        public String getFileId() { return fileId; }
        public String getFileName() { return fileName; }
        public String getUploadUrl() { return uploadUrl; }
    }
}
