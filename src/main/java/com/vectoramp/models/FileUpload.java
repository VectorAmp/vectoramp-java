package com.vectoramp.models;

public class FileUpload {
    private final String name;
    private final long sizeBytes;
    private final String contentType;

    public FileUpload(String name, long sizeBytes, String contentType) {
        this.name = name;
        this.sizeBytes = sizeBytes;
        this.contentType = contentType;
    }

    public String getName() { return name; }
    public long getSizeBytes() { return sizeBytes; }
    public String getContentType() { return contentType; }
}
