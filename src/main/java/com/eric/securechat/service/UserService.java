package com.eric.securechat.service;

import com.eric.securechat.dto.RegisterRequest;
import com.eric.securechat.dto.UserPublicKeyResponse;
import com.eric.securechat.model.User;
import com.eric.securechat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 注册一个新用户
     */
    @Transactional
    public User registerUser(RegisterRequest registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new IllegalStateException("Username already taken");
        }

        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setNickname(registerRequest.getNickname());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        // publicKey 在注册时为空

        return userRepository.save(newUser);
    }

    /**
     * 为当前认证的用户上传或更新其公钥
     */
    @Transactional
    public void updateUserPublicKey(String username, String publicKey) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
        user.setPublicKey(publicKey);
        userRepository.save(user); // JPA 会智能地执行 UPDATE
    }

    /**
     * 根据用户名获取其公钥和昵称
     * @return 返回一个包含公钥和昵称的DTO
     */
    @Transactional(readOnly = true)
    public UserPublicKeyResponse getUserPublicKey(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User '" + username + "' not found."));

        if (user.getPublicKey() == null || user.getPublicKey().isBlank()) {
            throw new IllegalStateException("User '" + username + "' has not uploaded a public key yet.");
        }

        // Service层负责构建并返回DTO，Controller只负责传递
        return new UserPublicKeyResponse(user.getUsername(), user.getNickname(), user.getPublicKey());
    }
}
