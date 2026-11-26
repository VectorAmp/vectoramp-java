package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Dataset-local security scope selection for scheduled ingestion writes. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SecurityScopeSelection {
    private final String externalKey;
    private final String scopeType;

    /** Creates a custom scope selection. @param externalKey active dataset-local external key */
    public SecurityScopeSelection(String externalKey) { this(externalKey, null); }

    /**
     * Creates a scope selection.
     * @param externalKey active dataset-local external key
     * @param scopeType optional scope type; the server defaults to custom
     */
    public SecurityScopeSelection(String externalKey, String scopeType) {
        this.externalKey = externalKey;
        this.scopeType = scopeType;
    }

    /** @return dataset-local external key */
    public String getExternalKey() { return externalKey; }
    /** @return scope type, or null for the server default */
    public String getScopeType() { return scopeType; }
}
