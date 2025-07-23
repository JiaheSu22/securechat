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

@Configuration
@EnableWebSocketMessageBroker // 开启WebSocket消息代理功能
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    public WebSocketConfig(JwtHandshakeInterceptor jwtHandshakeInterceptor) {
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
    }

    /**
     * 配置消息代理 (Message Broker)
     * @param registry a registry for configuring message broker options
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 1. 设置应用目标前缀：
        //    定义了客户端发送消息到服务器的目标地址的前缀。
        //    例如，客户端发送消息到 "/app/chat"，这个消息会被路由到带有 @MessageMapping("/chat") 注解的方法。
        registry.setApplicationDestinationPrefixes("/app");

        // 2. 启用一个简单的内存消息代理：
        //    定义了服务器广播消息给客户端的目标地址的前缀。
        //    客户端需要订阅以 "/topic" 或 "/queue" 开头的地址来接收消息。
        //    "/queue" 通常用于点对点消息（私聊）。
        //    "/topic" 通常用于发布/订阅模式（群聊）。
        registry.enableSimpleBroker("/queue", "/topic");

        // 3. 设置用户目标前缀：
        //    这是为了支持特定用户的点对点消息。Spring会自动将发送到 "/user/queue/messages" 的消息，
        //    路由给特定的用户。这是实现私聊的关键。
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * 注册STOMP端点，这是WebSocket的连接入口
     * @param registry a registry for STOMP over WebSocket endpoints
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. 注册一个 "/ws" 的STOMP端点。
        //    客户端将通过这个URL来建立WebSocket连接。
        // 2. withSockJS() 是一个备选方案。
        //    如果浏览器不支持WebSocket，它会使用SockJS（一个JavaScript库）来模拟一个类似WebSocket的连接。
        //    这大大提高了浏览器的兼容性。
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // 在开发阶段允许所有来源，生产环境应配置得更严格
                .addInterceptors(jwtHandshakeInterceptor); // 这里注入你的拦截器

    }
}
