package com.vectoramp.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.vectoramp.http.Transport;
import com.vectoramp.models.CreateScheduleRequest;
import com.vectoramp.models.Page;
import com.vectoramp.models.Schedule;
import com.vectoramp.models.TriggerScheduleResponse;
import com.vectoramp.models.UpdateScheduleRequest;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Client for recurring ingestion schedules.
 *
 * <p>A schedule pairs a source with a target dataset and a cron expression. The
 * ingestion scheduler daemon polls for due schedules and creates jobs as they
 * fire.</p>
 */
public final class SchedulesClient extends ApiService {
    /**
     * Creates a schedules client backed by the supplied transport.
     *
     * @param transport HTTP transport to use for API requests
     */
    public SchedulesClient(Transport transport) { super(transport); }

    /**
     * Lists schedules using API defaults for pagination.
     *
     * @return page of schedules with total, limit, and offset
     */
    public Page<Schedule> list() { return list(null, null); }

    /**
     * Lists schedules.
     *
     * @param limit optional maximum number of schedules
     * @param offset optional starting offset
     * @return page of schedules with total, limit, and offset
     */
    public Page<Schedule> list(Integer limit, Integer offset) {
        JsonNode root = parseTree(transport.execute(new Transport.Request(
                "GET", "/ingestion/schedules", pageQuery(limit, offset),
                Collections.emptyMap(), null)).getBody());
        List<Schedule> schedules = MAPPER.convertValue(root.path("schedules"), new TypeReference<List<Schedule>>() {});
        return new Page<>(schedules, root.path("total").asInt(), root.path("limit").asInt(), root.path("offset").asInt());
    }

    /**
     * Fetches a schedule by id.
     *
     * @param scheduleId schedule id
     * @return schedule resource
     */
    public Schedule get(String scheduleId) {
        Objects.requireNonNull(scheduleId, "scheduleId");
        return parse(transport.execute(new Transport.Request(
                "GET", "/ingestion/schedules/" + scheduleId, Collections.emptyMap(),
                Collections.emptyMap(), null)).getBody(), Schedule.class);
    }

    /**
     * Creates a recurring schedule.
     *
     * @param request source id, dataset id, cron, and optional timezone/pipeline/enabled/name/metadata
     * @return created schedule resource
     */
    public Schedule create(CreateScheduleRequest request) {
        return post("/ingestion/schedules", request, Schedule.class);
    }

    /**
     * Updates a schedule. Only non-null fields in the request body are sent.
     *
     * @param scheduleId schedule id
     * @param request fields to update
     * @return updated schedule resource
     */
    public Schedule update(String scheduleId, UpdateScheduleRequest request) {
        Objects.requireNonNull(scheduleId, "scheduleId");
        return patch("/ingestion/schedules/" + scheduleId, request, Schedule.class);
    }

    /**
     * Deletes a schedule.
     *
     * @param scheduleId schedule id
     */
    public void delete(String scheduleId) {
        Objects.requireNonNull(scheduleId, "scheduleId");
        delete("/ingestion/schedules/" + scheduleId);
    }

    /**
     * Triggers an immediate run for a schedule, outside its cron cadence.
     *
     * @param scheduleId schedule id
     * @return job id of the newly created ingestion job
     */
    public TriggerScheduleResponse trigger(String scheduleId) {
        Objects.requireNonNull(scheduleId, "scheduleId");
        return post("/ingestion/schedules/" + scheduleId + "/trigger", null, TriggerScheduleResponse.class);
    }
}
