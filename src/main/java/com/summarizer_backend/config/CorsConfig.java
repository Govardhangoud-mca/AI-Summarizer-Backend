package com.summarizer_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/**") // Apply CORS configuration to all API endpoints
            .allowedOrigins(
                "http://localhost:5173", // Keep this for local frontend development (assuming Vite's default port)
                "https://ai-summarizer-ias9.vercel.app" // 🎯 VERCEL DEPLOYED FRONTEND
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*") // Allow all headers
            .allowCredentials(true) // Crucial for session cookies (JSESSIONID)
            .maxAge(3600); // Cache pre-flight response for 1 hour
    }
}