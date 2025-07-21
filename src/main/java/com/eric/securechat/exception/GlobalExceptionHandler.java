package com.eric.securechat.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器。
 * 使用 @ControllerAdvice 注解，使其能作用于项目中所有的 @Controller。
 * 捕获控制器层抛出的异常，并生成统一、规范的JSON响应。
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // 推荐在类中显式定义 logger
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // =========================================================================================
    // 1. 安全与认证异常处理 (返回 401 或 403)
    // =========================================================================================

    /**
     * 【核心修复】处理所有 Spring Security 认证相关的异常。
     * 例如：用户密码错误、用户被禁用、账户被锁定等。
     * 这会捕获 AuthService 中抛出的 BadCredentialsException。
     *
     * @param ex      捕获到的认证异常。
     * @param request 当前请求。
     * @return 包含 401 Unauthorized 状态和错误信息的 ResponseEntity。
     */
    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        // 为了安全，统一返回一个模糊的提示，避免泄露过多信息（例如，避免提示“用户名不存在”还是“密码错误”）
        Map<String, String> customMessage = Map.of("message", "Invalid username or password.");
        return buildErrorResponse(ex, customMessage, HttpStatus.UNAUTHORIZED, request);
    }


    // =========================================================================================
    // 2. 常见业务逻辑异常处理 (返回 4xx 状态码)
    // =========================================================================================

    /**
     * 处理资源未找到的异常，例如查询一个不存在的用户。
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    /**
     * 处理非法参数异常，通常用于校验业务逻辑不满足的情况，例如用户名已存在。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * 处理非法状态异常，通常用于表示对象状态不正确，无法执行请求的操作。
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        // 409 Conflict 是一个很好的状态码，表示请求与服务器当前状态冲突。
        return buildErrorResponse(ex, HttpStatus.CONFLICT, request);
    }

    /**
     * 重写此方法，以自定义 JSR-380 (@Valid) 验证失败时的响应格式。
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", status.value());
        body.put("error", "Validation Failed");

        // 从异常中提取所有字段的详细错误信息
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> String.format("'%s': %s", fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.joining("; "));

        body.put("message", message);
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, headers, status);
    }


    // =========================================================================================
    // 3. 最终防线：处理所有未知异常 (返回 500)
    // =========================================================================================

    /**
     * 捕获所有其他未被专门处理的异常。
     * 这是系统的最后一道防线，防止任何内部错误泄露到客户端。
     *
     * @param ex      捕获到的未知异常。
     * @param request 当前请求。
     * @return 包含 500 Internal Server Error 状态的响应。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(Exception ex, WebRequest request) {
        // 对于未知的服务器内部错误，必须记录ERROR级别的日志，并附上完整的堆栈信息，以便排查问题。
        // 如果你配置了 logback-spring.xml，这里的堆栈会被自动精简。
        log.error("An unexpected error occurred processing request: {}", request.getDescription(false), ex);

        // 返回给客户端的错误信息应保持通用和模糊，避免暴露实现细节。
        Exception responseException = new Exception("An unexpected internal server error occurred. Please contact support.");
        return buildErrorResponse(responseException, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }


    // =========================================================================================
    // 4. 辅助方法
    // =========================================================================================

    /**
     * 基础的错误响应构建器。
     */
    private ResponseEntity<Object> buildErrorResponse(Exception ex, HttpStatus status, WebRequest request) {
        return buildErrorResponse(ex, null, status, request);
    }

    /**
     * 可定制消息的错误响应构建器。
     */
    private ResponseEntity<Object> buildErrorResponse(Exception ex, Map<String, String> customMessages, HttpStatus status, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        // 如果有自定义消息，则使用它，否则使用异常的原始消息
        body.put("message", (customMessages != null && customMessages.containsKey("message")) ? customMessages.get("message") : ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, status);
    }
}
