package com.vectoramp.models;

import java.util.List;
import java.util.Map;

/**
 * GitHub ingestion source input.
 *
 * <p>Repository content is read through a read-only GitHub App installation, so a
 * GitHub source carries an {@code installation_id} and the {@code repositories}
 * full names ({@code owner/repo}) to ingest rather than an access token. Both are
 * required by the ingestion service.</p>
 *
 * <p>{@code ref_mode} selects which branches are ingested: {@code active} (the
 * server default) walks branches touched within {@code active_branch_days},
 * {@code default} ingests only the default branch, and {@code explicit} ingests
 * exactly the configured {@code refs}. The {@code include_pull_requests},
 * {@code include_review_threads}, and {@code include_direct_commits} toggles all
 * default to true server-side, so leave them unset to accept that default.</p>
 */
public final class GitHubSource implements IngestionSourceInput {
    private final GenericSource delegate;

    private GitHubSource(GenericSource delegate) { this.delegate = delegate; }

    /**
     * Starts a GitHub source builder.
     * @param name source name
     * @return builder configured for the GitHub source type
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }

    /**
     * Creates a GitHub source for a single repository, named from that repository.
     * @param installationId GitHub App installation id
     * @param repository repository full name, for example {@code VectorAmp/Docs}
     * @return source input
     */
    public static GitHubSource of(long installationId, String repository) {
        return of(GenericSource.defaultName(SourceType.GITHUB, repository), installationId, repository);
    }

    /**
     * Creates a named GitHub source for a single repository.
     * @param name source name
     * @param installationId GitHub App installation id
     * @param repository repository full name, for example {@code VectorAmp/Docs}
     * @return source input
     */
    public static GitHubSource of(String name, long installationId, String repository) {
        return builder(name).installationId(installationId).repositories(List.of(repository)).build();
    }

    /**
     * Creates a named GitHub source for several repositories.
     * @param name source name
     * @param installationId GitHub App installation id
     * @param repositories repository full names; at least one is required
     * @return source input
     */
    public static GitHubSource of(String name, long installationId, List<String> repositories) {
        return builder(name).installationId(installationId).repositories(repositories).build();
    }

    /**
     * @return SourceType.GITHUB
     */
    public String getSourceType() { return SourceType.GITHUB; }

    @Override public CreateSourceRequest toCreateSourceRequest() { return delegate.toCreateSourceRequest(); }

    /** Builder for GitHubSource inputs. */
    public static final class Builder {
        private final GenericSource.Builder delegate;

        private Builder(String name) {
            this.delegate = GenericSource.builder(SourceType.GITHUB, name);
        }

        /**
         * Sets the GitHub App installation id that grants access to the repositories.
         * @param installationId GitHub App installation id
         * @return this builder
         */
        public Builder installationId(long installationId) { delegate.config("installation_id", installationId); return this; }
        /**
         * Sets the repository full names to ingest, for example {@code VectorAmp/Docs}.
         * @param repositories repository full names
         * @return this builder
         */
        public Builder repositories(List<String> repositories) { delegate.config("repositories", repositories); return this; }
        /**
         * Sets the branch selection mode: {@code default}, {@code active}, or {@code explicit}.
         * API default ({@code active}) applies when omitted.
         * @param refMode ref mode
         * @return this builder
         */
        public Builder refMode(String refMode) { delegate.config("ref_mode", refMode); return this; }
        /**
         * Sets the branches ingested when {@code ref_mode} is {@code explicit}.
         * @param refs branch names
         * @return this builder
         */
        public Builder refs(List<String> refs) { delegate.config("refs", refs); return this; }
        /**
         * Excludes branches from {@code active} ref selection.
         * @param excludedRefs branch names to skip
         * @return this builder
         */
        public Builder excludedRefs(List<String> excludedRefs) { delegate.config("excluded_refs", excludedRefs); return this; }
        /**
         * Sets how recently a branch must have been touched to count as active (1-90 days).
         * API default (7) applies when omitted.
         * @param activeBranchDays activity window in days
         * @return this builder
         */
        public Builder activeBranchDays(int activeBranchDays) { delegate.config("active_branch_days", activeBranchDays); return this; }
        /**
         * Ingests pull requests. Defaults to true server-side.
         * @param includePullRequests false to skip pull requests
         * @return this builder
         */
        public Builder includePullRequests(boolean includePullRequests) { delegate.config("include_pull_requests", includePullRequests); return this; }
        /**
         * Ingests review threads on pull requests. Defaults to true server-side.
         * @param includeReviewThreads false to skip review threads
         * @return this builder
         */
        public Builder includeReviewThreads(boolean includeReviewThreads) { delegate.config("include_review_threads", includeReviewThreads); return this; }
        /**
         * Ingests commits pushed directly to a branch. Defaults to true server-side.
         * @param includeDirectCommits false to skip direct commits
         * @return this builder
         */
        public Builder includeDirectCommits(boolean includeDirectCommits) { delegate.config("include_direct_commits", includeDirectCommits); return this; }
        /**
         * Restricts ingested files to these glob patterns. API default ({@code **}{@code /*}) applies when omitted.
         * @param includeGlobs glob patterns to include
         * @return this builder
         */
        public Builder includeGlobs(List<String> includeGlobs) { delegate.config("include_globs", includeGlobs); return this; }
        /**
         * Skips files matching these glob patterns.
         * @param excludeGlobs glob patterns to exclude
         * @return this builder
         */
        public Builder excludeGlobs(List<String> excludeGlobs) { delegate.config("exclude_globs", excludeGlobs); return this; }
        /**
         * Skips files larger than this size (1-25,000,000 bytes). API default (1,000,000) applies when omitted.
         * @param maxFileSizeBytes maximum file size in bytes
         * @return this builder
         */
        public Builder maxFileSizeBytes(int maxFileSizeBytes) { delegate.config("max_file_size_bytes", maxFileSizeBytes); return this; }
        /**
         * Sets the sync mode; API default ({@code incremental}) applies when omitted.
         * @param syncMode sync mode, {@code full} or {@code incremental}
         * @return this builder
         */
        public Builder syncMode(String syncMode) { delegate.config("sync_mode", syncMode); return this; }
        /**
         * Sets an optional source description.
         * @param description description text
         * @return this builder
         */
        public Builder description(String description) { delegate.description(description); return this; }
        /**
         * Adds an optional config value; null values are omitted.
         * @param key config key
         * @param value config value
         * @return this builder
         */
        public Builder config(String key, Object value) { delegate.config(key, value); return this; }
        /**
         * Adds optional metadata; null values are omitted.
         * @param key metadata key
         * @param value metadata value
         * @return this builder
         */
        public Builder metadata(String key, Object value) { delegate.metadata(key, value); return this; }
        /**
         * Adds optional metadata values; null values are omitted.
         * @param values metadata values
         * @return this builder
         */
        public Builder metadata(Map<String, Object> values) { delegate.metadata(values); return this; }
        /**
         * @return immutable GitHub source input
         */
        public GitHubSource build() { return new GitHubSource(delegate.build()); }
    }
}
