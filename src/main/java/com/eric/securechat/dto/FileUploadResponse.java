package com.eric.securechat.dto;

import java.util.UUID;

public record FileUploadResponse(
        String fileName,          // 服务端生成的唯一文件名
        String fileUrl,           // 文件的完整访问 URL
        String originalFilename,  // 文件的原始名称
        String fileType,          // 文件的 MIME 类型
        long size                 // 文件大小
) {}