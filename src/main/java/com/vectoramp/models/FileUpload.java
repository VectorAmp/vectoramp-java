package com.vectoramp.models;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** File descriptor used to initialize a direct upload session. */
public class FileUpload {
    private final String name;
    private final long sizeBytes;
    private final String contentType;

    /**
     * Creates a file descriptor.
     *
     * @param name file name sent to the API
     * @param sizeBytes file size in bytes
     * @param contentType MIME type; use {@code application/octet-stream} when unknown
     */
    public FileUpload(String name, long sizeBytes, String contentType) {
        this.name = name;
        this.sizeBytes = sizeBytes;
        this.contentType = contentType;
    }

    /**
     * Creates a descriptor with {@code application/octet-stream}.
     * @param name file name
     * @param sizeBytes size in bytes
     * @return file descriptor
     */
    public static FileUpload of(String name, long sizeBytes) {
        return new FileUpload(name, sizeBytes, "application/octet-stream");
    }

    /**
     * Creates a descriptor with an explicit content type.
     * @param name file name
     * @param sizeBytes size in bytes
     * @param contentType MIME type
     * @return file descriptor
     */
    public static FileUpload of(String name, long sizeBytes, String contentType) {
        return new FileUpload(name, sizeBytes, contentType);
    }

    /**
     * Creates a descriptor from a local path, probing MIME type and size.
     * @param path local file path
     * @return file descriptor
     * @throws IOException if the file cannot be inspected
     */
    public static FileUpload fromPath(Path path) throws IOException {
        String contentType = Files.probeContentType(path);
        if (contentType == null) contentType = "application/octet-stream";
        return new FileUpload(path.getFileName().toString(), Files.size(path), contentType);
    }

    /**
     * @return name
     */
    public String getName() { return name; }
    /**
     * @return sizeBytes
     */
    public long getSizeBytes() { return sizeBytes; }
    /**
     * @return contentType
     */
    public String getContentType() { return contentType; }
}
