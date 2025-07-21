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
     * 【已修改】注册一个新用户
     */
    @Transactional
    public User registerUser(RegisterRequest registerRequest) {
        // 【防御性校验 1】在 Service 层手动校验，确保逻辑健壮
        validateRegistrationRequest(registerRequest);

        // 【防御性校验 2】检查用户名是否存在
        if (userRepository.findByUsername(registerRequest.username()).isPresent()) {
            // 这个异常现在会被 GlobalExceptionHandler 正确处理，并返回 409
            throw new IllegalStateException("Username already taken");
        }

        User newUser = new User();
        newUser.setUsername(registerRequest.username());
        newUser.setNickname(registerRequest.nickname());
        newUser.setPassword(passwordEncoder.encode(registerRequest.password()));

        return userRepository.save(newUser);
    }

    /**
     * 【新增方法】用于手动校验注册请求的辅助方法
     */
    private void validateRegistrationRequest(RegisterRequest request) {
        // 你可以在这里添加任何你认为重要的、不能单靠 @Valid 的校验
        if (request.password() == null || request.password().length() < 6) {
            // 这个异常会被 GlobalExceptionHandler 正确处理，并返回 400
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
        if (request.username() == null || request.username().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
        // ... 可以添加更多校验规则
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
