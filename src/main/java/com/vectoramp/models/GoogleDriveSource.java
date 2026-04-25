package com.vectoramp.models;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Google Drive ingestion source input. Optional builder fields omitted from JSON use API defaults. */
public final class GoogleDriveSource implements IngestionSourceInput {
    private final String name;
    private final String description;
    private final Map<String, Object> config;
    private final Map<String, Object> metadata;

    private GoogleDriveSource(Builder builder) {
        this.name = GenericSource.requireText(builder.name, "name");
        this.description = builder.description;
        this.config = Map.copyOf(builder.config);
        this.metadata = Map.copyOf(builder.metadata);
    }

    /**
     * Starts a Google Drive source builder with an explicit source name.
     * @param name source name
     * @return source builder
     */
    public static Builder builder(String name) { return new Builder(name); }
    /**
     * Starts a folder source builder named from the folder ID.
     * @param folderId Drive folder ID
     * @return source builder
     */
    public static Builder forFolder(String folderId) { return builder(GenericSource.defaultName(SourceType.GOOGLE_DRIVE, folderId)).folderId(folderId); }
    /**
     * Creates a folder source named from the folder ID.
     * @param folderId Drive folder ID
     * @return source input
     */
    public static GoogleDriveSource folder(String folderId) { return forFolder(folderId).build(); }
    /**
     * Creates a named folder source.
     * @param name source name
     * @param folderId Drive folder ID
     * @return source input
     */
    public static GoogleDriveSource folder(String name, String folderId) { return builder(name).folderId(folderId).build(); }

    /**
     * @return SourceType.GOOGLE_DRIVE
     */
    public String getSourceType() { return SourceType.GOOGLE_DRIVE; }
    /**
     * @return name
     */
    public String getName() { return name; }
    /**
     * @return description
     */
    public String getDescription() { return description; }
    /**
     * @return config
     */
    public Map<String, Object> getConfig() { return config; }
    /**
     * @return metadata
     */
    public Map<String, Object> getMetadata() { return metadata; }

    @Override public CreateSourceRequest toCreateSourceRequest() {
        return new CreateSourceRequest(SourceType.GOOGLE_DRIVE, name, description, config, metadata);
    }

    /** Builder for GoogleDriveSource inputs. */
    public static final class Builder {
        private final String name;
        private String description;
        private final Map<String, Object> config = new LinkedHashMap<>();
        private final Map<String, Object> metadata = new LinkedHashMap<>();

        private Builder(String name) { this.name = name; }
        /**
     * Sets Google Drive folder ID.
     * @param folderId Drive folder ID
     * @return this builder
     */
        public Builder folderId(String folderId) { GenericSource.put(config, "folder_id", folderId); return this; }
        /**
     * Sets specific Google Drive file IDs.
     * @param fileIds Drive file IDs
     * @return this builder
     */
        public Builder fileIds(List<String> fileIds) { GenericSource.put(config, "file_ids", fileIds); return this; }
        /**
     * Sets optional shared-drive ID; null is omitted.
     * @param sharedDriveId shared-drive ID
     * @return this builder
     */
        public Builder sharedDriveId(String sharedDriveId) { GenericSource.put(config, "shared_drive_id", sharedDriveId); return this; }
        /**
     * Sets optional sync mode; API default applies when omitted.
     * @param syncMode sync mode
     * @return this builder
     */
        public Builder syncMode(String syncMode) { GenericSource.put(config, "sync_mode", syncMode); return this; }
        /**
     * Sets optional source description.
     * @param description description text
     * @return this builder
     */
        public Builder description(String description) { this.description = description; return this; }
        /**
     * Adds an optional config value; null values are omitted.
     * @param key config key
     * @param value config value
     * @return this builder
     */
        public Builder config(String key, Object value) { GenericSource.put(config, key, value); return this; }
        /**
     * Adds optional metadata; null values are omitted.
     * @param key metadata key
     * @param value metadata value
     * @return this builder
     */
        public Builder metadata(String key, Object value) { GenericSource.put(metadata, key, value); return this; }
        /**
     * Adds optional metadata values; null values are omitted.
     * @param values metadata values
     * @return this builder
     */
        public Builder metadata(Map<String, Object> values) { GenericSource.putAll(metadata, values); return this; }
        /**
     * @return immutable source input
     */
        public GoogleDriveSource build() { return new GoogleDriveSource(this); }
    }
}
