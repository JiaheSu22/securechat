// src/main/java/com/eric/securechat/controller/FileController.java
package com.eric.securechat.controller;

import com.eric.securechat.dto.FileUploadResponse;
import com.eric.securechat.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * REST Controller for handling file upload operations.
 * Provides endpoints for secure file uploads with authentication requirements.
 * All endpoints require user authentication and are accessible under /api/files.
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    /**
     * Constructor for FileController.
     * 
     * @param fileStorageService The service for handling file storage operations
     */
    @Autowired
    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * Endpoint to upload a file securely.
     * Only authenticated users can upload files.
     * 
     * @param file The multipart file to be uploaded
     * @return ResponseEntity containing file upload response with metadata
     */
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        String uniqueFileName = fileStorageService.storeFile(file);

        // Generate a relative URL path that matches the MvcConfig static resource mapping.
        // This ensures consistency between backend serving and frontend access.
        String fileUrl = "/files/" + uniqueFileName;

        FileUploadResponse response = new FileUploadResponse(
                uniqueFileName,
                fileUrl,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize()
        );

        return ResponseEntity.ok(response);
    }
}
