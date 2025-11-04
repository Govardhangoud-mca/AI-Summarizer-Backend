package com.summarizer_backend.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.Arrays;

@Service
public class SummarizerEngine {

    // The Gemini client is now injected by Spring (thanks to GeminiConfig)
    private final Client geminiClient;
    
    // Inject the model name from application.properties
    @Value("${gemini.model.name}")
    private String modelName;

    // Dependency Injection via Constructor
    public SummarizerEngine(Client geminiClient) {
        this.geminiClient = geminiClient;
    }

    /**
     * Main method to summarize and format text using Gemini AI.
     */
    public String summarizeAndFormat(String text, int lengthId, int modeId) {
        if (text == null || text.trim().isEmpty()) {
            return "Input text is empty.";
        }
        
        // 1. Determine the prompt based on the user's length preference (lengthId)
        String lengthInstruction = getLengthInstruction(lengthId, modeId);
        
        // 2. Construct the full prompt
        String fullPrompt = String.format(
            "Summarize the following text for a technical audience. %s The original text is: %s",
            lengthInstruction,
            text
        );

        try {
            // 3. Configure the content generation options
            GenerateContentConfig config = GenerateContentConfig.builder()
                .temperature((float) 0.2) // Low temperature for factual summarization
                .build();

            // 4. Call the Gemini API
            GenerateContentResponse response = geminiClient.models.generateContent(
                modelName,
                fullPrompt,
                config
            );

            // 5. Return the summarized text
            String summary = response.text();
            
            // Note: The formatting logic (bullet points) is now handled by Gemini's response
            // due to the instruction, making the old formatSummary method largely unnecessary.
            return summary; 

        } catch (Exception e) {
            System.err.println("Gemini API Error: " + e.getMessage());
            // Fallback or detailed error message
            return "Error generating summary: " + e.getMessage();
        }
    }
    
    /**
     * Creates an instruction for Gemini based on length and format.
     */
    private String getLengthInstruction(int lengthId, int modeId) {
        String length = switch (lengthId) {
            case 1 -> "Keep the summary extremely concise, in 1-2 key sentences."; // SHORT
            case 2 -> "Provide a medium-length summary in 3-5 key points.";       // MEDIUM
            case 3 -> "Provide a detailed summary in 5-8 key points.";            // LONG
            default -> "Provide a medium-length summary.";
        };

        String format = switch (modeId) {
            case 2, 3 -> "Return the final summary as a bulleted list.";         // BULLET_POINT or CUSTOM
            case 1 -> "Return the final summary as a single, coherent paragraph."; // PARAGRAPH
            default -> "";
        };

        // Combine length and format instructions
        return length + " " + format;
    }
}