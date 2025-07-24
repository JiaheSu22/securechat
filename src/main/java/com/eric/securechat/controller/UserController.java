package com.eric.securechat.controller;

import com.eric.securechat.dto.*;
import com.eric.securechat.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        UserDto userDto = userService.getUserProfile(username);
        return ResponseEntity.ok(userDto);
    }


    @PutMapping("/me/x25519-key")
    public ResponseEntity<Void> uploadMyX25519Key(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, String> body) {
        userService.updateUserX25519Key(userDetails.getUsername(), body.get("x25519PublicKey"));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/me/ed25519-key")
    public ResponseEntity<Void> uploadMyEd25519Key(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, String> body) {
        userService.updateUserEd25519Key(userDetails.getUsername(), body.get("ed25519PublicKey"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/x25519-key")
    public ResponseEntity<Map<String, String>> getUserX25519Key(@PathVariable String username) {
        return ResponseEntity.ok(Map.of("x25519PublicKey", userService.getUserX25519Key(username)));
    }

    @GetMapping("/{username}/ed25519-key")
    public ResponseEntity<Map<String, String>> getUserEd25519Key(@PathVariable String username) {
        return ResponseEntity.ok(Map.of("ed25519PublicKey", userService.getUserEd25519Key(username)));
    }
}
