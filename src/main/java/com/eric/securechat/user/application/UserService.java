package com.eric.securechat.user.application;

import com.eric.securechat.auth.dto.RegisterRequest;
import com.eric.securechat.user.dto.UserDto;
import com.eric.securechat.user.domain.User;
import com.eric.securechat.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling user-related operations including registration, profile management,
 * and cryptographic key management for end-to-end encryption.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor for UserService.
     * 
     * @param userRepository Repository for user data operations
     * @param passwordEncoder Service for password encryption
     */
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user with validation and password encryption.
     * Performs defensive validation to ensure data integrity.
     * 
     * @param registerRequest The registration request containing user credentials
     * @return The created user entity
     * @throws IllegalStateException if username is already taken
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public User registerUser(RegisterRequest registerRequest) {
        validateRegistrationRequest(registerRequest);

        if (userRepository.findByUsername(registerRequest.username()).isPresent()) {
            throw new IllegalStateException("Username already taken");
        }

        User newUser = new User();
        newUser.setUsername(registerRequest.username());
        newUser.setNickname(registerRequest.nickname());
        newUser.setPassword(passwordEncoder.encode(registerRequest.password()));

        return userRepository.save(newUser);
    }

    /**
     * Validates registration request data for completeness and security.
     * Performs additional validation beyond standard @Valid annotations.
     * 
     * @param request The registration request to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateRegistrationRequest(RegisterRequest request) {
        if (request.password() == null || request.password().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
        if (request.username() == null || request.username().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
    }

    /**
     * Retrieves a user's public profile information by username.
     * 
     * @param username The username of the user to retrieve
     * @return UserDto containing user's public information
     * @throws IllegalArgumentException if user is not found
     */
    @Transactional(readOnly = true)
    public UserDto getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User '" + username + "' not found."));
        return new UserDto(user.getId(), user.getUsername(), user.getNickname());
    }

    /**
     * Updates a user's X25519 public key for end-to-end encryption.
     * 
     * @param username The username of the user to update
     * @param x25519PublicKey The X25519 public key to set
     * @throws IllegalStateException if user is not found
     */
    @Transactional
    public void updateUserX25519Key(String username, String x25519PublicKey) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
        user.setX25519PublicKey(x25519PublicKey);
        userRepository.save(user);
    }

    /**
     * Updates a user's Ed25519 public key for digital signatures.
     * 
     * @param username The username of the user to update
     * @param ed25519PublicKey The Ed25519 public key to set
     * @throws IllegalStateException if user is not found
     */
    @Transactional
    public void updateUserEd25519Key(String username, String ed25519PublicKey) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
        user.setEd25519PublicKey(ed25519PublicKey);
        userRepository.save(user);
    }

    /**
     * Retrieves a user's X25519 public key for encryption operations.
     * 
     * @param username The username of the user whose key to retrieve
     * @return The user's X25519 public key
     * @throws IllegalArgumentException if user is not found
     * @throws IllegalStateException if user has not uploaded a key
     */
    @Transactional(readOnly = true)
    public String getUserX25519Key(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User '" + username + "' not found."));
        if (user.getX25519PublicKey() == null || user.getX25519PublicKey().isBlank()) {
            throw new IllegalStateException("User '" + username + "' has not uploaded a X25519 public key yet.");
        }
        return user.getX25519PublicKey();
    }

    /**
     * Retrieves a user's Ed25519 public key for signature verification.
     * 
     * @param username The username of the user whose key to retrieve
     * @return The user's Ed25519 public key
     * @throws IllegalArgumentException if user is not found
     * @throws IllegalStateException if user has not uploaded a key
     */
    @Transactional(readOnly = true)
    public String getUserEd25519Key(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User '" + username + "' not found."));
        if (user.getEd25519PublicKey() == null || user.getEd25519PublicKey().isBlank()) {
            throw new IllegalStateException("User '" + username + "' has not uploaded an Ed25519 public key yet.");
        }
        return user.getEd25519PublicKey();
    }
}
