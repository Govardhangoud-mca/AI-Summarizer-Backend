package com.summarizer_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder; // <-- NEW IMPORT
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.summarizer_backend.dto.SummarizeRequest;
import com.summarizer_backend.dto.SummarizeResponse;
import com.summarizer_backend.model.SummaryResult;
import com.summarizer_backend.repository.SummaryResultRepository;
import com.summarizer_backend.repository.UserRepository; // <-- NEW IMPORT
import com.summarizer_backend.model.User; // <-- NEW IMPORT
import com.summarizer_backend.model.OutputMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class SummarizationService {

    @Autowired
    private SummarizerEngine summarizerEngine;

    @Autowired
    private SummaryResultRepository repository;
    
    @Autowired // <-- ADDED THIS DEPENDENCY
    private UserRepository userRepository;
    
    @Autowired
    private FileTextExtractor fileTextExtractor; 

    // Define the maximum text size before chunking is triggered (e.g., 15,000 characters)
    private static final int MAX_CHUNK_SIZE = 15000; 

    // ----------------------------------------------------------------------
    // Main Summarization Entry Point
    // ----------------------------------------------------------------------
    public SummarizeResponse summarizeAndSave(SummarizeRequest request) {
        
        String inputText = request.getText();
        
        // 1. Check if chunking is necessary for large text
        if (inputText.length() > MAX_CHUNK_SIZE) {
            return summarizeByChunking(request);
        }

        // 2. Standard single-call logic for normal text
        String summary = summarizerEngine.summarizeAndFormat(
            inputText, 
            request.getSummaryLength().getId(), 
            request.getMode().getId() 
        ); 

        SummarizeResponse response = calculateStatsAndCreateResponse(summary);
        
        // **Calling the updated save method**
        saveSummaryResult(request.getText(), summary, request.getMode()); 

        return response;
    }
    
    // ----------------------------------------------------------------------
    // File Extraction Entry Point
    // ----------------------------------------------------------------------
    public String extractText(MultipartFile file) throws IOException {
        String extractedText = fileTextExtractor.extractText(file);

        if (extractedText.length() < 100) {
            return "ERROR: Extracted text is too short or file contains unreadable content.";
        }
        return extractedText;
    }


    // ----------------------------------------------------------------------
    // Large Text Chunking Logic
    // ----------------------------------------------------------------------
    private SummarizeResponse summarizeByChunking(SummarizeRequest request) {
        String inputText = request.getText();
        
        // 🛑 NOTE: Text is split into chunks, attempting to preserve sentence breaks.
        List<String> chunks = splitTextIntoChunks(inputText, MAX_CHUNK_SIZE);
        StringBuilder finalSummary = new StringBuilder();
        
        // The summarization mode (PARAGRAPH, BULLET_POINT)
        int modeId = request.getMode().getId();

        // Process each chunk individually
        for (String chunk : chunks) {
            // Summarize this chunk
            String chunkSummary = summarizerEngine.summarizeAndFormat(
                chunk,
                request.getSummaryLength().getId(), 
                modeId 
            );
            
            // Append the summary. We add a line break between chunk summaries
            finalSummary.append(chunkSummary).append("\n\n");
        }
        
        String summary = finalSummary.toString().trim();
        
        SummarizeResponse response = calculateStatsAndCreateResponse(summary);
        
        // Save only a snippet of the large text to avoid overloading the database
        String originalTextSnippet = request.getText().substring(0, Math.min(request.getText().length(), 500)) + 
                                     (request.getText().length() > 500 ? "..." : "");
                                         
        // **Calling the updated save method**
        saveSummaryResult(originalTextSnippet, summary, request.getMode()); 
        return response;
    }

    // --- Helper Methods ---
    
    // ... (splitTextIntoChunks remains unchanged) ...
    private List<String> splitTextIntoChunks(String text, int maxSize) {
        List<String> chunks = new ArrayList<>();
        // Use a simple splitter for very large text
        Pattern sentenceSplitter = Pattern.compile("(?<=[.?!])\\s+");
        String[] sentences = sentenceSplitter.split(text);
        
        StringBuilder currentChunk = new StringBuilder();
        for (String sentence : sentences) {
            // Check if adding the next sentence exceeds the max chunk size
            if (currentChunk.length() + sentence.length() + 1 > maxSize) {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString());
                    currentChunk = new StringBuilder();
                }
                // Handle the case where a single sentence is larger than the chunk size
                if (sentence.length() > maxSize) {
                    chunks.add(sentence);
                    continue;
                }
            }
            currentChunk.append(sentence).append(" ");
        }
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }
        return chunks;
    }

    // ... (calculateStatsAndCreateResponse remains unchanged) ...
    private SummarizeResponse calculateStatsAndCreateResponse(String summary) {
        // Clean summary before counting to remove final bullet points or trailing spaces
        String cleanSummary = summary.replaceAll("[•\\n\\r\\t]", " ").trim();
        
        // Count sentences by looking for punctuation followed by a space (similar to engine logic)
        int sentenceCount = (int) Arrays.stream(cleanSummary.split("(?<=[.?!])\\s+"))
                                             .filter(s -> !s.isBlank())
                                             .count();
                                             
        // Word count: split by any whitespace.
        int wordCount = cleanSummary.isBlank() ? 0 : cleanSummary.split("\\s+").length;
        
        if (wordCount > 0 && sentenceCount == 0) {
            sentenceCount = 1;
        }
        
        return new SummarizeResponse(summary, sentenceCount, wordCount);
    }
    
    /**
     * Fixes the DataIntegrityViolationException by setting the authenticated user.
     */
    private void saveSummaryResult(String originalText, String summary, OutputMode mode) {
        String modeString = mode.name(); 

        // 1. Get the username from the Security Context
        // This relies on JwtAuthenticationFilter successfully setting the context.
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Fetch the full User entity using the repository
        User user = userRepository.findByUsername(username)
                     .orElseThrow(() -> new RuntimeException("Authenticated user not found. Data issue.")); // Use RuntimeException or a custom exception

        SummaryResult result = new SummaryResult();
        result.setOriginalText(originalText);
        result.setSummarizedText(summary);
        result.setSummarizationMode(modeString);
        
        // 3. Set the required User object
        result.setUser(user); 
        
        repository.save(result);
    }
}