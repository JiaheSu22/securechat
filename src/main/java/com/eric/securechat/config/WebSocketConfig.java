package com.eric.securechat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import com.eric.securechat.security.JwtHandshakeInterceptor;

/**
 * WebSocket configuration for real-time messaging.
 * Configures STOMP message broker, endpoints, and JWT authentication for WebSocket connections.
 * Enables secure real-time communication between clients and server.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    /**
     * Constructor for WebSocketConfig.
     * 
     * @param jwtHandshakeInterceptor Interceptor for JWT authentication in WebSocket handshakes
     */
    public WebSocketConfig(JwtHandshakeInterceptor jwtHandshakeInterceptor) {
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
    }

    /**
     * Configures the message broker for WebSocket communication.
     * Sets up application destination prefixes, message broker destinations, and user destination prefixes.
     * 
     * @param registry Registry for configuring message broker options
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");

        registry.enableSimpleBroker("/queue", "/topic");

        registry.setUserDestinationPrefix("/user");
    }

    /**
     * Registers STOMP endpoints for WebSocket connections.
     * Configures the WebSocket connection endpoint with CORS and JWT authentication.
     * 
     * @param registry Registry for STOMP over WebSocket endpoints
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(jwtHandshakeInterceptor);
    }
}
