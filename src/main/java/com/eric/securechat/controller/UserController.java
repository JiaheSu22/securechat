package com.eric.securechat.controller;

import com.eric.securechat.dto.*;
import com.eric.securechat.service.AuthService;
import com.eric.securechat.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService; // 引入 AuthService 用于登录

    @Autowired
    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    // Endpoint: 上传/更新自己的公钥 (采纳您的 PUT /me/key)
    @PutMapping("/me/key")
    public ResponseEntity<Void> uploadMyPublicKey(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PublicKeyUploadRequest request) {

        String currentUsername = userDetails.getUsername();
        userService.updateUserPublicKey(currentUsername, request.publicKey());
        return ResponseEntity.ok().build();
    }

    // Endpoint: 获取指定用户的公钥和昵称 (采纳您的 GET /{username}/key)
    @GetMapping("/{username}/key")
    public ResponseEntity<UserPublicKeyResponse> getUserPublicKey(@PathVariable String username) {
        UserPublicKeyResponse response = userService.getUserPublicKey(username);
        return ResponseEntity.ok(response);
    }
}
