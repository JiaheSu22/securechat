// src/main/java/com/eric/securechat/service/FileStorageService.java
package com.eric.securechat.service;

import com.eric.securechat.config.FileStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            // 如果目录不存在，则创建它
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /**
     * 存储文件到磁盘
     * @param file 上传的文件
     * @return 生成的唯一文件名 (用于构建URL)
     */
    public String storeFile(MultipartFile file) {
        // 1. 清理并获取原始文件名
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // 2. 检查文件名是否有效
            if (originalFilename.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + originalFilename);
            }

            // 3. 生成一个唯一的文件名来避免冲突 (e.g., uuid-original.ext)
            String fileExtension = "";
            try {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            } catch (Exception e) {
                fileExtension = "";
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;


            // 4. 构建目标路径
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);

            // 5. 将文件复制到目标位置 (如果已存在则替换)
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return uniqueFileName;

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFilename + ". Please try again!", ex);
        }
    }
}
