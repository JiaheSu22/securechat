package com.eric.securechat.user.web;

import com.eric.securechat.user.application.UserService;
import com.eric.securechat.user.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User Management", description = "User profile and encryption key management")
@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "Bearer Authentication")
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
    @Operation(summary = "Get current user profile", description = "Retrieve the profile information of the currently logged-in user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
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
    @Operation(summary = "Upload X25519 public key", description = "Upload X25519 public key for end-to-end encryption")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Key uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
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
    @Operation(summary = "Upload Ed25519 public key", description = "Upload Ed25519 public key for digital signatures")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Key uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
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
    @Operation(summary = "Get user X25519 public key", description = "Retrieve the X25519 public key of a specific user for encrypted communication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Key retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found or key not set"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @GetMapping("/{username}/x25519-key")
    public ResponseEntity<Map<String, String>> getUserX25519Key(
            @Parameter(description = "Username") @PathVariable String username) {
        return ResponseEntity.ok(Map.of("x25519PublicKey", userService.getUserX25519Key(username)));
    }

    /**
     * Endpoint to retrieve a specific user's Ed25519 public key.
     * 
     * @param username The username of the user whose key is being retrieved
     * @return ResponseEntity containing the user's Ed25519 public key
     */
    @Operation(summary = "Get user Ed25519 public key", description = "Retrieve the Ed25519 public key of a specific user for digital signature verification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Key retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found or key not set"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @GetMapping("/{username}/ed25519-key")
    public ResponseEntity<Map<String, String>> getUserEd25519Key(
            @Parameter(description = "Username") @PathVariable String username) {
        return ResponseEntity.ok(Map.of("ed25519PublicKey", userService.getUserEd25519Key(username)));
    }
}
