package com.eric.securechat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration for the secure chat application.
 * Configures cross-origin resource sharing for API endpoints and file access.
 * Allows frontend application to communicate with the backend securely.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    /**
     * Configures CORS mappings for API endpoints and file access.
     * Sets up allowed origins, methods, headers, and credentials for cross-origin requests.
     * 
     * @param registry The CORS registry to configure
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
        registry.addMapping("/files/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
