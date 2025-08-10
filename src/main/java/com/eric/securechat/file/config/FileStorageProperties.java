package com.eric.securechat.file.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for file storage settings.
 * Maps file upload directory configuration from application properties.
 */
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {
    
    /**
     * Directory path for storing uploaded files.
     */
    private String uploadDir;

    /**
     * Gets the upload directory path.
     * 
     * @return The upload directory path
     */
    public String getUploadDir() {
        return uploadDir;
    }

    /**
     * Sets the upload directory path.
     * 
     * @param uploadDir The upload directory path to set
     */
    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
}
