package com.eric.securechat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user is not found in the system.
 * Maps to HTTP 404 NOT_FOUND status code.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {

    /**
     * Constructor with error message.
     * 
     * @param message The error message
     */
    public UserNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor with error message and cause.
     * 
     * @param message The error message
     * @param cause The cause of the exception
     */
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
