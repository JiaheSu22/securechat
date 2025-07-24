package com.eric.securechat.service;

import com.eric.securechat.dto.RegisterRequest;
import com.eric.securechat.dto.UserDto;
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
     * 【新增方法】根据用户名获取用户公开信息
     * @param username 用户名
     * @return 包含用户ID、用户名和昵称的DTO
     */
    @Transactional(readOnly = true)
    public UserDto getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User '" + username + "' not found."));
        return new UserDto(user.getId(), user.getUsername(), user.getNickname());
    }

    @Transactional
    public void updateUserX25519Key(String username, String x25519PublicKey) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
        user.setX25519PublicKey(x25519PublicKey);
        userRepository.save(user);
    }

    @Transactional
    public void updateUserEd25519Key(String username, String ed25519PublicKey) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
        user.setEd25519PublicKey(ed25519PublicKey);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public String getUserX25519Key(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User '" + username + "' not found."));
        if (user.getX25519PublicKey() == null || user.getX25519PublicKey().isBlank()) {
            throw new IllegalStateException("User '" + username + "' has not uploaded a X25519 public key yet.");
        }
        return user.getX25519PublicKey();
    }

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
