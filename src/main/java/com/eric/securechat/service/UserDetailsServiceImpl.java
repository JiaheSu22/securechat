package com.eric.securechat.service;

import com.eric.securechat.exception.UserNotFoundException; // 导入我们自己的异常
import com.eric.securechat.model.User;
import com.eric.securechat.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                // 在这里抛出我们自定义的异常
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

        // 返回 Spring Security 需要的 UserDetails 对象
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.emptyList() // 我们目前没有角色/权限，所以用一个空列表
        );
    }
}
