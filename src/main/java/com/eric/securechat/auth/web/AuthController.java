package com.eric.securechat.auth.web;

import com.eric.securechat.auth.dto.AuthResponse;
import com.eric.securechat.auth.dto.LoginRequest;
import com.eric.securechat.auth.dto.RegisterRequest;
import com.eric.securechat.auth.application.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for handling user authentication operations.
 * Provides endpoints for user registration and login functionality.
 * All endpoints are accessible under the /api/auth base path.
 */
@Tag(name = "Authentication", description = "User registration, login and authentication operations")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Constructor for AuthController.
     * 
     * @param authService The authentication service for handling user registration and login
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint for user registration.
     * Accepts a registration request with username, password, and optional nickname.
     * Returns an authentication response containing a JWT token upon successful registration.
     * 
     * @param request The registration request containing user credentials
     * @return ResponseEntity containing the authentication response with JWT token
     */
    @Operation(summary = "User Registration", description = "Create a new user account and return JWT token upon success")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "409", description = "Username already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Endpoint for user login.
     * Accepts login credentials and returns an authentication response with JWT token.
     * 
     * @param request The login request containing username and password
     * @return ResponseEntity containing the authentication response with JWT token
     */
    @Operation(summary = "User Login", description = "Authenticate user credentials and return JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid username or password"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}