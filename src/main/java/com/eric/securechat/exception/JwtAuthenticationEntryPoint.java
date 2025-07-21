package com.eric.securechat.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component // 将其声明为 Spring 组件，以便可以注入
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // 当认证失败时，此方法被调用
        response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 设置状态码 401
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Unauthorized");
        // 提供一个更通用的、对前端友好的错误信息
        body.put("message", "Authentication failed: " + authException.getMessage());
        body.put("path", request.getRequestURI());

        // 使用 ObjectMapper 将 Map 转换为 JSON 字符串并写入响应
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
