package com.vectoramp.models;

/** Constants for supported ingestion source types. */
public final class SourceType {
    /** S3 bucket/prefix source type. */
    public static final String S3 = "s3";
    /** Web crawl source type. */
    public static final String WEB = "web";
    /** Google Cloud Storage source type. */
    public static final String GCS = "gcs";
    /** Google Drive source type. */
    public static final String GOOGLE_DRIVE = "gdrive";
    /** Jira source type. */
    public static final String JIRA = "jira";
    /** Direct file-upload source type. */
    public static final String FILE_UPLOAD = "file_upload";

    private SourceType() {}
}
