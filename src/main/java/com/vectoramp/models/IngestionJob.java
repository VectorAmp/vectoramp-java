package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;

/** Ingestion job status and metrics returned by the ingestion API. */
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

    /**
     * @return jobId
     */
    public String getJobId() { return jobId; }
    /**
     * @return status
     */
    public String getStatus() { return status; }
    /**
     * @return message
     */
    public String getMessage() { return message; }
    /**
     * @return documentsProcessed
     */
    public Integer getDocumentsProcessed() { return documentsProcessed; }
    /**
     * @return vectorsInserted
     */
    public Integer getVectorsInserted() { return vectorsInserted; }
    /**
     * @return processingTimeSeconds
     */
    public Double getProcessingTimeSeconds() { return processingTimeSeconds; }
    /**
     * @return pipelineResult
     */
    public JsonNode getPipelineResult() { return pipelineResult; }
    /**
     * @return errorDetails
     */
    public JsonNode getErrorDetails() { return errorDetails; }
    /**
     * @return startedAt
     */
    public OffsetDateTime getStartedAt() { return startedAt; }
    /**
     * @return completedAt
     */
    public OffsetDateTime getCompletedAt() { return completedAt; }
    /**
     * @return progressPercentage
     */
    public Double getProgressPercentage() { return progressPercentage; }
    /**
     * @return currentStep
     */
    public String getCurrentStep() { return currentStep; }
}
