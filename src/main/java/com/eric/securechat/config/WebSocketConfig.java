package com.eric.securechat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Enables WebSocket message handling, backed by a message broker.
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The "/ws" endpoint is where the clients will connect to.
        // STOMP is a simple text-oriented messaging protocol that we'll use over WebSocket.
        // `withSockJS()` is a fallback for browsers that don't support WebSocket.
        registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Messages whose destination starts with "/app" should be routed to message-handling methods (in our @Controller).
        registry.setApplicationDestinationPrefixes("/app");
        // Messages whose destination starts with "/topic" or "/user" should be routed to the message broker.
        // The broker broadcasts messages to all connected clients who are subscribed to a topic.
        // "/user" is used for sending messages to a specific user.
        registry.enableSimpleBroker("/topic", "/user");
        registry.setUserDestinationPrefix("/user");
    }
}
