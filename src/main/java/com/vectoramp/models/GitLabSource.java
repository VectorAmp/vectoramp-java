package com.vectoramp.models;

import java.util.List;
import java.util.Map;

/**
 * GitLab ingestion source input, for gitlab.com or a self-managed instance.
 *
 * <p>{@code auth_mode} defaults to {@code oauth} server-side, which resolves fresh
 * credentials from a stored connection ({@code connection_id}) at ingest time. Use
 * {@code token} auth with a personal or group access token instead. Set
 * {@code gitlab_url} to target a self-managed instance; it defaults to
 * {@code https://gitlab.com}.</p>
 *
 * <p>Scope is set by {@code groups} (group paths) and/or {@code projects} (full
 * paths such as {@code group/project}); the ingestion service requires at least one
 * of the two. The {@code include_merge_requests}, {@code include_review_threads},
 * and {@code include_direct_commits} toggles all default to true server-side, so
 * leave them unset to accept that default.</p>
 */
public final class GitLabSource implements IngestionSourceInput {
    private final GenericSource delegate;

    private GitLabSource(GenericSource delegate) { this.delegate = delegate; }

    /**
     * Starts a GitLab source builder.
     * @param name source name
     * @return builder configured for the GitLab source type
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }

    /**
     * Creates a GitLab source for a single project, named from that project path.
     * @param project project full path, for example {@code platform/ingestion}
     * @return source input
     */
    public static GitLabSource ofProject(String project) {
        return ofProject(GenericSource.defaultName(SourceType.GITLAB, project), project);
    }

    /**
     * Creates a named GitLab source for a single project.
     * @param name source name
     * @param project project full path, for example {@code platform/ingestion}
     * @return source input
     */
    public static GitLabSource ofProject(String name, String project) {
        return builder(name).projects(List.of(project)).build();
    }

    /**
     * Creates a GitLab source for every project in a group, named from that group path.
     * @param group group path, for example {@code platform}
     * @return source input
     */
    public static GitLabSource ofGroup(String group) {
        return ofGroup(GenericSource.defaultName(SourceType.GITLAB, group), group);
    }

    /**
     * Creates a named GitLab source for every project in a group.
     * @param name source name
     * @param group group path, for example {@code platform}
     * @return source input
     */
    public static GitLabSource ofGroup(String name, String group) {
        return builder(name).groups(List.of(group)).build();
    }

    /**
     * @return SourceType.GITLAB
     */
    public String getSourceType() { return SourceType.GITLAB; }

    @Override public CreateSourceRequest toCreateSourceRequest() { return delegate.toCreateSourceRequest(); }

    /** Builder for GitLabSource inputs. */
    public static final class Builder {
        private final GenericSource.Builder delegate;

        private Builder(String name) {
            this.delegate = GenericSource.builder(SourceType.GITLAB, name);
        }

        /**
         * Configures token auth with a personal or group access token (sets {@code auth_mode=token}).
         * @param accessToken GitLab personal or group access token
         * @return this builder
         */
        public Builder accessToken(String accessToken) {
            delegate.config("auth_mode", "token").config("access_token", accessToken);
            return this;
        }
        /**
         * References a stored OAuth connection by id (sets config {@code connection_id}).
         * @param connectionId connection id from the connections API
         * @return this builder
         */
        public Builder connection(String connectionId) { delegate.config("connection_id", connectionId); return this; }
        /**
         * Sets the auth mode explicitly; API default ({@code oauth}) applies when omitted.
         * @param authMode {@code oauth} or {@code token}
         * @return this builder
         */
        public Builder authMode(String authMode) { delegate.config("auth_mode", authMode); return this; }
        /**
         * Targets a self-managed GitLab instance; defaults to {@code https://gitlab.com}.
         * @param gitlabUrl instance base URL
         * @return this builder
         */
        public Builder gitlabUrl(String gitlabUrl) { delegate.config("gitlab_url", gitlabUrl); return this; }
        /**
         * Ingests every accessible project in these groups.
         * @param groups group paths
         * @return this builder
         */
        public Builder groups(List<String> groups) { delegate.config("groups", groups); return this; }
        /**
         * Ingests these projects by full path, for example {@code group/project}.
         * @param projects project full paths
         * @return this builder
         */
        public Builder projects(List<String> projects) { delegate.config("projects", projects); return this; }
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
         * Ingests merge requests. Defaults to true server-side.
         * @param includeMergeRequests false to skip merge requests
         * @return this builder
         */
        public Builder includeMergeRequests(boolean includeMergeRequests) { delegate.config("include_merge_requests", includeMergeRequests); return this; }
        /**
         * Ingests review discussions on merge requests. Defaults to true server-side.
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
         * @return immutable GitLab source input
         */
        public GitLabSource build() { return new GitLabSource(delegate.build()); }
    }
}
