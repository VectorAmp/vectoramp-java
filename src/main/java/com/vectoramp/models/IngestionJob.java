package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IngestionJob {
    private String jobId;
    private String status;
    private String message;
    private Integer documentsProcessed;
    private Integer vectorsInserted;
    private Double processingTimeSeconds;
    private JsonNode pipelineResult;
    private JsonNode errorDetails;
    private OffsetDateTime startedAt;
    private OffsetDateTime completedAt;
    private Double progressPercentage;
    private String currentStep;

    public String getJobId() { return jobId; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public Integer getDocumentsProcessed() { return documentsProcessed; }
    public Integer getVectorsInserted() { return vectorsInserted; }
    public Double getProcessingTimeSeconds() { return processingTimeSeconds; }
    public JsonNode getPipelineResult() { return pipelineResult; }
    public JsonNode getErrorDetails() { return errorDetails; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public Double getProgressPercentage() { return progressPercentage; }
    public String getCurrentStep() { return currentStep; }
}
