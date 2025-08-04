package com.eric.securechat.service;

import com.eric.securechat.exception.UserNotFoundException;
import com.eric.securechat.model.User;
import com.eric.securechat.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Implementation of Spring Security's UserDetailsService.
 * Loads user details for authentication and authorization.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Constructor for UserDetailsServiceImpl.
     * 
     * @param userRepository Repository for user data operations
     */
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads user details by username for Spring Security authentication.
     * 
     * @param username The username to load user details for
     * @return UserDetails object for Spring Security
     * @throws UserNotFoundException if user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.emptyList()
        );
    }
}
