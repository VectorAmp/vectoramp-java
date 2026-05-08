package com.vectoramp.models;

/** Google Cloud Storage ingestion source input. */
public final class GCSSource implements IngestionSourceInput {
    private final GenericSource delegate;

    private GCSSource(GenericSource delegate) { this.delegate = delegate; }

    public static GenericSource.Builder builder(String name) { return GenericSource.builder(SourceType.GCS, name); }
    public static GCSSource of(String bucket) { return of(GenericSource.defaultName(SourceType.GCS, bucket), bucket); }
    public static GCSSource of(String name, String bucket) {
        return new GCSSource(builder(name).config("bucket", bucket).build());
    }

    public String getSourceType() { return SourceType.GCS; }
    @Override public CreateSourceRequest toCreateSourceRequest() { return delegate.toCreateSourceRequest(); }
}
