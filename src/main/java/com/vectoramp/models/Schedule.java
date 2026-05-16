package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.OffsetDateTime;
import java.util.Map;

/** Recurring ingestion schedule returned by the API. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Schedule {
    private String id;
    private String organizationId;
    private String sourceId;
    private String datasetId;
    private String pipelineId;
    private String cron;
    private String timezone;
    private boolean enabled;
    private String name;
    private OffsetDateTime nextRunAt;
    private OffsetDateTime lastRunAt;
    private Map<String, Object> metadata;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    /** @return schedule id */
    public String getId() { return id; }
    /** @return organization id */
    public String getOrganizationId() { return organizationId; }
    /** @return source id */
    public String getSourceId() { return sourceId; }
    /** @return dataset id */
    public String getDatasetId() { return datasetId; }
    /** @return pipeline id, when set */
    public String getPipelineId() { return pipelineId; }
    /** @return cron expression */
    public String getCron() { return cron; }
    /** @return IANA timezone */
    public String getTimezone() { return timezone; }
    /** @return true when the scheduler will fire this schedule */
    public boolean isEnabled() { return enabled; }
    /** @return human-readable name, when set */
    public String getName() { return name; }
    /** @return next planned run time */
    public OffsetDateTime getNextRunAt() { return nextRunAt; }
    /** @return last completed run time */
    public OffsetDateTime getLastRunAt() { return lastRunAt; }
    /** @return metadata blob attached to the schedule */
    public Map<String, Object> getMetadata() { return metadata; }
    /** @return created-at timestamp */
    public OffsetDateTime getCreatedAt() { return createdAt; }
    /** @return updated-at timestamp */
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
