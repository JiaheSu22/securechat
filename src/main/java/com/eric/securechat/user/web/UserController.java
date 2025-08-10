package com.eric.securechat.user.web;

import com.eric.securechat.user.application.UserService;
import com.eric.securechat.user.dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for handling user profile and cryptographic key operations.
 * Provides endpoints for retrieving user profiles and managing public keys for end-to-end encryption.
 * All endpoints require user authentication and are accessible under /api/users.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /**
     * Constructor for UserController.
     * 
     * @param userService The service for handling user operations
     */
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint to retrieve the current user's profile information.
     * 
     * @param userDetails Spring Security provided current logged-in user information
     * @return ResponseEntity containing the user's profile DTO
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        UserDto userDto = userService.getUserProfile(username);
        return ResponseEntity.ok(userDto);
    }

    /**
     * Endpoint to upload the current user's X25519 public key for end-to-end encryption.
     * 
     * @param userDetails Spring Security provided current logged-in user information
     * @param body Request body containing the X25519 public key
     * @return ResponseEntity with 200 OK status
     */
    @PutMapping("/me/x25519-key")
    public ResponseEntity<Void> uploadMyX25519Key(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, String> body) {
        userService.updateUserX25519Key(userDetails.getUsername(), body.get("x25519PublicKey"));
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint to upload the current user's Ed25519 public key for digital signatures.
     * 
     * @param userDetails Spring Security provided current logged-in user information
     * @param body Request body containing the Ed25519 public key
     * @return ResponseEntity with 200 OK status
     */
    @PutMapping("/me/ed25519-key")
    public ResponseEntity<Void> uploadMyEd25519Key(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, String> body) {
        userService.updateUserEd25519Key(userDetails.getUsername(), body.get("ed25519PublicKey"));
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint to retrieve a specific user's X25519 public key.
     * 
     * @param username The username of the user whose key is being retrieved
     * @return ResponseEntity containing the user's X25519 public key
     */
    @GetMapping("/{username}/x25519-key")
    public ResponseEntity<Map<String, String>> getUserX25519Key(@PathVariable String username) {
        return ResponseEntity.ok(Map.of("x25519PublicKey", userService.getUserX25519Key(username)));
    }

    /**
     * Endpoint to retrieve a specific user's Ed25519 public key.
     * 
     * @param username The username of the user whose key is being retrieved
     * @return ResponseEntity containing the user's Ed25519 public key
     */
    @GetMapping("/{username}/ed25519-key")
    public ResponseEntity<Map<String, String>> getUserEd25519Key(@PathVariable String username) {
        return ResponseEntity.ok(Map.of("ed25519PublicKey", userService.getUserEd25519Key(username)));
    }
}
