package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/** Body for creating a recurring ingestion schedule. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateScheduleRequest {
    private final String sourceId;
    private final String datasetId;
    private final String cron;
    private final String timezone;
    private final String pipelineId;
    private final Boolean enabled;
    private final String name;
    private final Map<String, Object> metadata;

    private CreateScheduleRequest(Builder b) {
        this.sourceId = b.sourceId;
        this.datasetId = b.datasetId;
        this.cron = b.cron;
        this.timezone = b.timezone;
        this.pipelineId = b.pipelineId;
        this.enabled = b.enabled;
        this.name = b.name;
        this.metadata = b.metadata;
    }

    /** Starts a builder. */
    public static Builder builder() { return new Builder(); }

    /** @return source id */
    public String getSourceId() { return sourceId; }
    /** @return dataset id */
    public String getDatasetId() { return datasetId; }
    /** @return cron expression */
    public String getCron() { return cron; }
    /** @return timezone (nullable) */
    public String getTimezone() { return timezone; }
    /** @return pipeline id (nullable) */
    public String getPipelineId() { return pipelineId; }
    /** @return enabled flag (nullable) */
    public Boolean getEnabled() { return enabled; }
    /** @return name (nullable) */
    public String getName() { return name; }
    /** @return metadata (nullable) */
    public Map<String, Object> getMetadata() { return metadata; }

    /** Builder for {@link CreateScheduleRequest}. */
    public static final class Builder {
        private String sourceId;
        private String datasetId;
        private String cron;
        private String timezone;
        private String pipelineId;
        private Boolean enabled;
        private String name;
        private Map<String, Object> metadata;

        /** @param sourceId source id; required */
        public Builder sourceId(String sourceId) { this.sourceId = sourceId; return this; }
        /** @param datasetId dataset id; required */
        public Builder datasetId(String datasetId) { this.datasetId = datasetId; return this; }
        /** @param cron 5-field cron expression; required */
        public Builder cron(String cron) { this.cron = cron; return this; }
        /** @param timezone IANA timezone; defaults to UTC server-side */
        public Builder timezone(String timezone) { this.timezone = timezone; return this; }
        /** @param pipelineId pipeline id; defaults to ingestion default */
        public Builder pipelineId(String pipelineId) { this.pipelineId = pipelineId; return this; }
        /** @param enabled enable flag; defaults to true server-side */
        public Builder enabled(Boolean enabled) { this.enabled = enabled; return this; }
        /** @param name human-readable name */
        public Builder name(String name) { this.name = name; return this; }
        /** @param metadata metadata blob */
        public Builder metadata(Map<String, Object> metadata) { this.metadata = metadata; return this; }
        /** @return the built request */
        public CreateScheduleRequest build() { return new CreateScheduleRequest(this); }
    }
}
