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

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    @Autowired
    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()") // 只有登录的用户才能上传文件
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        // 1. 调用服务层存储文件，并获取唯一文件名
        String uniqueFileName = fileStorageService.storeFile(file);

        // 2. 构建文件的完整可访问 URL
        String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/files/") // 注意：这里的路径要和下面的静态资源配置匹配
                .path(uniqueFileName)
                .toUriString();

        // 3. 构建响应体
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
