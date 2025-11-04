package com.summarizer_backend.config;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    // Inject the API key from application.properties
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    /**
     * Creates and configures the Gemini Client as a Spring Bean.
     * This bean is then automatically available for injection into other services.
     */
    @Bean
    public Client geminiClient() {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Gemini API Key is not set in application.properties. Please check 'gemini.api.key'."
            );
        }
        
        // The Client.builder() is used to explicitly configure the API key.
        return Client.builder()
                     .apiKey(geminiApiKey)
                     .build();
    }
}