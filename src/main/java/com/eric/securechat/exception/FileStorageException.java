package com.eric.securechat.exception;

/**
 * Exception thrown when file storage operations fail.
 * Used for file upload, download, and storage-related errors.
 */
public class FileStorageException extends RuntimeException {
    
    /**
     * Constructor with error message.
     * 
     * @param message The error message
     */
    public FileStorageException(String message) {
        super(message);
    }

    /**
     * Constructor with error message and cause.
     * 
     * @param message The error message
     * @param cause The cause of the exception
     */
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
