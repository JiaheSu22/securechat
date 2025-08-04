package com.eric.securechat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC configuration for static resource handling.
 * Maps file URLs to local file system for serving uploaded files.
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    /**
     * Configures static resource handlers for serving uploaded files.
     * Maps URL path /files/** to local file system directory ./uploads/.
     * 
     * @param registry The resource handler registry to configure
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:./uploads/");
    }
}
