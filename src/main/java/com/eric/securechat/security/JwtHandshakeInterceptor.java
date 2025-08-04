package com.eric.securechat.security;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

/**
 * WebSocket handshake interceptor for JWT authentication.
 * Extracts JWT token from the WebSocket handshake query and sets authentication context.
 * Enables secure WebSocket connections with user identity.
 */
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;

    /**
     * Constructor for JwtHandshakeInterceptor.
     * 
     * @param jwtService Service for JWT token operations
     */
    public JwtHandshakeInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Intercepts the WebSocket handshake to extract and validate JWT token.
     * Sets authentication context if token is valid.
     * 
     * @param request The server HTTP request
     * @param response The server HTTP response
     * @param wsHandler The WebSocket handler
     * @param attributes The handshake attributes
     * @return true to proceed with the handshake
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    String token = param.substring("token=".length());
                    try {
                        String username = jwtService.extractUsername(token);
                        if (username != null) {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(username, null, null);
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    } catch (Exception e) {
                        // Ignore token parsing errors
                    }
                }
            }
        }
        return true;
    }

    /**
     * Called after the WebSocket handshake is complete. No implementation needed.
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                              WebSocketHandler wsHandler, Exception exception) {
        // No implementation needed
    }
} 