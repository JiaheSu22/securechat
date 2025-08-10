package com.eric.securechat.file.dto;

/**
 * Response DTO for file upload operations.
 * Contains file metadata and access information.
 */
public record FileUploadResponse(
        String fileName,
        String fileUrl,
        String originalFilename,
        String fileType,
        long size
) {}