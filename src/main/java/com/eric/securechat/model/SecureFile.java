package com.eric.securechat.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing uploaded files in the secure chat system.
 * Tracks file metadata and ownership information.
 */
@Entity
@Table(name = "secure_files")
public class SecureFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String fileName;
    private String contentType;
    private long fileSize;

    /**
     * Unique storage path/filename on the server.
     */
    @Column(unique = true, nullable = false)
    private String storagePath;

    /**
     * User who uploaded the file.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    private Instant createdAt;

    /**
     * Default constructor.
     */
    public SecureFile() {}

    /**
     * Constructor for creating a new file record.
     * 
     * @param fileName Original filename
     * @param contentType MIME type of the file
     * @param fileSize Size of the file in bytes
     * @param storagePath Server storage path
     * @param uploader User who uploaded the file
     */
    public SecureFile(String fileName, String contentType, long fileSize, String storagePath, User uploader) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.storagePath = storagePath;
        this.uploader = uploader;
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public User getUploader() { return uploader; }
    public void setUploader(User uploader) { this.uploader = uploader; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
