package com.vectoramp.models;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public static Builder builder(String name) { return new Builder(name); }
    public static Builder forFolder(String folderId) { return builder(GenericSource.defaultName(SourceType.GOOGLE_DRIVE, folderId)).folderId(folderId); }
    public static GoogleDriveSource folder(String folderId) { return forFolder(folderId).build(); }
    public static GoogleDriveSource folder(String name, String folderId) { return builder(name).folderId(folderId).build(); }

    public String getSourceType() { return SourceType.GOOGLE_DRIVE; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Map<String, Object> getConfig() { return config; }
    public Map<String, Object> getMetadata() { return metadata; }

    @Override public CreateSourceRequest toCreateSourceRequest() {
        return new CreateSourceRequest(SourceType.GOOGLE_DRIVE, name, description, config, metadata);
    }

    public static final class Builder {
        private final String name;
        private String description;
        private final Map<String, Object> config = new LinkedHashMap<>();
        private final Map<String, Object> metadata = new LinkedHashMap<>();

        private Builder(String name) { this.name = name; }
        public Builder folderId(String folderId) { GenericSource.put(config, "folder_id", folderId); return this; }
        public Builder fileIds(List<String> fileIds) { GenericSource.put(config, "file_ids", fileIds); return this; }
        public Builder sharedDriveId(String sharedDriveId) { GenericSource.put(config, "shared_drive_id", sharedDriveId); return this; }
        public Builder syncMode(String syncMode) { GenericSource.put(config, "sync_mode", syncMode); return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder config(String key, Object value) { GenericSource.put(config, key, value); return this; }
        public Builder metadata(String key, Object value) { GenericSource.put(metadata, key, value); return this; }
        public Builder metadata(Map<String, Object> values) { GenericSource.putAll(metadata, values); return this; }
        public GoogleDriveSource build() { return new GoogleDriveSource(this); }
    }
}
