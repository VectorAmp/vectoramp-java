package com.vectoramp.services;

import com.vectoramp.http.Transport;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Client for organization-scoped secret helpers exposed by the public API. */
public final class OrgSecretsClient extends ApiService {
    /** Default OpenAI embedding secret reference used by VectorAmp. */
    public static final String DEFAULT_OPENAI_SECRET_REF = "emb:openai:api_key";

    /**
     * Creates an organization secrets client backed by the supplied transport.
     * @param transport HTTP transport to use for API requests
     */
    public OrgSecretsClient(Transport transport) { super(transport); }

    /** Store or update an organization secret by name. */
    public void put(String name, String value) {
        String trimmedName = Objects.requireNonNull(name, "name").trim();
        String trimmedValue = Objects.requireNonNull(value, "value").trim();
        if (trimmedName.isEmpty()) throw new IllegalArgumentException("name must not be blank");
        if (trimmedValue.isEmpty()) throw new IllegalArgumentException("value must not be blank");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("value", trimmedValue);
        put("/org-secrets/" + URLEncoder.encode(trimmedName, StandardCharsets.UTF_8), body, Void.class);
    }

    /** Check whether an organization secret exists. */
    public boolean has(String name) {
        String trimmedName = Objects.requireNonNull(name, "name").trim();
        if (trimmedName.isEmpty()) throw new IllegalArgumentException("name must not be blank");
        get("/org-secrets/" + URLEncoder.encode(trimmedName, StandardCharsets.UTF_8), java.util.Collections.emptyMap(), Void.class);
        return true;
    }

    /**
     * Stores or updates the organization OpenAI API key at the default secret reference.
     *
     * @param apiKey OpenAI API key to store server-side
     */
    public void putOpenAiApiKey(String apiKey) {
        putOpenAiApiKey(apiKey, DEFAULT_OPENAI_SECRET_REF, false, null);
    }

    /**
     * Stores or updates the organization OpenAI API key.
     *
     * @param apiKey OpenAI API key to store server-side
     * @param secretRef secret reference to write; defaults to {@link #DEFAULT_OPENAI_SECRET_REF} when blank
     * @param validate true to ask the API to validate the key before storing
     * @param model optional OpenAI embedding model used during validation
     */
    public void putOpenAiApiKey(String apiKey, String secretRef, boolean validate, String model) {
        String trimmedKey = Objects.requireNonNull(apiKey, "apiKey").trim();
        if (trimmedKey.isEmpty()) throw new IllegalArgumentException("apiKey must not be blank");
        String ref = secretRef != null && !secretRef.isBlank() ? secretRef.trim() : DEFAULT_OPENAI_SECRET_REF;
        put(ref, trimmedKey);
    }

    /** Alias for {@link #putOpenAiApiKey(String, String, boolean, String)}. */
    public void updateOpenAiApiKey(String apiKey, String secretRef, boolean validate, String model) {
        putOpenAiApiKey(apiKey, secretRef, validate, model);
    }

    /**
     * Checks whether the default organization OpenAI API key exists. Returns true for HTTP 204;
     * the underlying transport raises a VectorAmpApiException for 404 or other API errors.
     *
     * @return true when the API reports the key is present
     */
    public boolean hasOpenAiApiKey() {
        return has(DEFAULT_OPENAI_SECRET_REF);
    }
}
