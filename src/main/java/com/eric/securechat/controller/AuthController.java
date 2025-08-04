package com.eric.securechat.controller;

import com.eric.securechat.dto.AuthResponse;
import com.eric.securechat.dto.LoginRequest;
import com.eric.securechat.dto.RegisterRequest;
import com.eric.securechat.service.AuthService;
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
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}