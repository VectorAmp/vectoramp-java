package com.vectoramp.models;

import java.util.List;

/** Jira ingestion source input. include_comments defaults to true. */
public final class JiraSource implements IngestionSourceInput {
    private final GenericSource delegate;

    private JiraSource(GenericSource delegate) { this.delegate = delegate; }

    public static GenericSource.Builder builder(String name) {
        return GenericSource.builder(SourceType.JIRA, name).config("include_comments", true);
    }
    public static JiraSource of(String cloudId) { return of(GenericSource.defaultName(SourceType.JIRA, cloudId), cloudId); }
    public static JiraSource of(String name, String cloudId) {
        return new JiraSource(builder(name).config("cloud_id", cloudId).build());
    }
    public static JiraSource of(String name, String cloudId, List<String> projectKeys) {
        return new JiraSource(builder(name).config("cloud_id", cloudId).config("project_keys", projectKeys).build());
    }

    public String getSourceType() { return SourceType.JIRA; }
    @Override public CreateSourceRequest toCreateSourceRequest() { return delegate.toCreateSourceRequest(); }
}
