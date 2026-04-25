package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadSession {
    private String jobId;
    private List<UploadTarget> uploads;

    public String getJobId() { return jobId; }
    public List<UploadTarget> getUploads() { return uploads; }

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
