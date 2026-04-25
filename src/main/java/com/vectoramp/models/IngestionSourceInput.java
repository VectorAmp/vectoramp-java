package com.vectoramp.models;

/** Typed input that can be serialized as a create-source request. */
public interface IngestionSourceInput {
    CreateSourceRequest toCreateSourceRequest();
}
