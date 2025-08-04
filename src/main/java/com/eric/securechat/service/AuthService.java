package com.eric.securechat.service;

import com.eric.securechat.dto.AuthResponse;
import com.eric.securechat.dto.LoginRequest;
import com.eric.securechat.dto.RegisterRequest;
import com.eric.securechat.model.User;
import com.eric.securechat.repository.UserRepository;
import com.eric.securechat.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for handling user authentication operations including registration and login.
 * Provides secure user registration with automatic JWT token generation and login functionality.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    /**
     * Constructor for AuthService.
     * 
     * @param userRepository Repository for user data operations
     * @param passwordEncoder Service for password encryption
     * @param jwtService Service for JWT token operations
     * @param authenticationManager Spring Security authentication manager
     */
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Registers a new user and generates a JWT token for immediate authentication.
     * Validates username uniqueness and handles optional nickname assignment.
     * 
     * @param request The registration request containing user credentials
     * @return AuthResponse containing the generated JWT token
     * @throws IllegalStateException if username is already taken
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalStateException("Error: Username is already taken!");
        }

        User newUser = new User();
        newUser.setUsername(request.username());
        newUser.setPassword(passwordEncoder.encode(request.password()));

        String nickname = request.nickname();
        if (nickname == null || nickname.trim().isEmpty()) {
            newUser.setNickname(request.username());
        } else {
            newUser.setNickname(nickname.trim());
        }

        userRepository.save(newUser);

        String jwtToken = jwtService.generateToken(newUser);

        logger.info("User '{}' registered successfully.", newUser.getUsername());

        return new AuthResponse(jwtToken);
    }

    /**
     * Authenticates a user and generates a JWT token upon successful login.
     * Validates credentials using Spring Security's authentication manager.
     * 
     * @param request The login request containing username and password
     * @return AuthResponse containing the generated JWT token
     * @throws BadCredentialsException if authentication fails
     */
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );

            User user = userRepository.findByUsername(request.username())
                    .orElseThrow(() -> {
                        logger.error("CRITICAL: User '{}' passed authentication but was not found in database.", request.username());
                        return new IllegalStateException("User not found after authentication. Data inconsistency.");
                    });

            String jwtToken = jwtService.generateToken(user);
            logger.info("User '{}' logged in successfully.", user.getUsername());
            return new AuthResponse(jwtToken);

        } catch (AuthenticationException e) {
            logger.warn("Failed login attempt for username '{}'. Reason: {}",
                    request.username(), e.getMessage());

            throw new BadCredentialsException("Invalid username or password");
        }
    }
}
