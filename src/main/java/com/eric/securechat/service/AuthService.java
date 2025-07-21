package com.eric.securechat.service;

import com.eric.securechat.dto.AuthResponse;
import com.eric.securechat.dto.LoginRequest; // 确保这个 DTO 存在
import com.eric.securechat.dto.RegisterRequest;
import com.eric.securechat.model.User;
import com.eric.securechat.repository.UserRepository;
import com.eric.securechat.security.JwtService; // 确保 JwtService 的路径正确
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    // 您的构造函数是正确的，保持不变
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * 注册一个新用户，并立即为其生成JWT令牌（注册并自动登录）。
     * 此方法现在与 AuthController 的期望完全匹配。
     *
     * @param request 包含用户名、密码和可选昵称的注册请求。
     * @return AuthResponse 包含新生成的JWT令牌。
     */
    public AuthResponse register(RegisterRequest request) {
        // 1. 检查用户名是否已存在 (您的逻辑)
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalStateException("Error: Username is already taken!");
        }

        // 2. 创建新用户并加密密码 (您的逻辑)
        User newUser = new User();
        newUser.setUsername(request.username());
        newUser.setPassword(passwordEncoder.encode(request.password()));

        // 3. 处理昵称 (您的逻辑)
        String nickname = request.nickname();
        if (nickname == null || nickname.trim().isEmpty()) {
            newUser.setNickname(request.username());
        } else {
            newUser.setNickname(nickname.trim());
        }

        // 4. 保存用户到数据库
        userRepository.save(newUser);

        // 5. 【核心修改】为新注册的用户生成JWT令牌
        // 注意：这里需要确认您的 JwtService.generateToken 方法接受的是 User 对象还是 String
        // 根据您之前的代码，它接受 User 对象，所以我们用 newUser
        String jwtToken = jwtService.generateToken(newUser);

        logger.info("User '{}' registered successfully.", newUser.getUsername());

        // 6. 【核心修改】返回包含令牌的 AuthResponse
        return new AuthResponse(jwtToken);
    }

    /**
     * 用户登录认证，成功后生成JWT令牌。
     * 此方法已经与 AuthController 匹配，无需修改。
     *
     * @param request 包含用户名和密码的登录请求。
     * @return AuthResponse 包含生成的JWT令牌。
     */
    public AuthResponse login(LoginRequest request) {
        try {
            // 1. 尝试认证，这是可能抛出异常的地方
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );

            // --- 只有认证成功，代码才会走到这里 ---

            // 2. 获取用户信息
            User user = userRepository.findByUsername(request.username())
                    .orElseThrow(() -> {
                        // 这是一个严重的内部不一致错误：认证通过了但数据库找不到用户
                        logger.error("CRITICAL: User '{}' passed authentication but was not found in database.", request.username());
                        return new IllegalStateException("User not found after authentication. Data inconsistency.");
                    });

            // 3. 生成JWT
            String jwtToken = jwtService.generateToken(user);
            logger.info("User '{}' logged in successfully.", user.getUsername());
            return new AuthResponse(jwtToken);

        } catch (AuthenticationException e) {
            // --- 认证失败，代码会跳到这里 ---

            // 4. 记录业务警告日志（不带堆栈）
            logger.warn("Failed login attempt for username '{}'. Reason: {}",
                    request.username(), e.getMessage());

            // 5. 抛出一个对API友好的异常
            // Spring Security会自动将这个异常映射为 401 Unauthorized 状态码
            throw new BadCredentialsException("Invalid username or password");
        }
    }
}
