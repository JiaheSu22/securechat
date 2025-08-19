package com.eric.securechat.common.exception;

import com.eric.securechat.user.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import io.swagger.v3.oas.annotations.Hidden;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application.
 * Provides centralized exception handling with standardized JSON responses.
 * Handles authentication, validation, and business logic exceptions.
 */
@ControllerAdvice
@Hidden
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles Spring Security authentication exceptions.
     * Returns 401 Unauthorized with generic error message for security.
     * 
     * @param ex The authentication exception
     * @param request The current request
     * @return ResponseEntity with 401 status and error message
     */
    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        Map<String, String> customMessage = Map.of("message", "Invalid username or password.");
        return buildErrorResponse(ex, customMessage, HttpStatus.UNAUTHORIZED, request);
    }

    /**
     * Handles user not found exceptions.
     * 
     * @param ex The user not found exception
     * @param request The current request
     * @return ResponseEntity with 404 status
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    /**
     * Handles illegal argument exceptions for business logic validation.
     * 
     * @param ex The illegal argument exception
     * @param request The current request
     * @return ResponseEntity with 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handles illegal state exceptions for object state conflicts.
     * 
     * @param ex The illegal state exception
     * @param request The current request
     * @return ResponseEntity with 409 status
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.CONFLICT, request);
    }

    /**
     * Handles JSR-380 validation failures with detailed field error messages.
     * 
     * @param ex The validation exception
     * @param headers HTTP headers
     * @param status HTTP status
     * @param request The current request
     * @return ResponseEntity with validation error details
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", status.value());
        body.put("error", "Validation Failed");

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> String.format("'%s': %s", fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.joining("; "));

        body.put("message", message);
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, headers, status);
    }

    /**
     * Handles all uncaught exceptions as a final fallback.
     * Logs detailed error information and returns generic error message.
     * 
     * @param ex The uncaught exception
     * @param request The current request
     * @return ResponseEntity with 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(Exception ex, WebRequest request) {
        log.error("An unexpected error occurred processing request: {}", request.getDescription(false), ex);

        Exception responseException = new Exception("An unexpected internal server error occurred. Please contact support.");
        return buildErrorResponse(responseException, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * Builds error response with default exception message.
     * 
     * @param ex The exception
     * @param status The HTTP status
     * @param request The current request
     * @return ResponseEntity with error details
     */
    private ResponseEntity<Object> buildErrorResponse(Exception ex, HttpStatus status, WebRequest request) {
        return buildErrorResponse(ex, null, status, request);
    }

    /**
     * Builds error response with custom message.
     * 
     * @param ex The exception
     * @param customMessages Custom error messages
     * @param status The HTTP status
     * @param request The current request
     * @return ResponseEntity with error details
     */
    private ResponseEntity<Object> buildErrorResponse(Exception ex, Map<String, String> customMessages, HttpStatus status, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", (customMessages != null && customMessages.containsKey("message")) ? customMessages.get("message") : ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, status);
    }
}
