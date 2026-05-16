package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/** Partial update for a recurring ingestion schedule. Only non-null fields are sent. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateScheduleRequest {
    private final String cron;
    private final String timezone;
    private final String pipelineId;
    private final Boolean enabled;
    private final String name;
    private final Map<String, Object> metadata;

    private UpdateScheduleRequest(Builder b) {
        this.cron = b.cron;
        this.timezone = b.timezone;
        this.pipelineId = b.pipelineId;
        this.enabled = b.enabled;
        this.name = b.name;
        this.metadata = b.metadata;
    }

    /** Starts a builder. */
    public static Builder builder() { return new Builder(); }

    /** @return cron */
    public String getCron() { return cron; }
    /** @return timezone */
    public String getTimezone() { return timezone; }
    /** @return pipeline id */
    public String getPipelineId() { return pipelineId; }
    /** @return enabled flag */
    public Boolean getEnabled() { return enabled; }
    /** @return name */
    public String getName() { return name; }
    /** @return metadata */
    public Map<String, Object> getMetadata() { return metadata; }

    /** Builder for {@link UpdateScheduleRequest}. */
    public static final class Builder {
        private String cron;
        private String timezone;
        private String pipelineId;
        private Boolean enabled;
        private String name;
        private Map<String, Object> metadata;

        /** @param cron cron expression */
        public Builder cron(String cron) { this.cron = cron; return this; }
        /** @param timezone IANA timezone */
        public Builder timezone(String timezone) { this.timezone = timezone; return this; }
        /** @param pipelineId pipeline id */
        public Builder pipelineId(String pipelineId) { this.pipelineId = pipelineId; return this; }
        /** @param enabled enabled flag */
        public Builder enabled(Boolean enabled) { this.enabled = enabled; return this; }
        /** @param name name */
        public Builder name(String name) { this.name = name; return this; }
        /** @param metadata metadata blob */
        public Builder metadata(Map<String, Object> metadata) { this.metadata = metadata; return this; }
        /** @return the built request */
        public UpdateScheduleRequest build() { return new UpdateScheduleRequest(this); }
    }
}
