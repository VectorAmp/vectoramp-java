package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response from an immediate schedule trigger. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TriggerScheduleResponse {
    private String jobId;

    /** @return id of the new ingestion job kicked off by the trigger */
    public String getJobId() { return jobId; }
}
