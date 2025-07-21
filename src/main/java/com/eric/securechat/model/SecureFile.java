package com.eric.securechat.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "secure_files")
public class SecureFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String fileName;
    private String contentType;
    private long fileSize;

    @Column(unique = true, nullable = false)
    private String storagePath; // 服务器上存储的唯一路径/文件名

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    private Instant createdAt;

    // Constructors, Getters, and Setters
    public SecureFile() {}

    public SecureFile(String fileName, String contentType, long fileSize, String storagePath, User uploader) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.storagePath = storagePath;
        this.uploader = uploader;
        this.createdAt = Instant.now();
    }

    // ... generate all getters and setters ...
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
