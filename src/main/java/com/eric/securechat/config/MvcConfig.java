package com.eric.securechat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 URL 路径 /files/** 映射到本地文件系统的 ./uploads/ 目录
        // 例如，访问 http://.../files/abc.jpg 就会去 ./uploads/abc.jpg 找文件
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:./uploads/");
    }
}
