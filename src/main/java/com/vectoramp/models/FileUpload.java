package com.vectoramp.models;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUpload {
    private final String name;
    private final long sizeBytes;
    private final String contentType;

    public FileUpload(String name, long sizeBytes, String contentType) {
        this.name = name;
        this.sizeBytes = sizeBytes;
        this.contentType = contentType;
    }

    public static FileUpload of(String name, long sizeBytes) {
        return new FileUpload(name, sizeBytes, "application/octet-stream");
    }

    public static FileUpload of(String name, long sizeBytes, String contentType) {
        return new FileUpload(name, sizeBytes, contentType);
    }

    public static FileUpload fromPath(Path path) throws IOException {
        String contentType = Files.probeContentType(path);
        if (contentType == null) contentType = "application/octet-stream";
        return new FileUpload(path.getFileName().toString(), Files.size(path), contentType);
    }

    public String getName() { return name; }
    public long getSizeBytes() { return sizeBytes; }
    public String getContentType() { return contentType; }
}
