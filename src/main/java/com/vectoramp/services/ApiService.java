package com.vectoramp.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vectoramp.VectorAmpException;
import com.vectoramp.http.Transport;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

abstract class ApiService {
    protected static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    protected final Transport transport;

    ApiService(Transport transport) {
        this.transport = transport;
    }

    protected <T> T get(String path, Map<String, String> query, Class<T> responseType) {
        return parse(transport.execute(new Transport.Request("GET", path, query == null ? Collections.emptyMap() : query, Collections.emptyMap(), null)).getBody(), responseType);
    }

    protected <T> T post(String path, Object body, Class<T> responseType) {
        return parse(transport.execute(new Transport.Request("POST", path, Collections.emptyMap(), Collections.emptyMap(), json(body))).getBody(), responseType);
    }

    protected <T> T post(String path, Object body, TypeReference<T> responseType) {
        return parse(transport.execute(new Transport.Request("POST", path, Collections.emptyMap(), Collections.emptyMap(), json(body))).getBody(), responseType);
    }

    protected <T> T patch(String path, Object body, Class<T> responseType) {
        return parse(transport.execute(new Transport.Request("PATCH", path, Collections.emptyMap(), Collections.emptyMap(), json(body))).getBody(), responseType);
    }

    protected void delete(String path) {
        transport.execute(new Transport.Request("DELETE", path, Collections.emptyMap(), Collections.emptyMap(), null));
    }

    protected <T> T delete(String path, Object body, Class<T> responseType) {
        return parse(transport.execute(new Transport.Request("DELETE", path, Collections.emptyMap(), Collections.emptyMap(), json(body))).getBody(), responseType);
    }

    protected byte[] download(String path) {
        try (java.io.InputStream input = transport.stream(new Transport.Request("GET", path, Collections.emptyMap(), Collections.singletonMap("Accept", "*/*"), null))) {
            return input.readAllBytes();
        } catch (java.io.IOException e) {
            throw new VectorAmpException("Failed to read VectorAmp download response", e);
        }
    }

    protected String json(Object body) {
        try {
            return MAPPER.writeValueAsString(body == null ? Collections.emptyMap() : body);
        } catch (JsonProcessingException e) {
            throw new VectorAmpException("Failed to serialize request", e);
        }
    }

    protected <T> T parse(String body, Class<T> responseType) {
        try {
            if (responseType == Void.class) return null;
            return MAPPER.readValue(body == null || body.isEmpty() ? "{}" : body, responseType);
        } catch (JsonProcessingException e) {
            throw new VectorAmpException("Failed to parse VectorAmp API response", e);
        }
    }

    protected <T> T parse(String body, TypeReference<T> responseType) {
        try {
            return MAPPER.readValue(body == null || body.isEmpty() ? "{}" : body, responseType);
        } catch (JsonProcessingException e) {
            throw new VectorAmpException("Failed to parse VectorAmp API response", e);
        }
    }

    protected JsonNode parseTree(String body) {
        try {
            return MAPPER.readTree(body == null || body.isEmpty() ? "{}" : body);
        } catch (JsonProcessingException e) {
            throw new VectorAmpException("Failed to parse VectorAmp API response", e);
        }
    }

    protected static Map<String, String> pageQuery(Integer limit, Integer offset) {
        Map<String, String> query = new LinkedHashMap<>();
        if (limit != null) query.put("limit", String.valueOf(limit));
        if (offset != null) query.put("offset", String.valueOf(offset));
        return query;
    }

    protected static Map<String, String> documentListQuery(Integer limit, String cursor, String status) {
        Map<String, String> query = new LinkedHashMap<>();
        if (limit != null) query.put("limit", String.valueOf(limit));
        if (cursor != null && !cursor.isBlank()) query.put("cursor", cursor);
        if (status != null && !status.isBlank()) query.put("status", status);
        return query;
    }

    protected static String encodePath(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
    }
}
