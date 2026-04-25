package com.vectoramp.models;

/** Typed input that can be serialized as a create-source request. */
public interface IngestionSourceInput {
    /**
     * Converts this typed input into the API create-source payload.
     *
     * @return create-source request
     */
    CreateSourceRequest toCreateSourceRequest();
}
