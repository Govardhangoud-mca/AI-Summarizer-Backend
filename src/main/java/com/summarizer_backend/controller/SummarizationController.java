package com.summarizer_backend.controller;

import com.summarizer_backend.dto.SummarizeRequest;
import com.summarizer_backend.dto.SummarizeResponse;
import com.summarizer_backend.service.SummarizationService;
import com.summarizer_backend.model.OutputMode;
import com.summarizer_backend.model.SummaryLength;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/text")
@CrossOrigin(origins = "https://ai-summarizer-frontend-rp6k.vercel.app", allowCredentials = "true")
public class SummarizationController {

    @Autowired
    private SummarizationService summarizationService;

    
    @PostMapping("/summarize")
    public ResponseEntity<SummarizeResponse> summarizeRawText(
        @Valid @RequestBody SummarizeRequest request) {
        
        // Validation for minimum text length (beyond @NotBlank)
        if (request.getText() == null || request.getText().length() < 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Input text must be at least 100 characters long for meaningful summarization.");
        }
        
        SummarizeResponse response = summarizationService.summarizeAndSave(request);
        return ResponseEntity.ok(response);
    }

    
    @PostMapping("/summarize/file")
    public ResponseEntity<SummarizeResponse> summarizeFile(
        @RequestParam("file") MultipartFile file,
        @RequestParam("summaryLength") SummaryLength lengthMode,
        @RequestParam("mode") OutputMode mode
    ) throws IOException {
        
        if (file.isEmpty() || file.getSize() == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is missing or empty.");
        }
        
      
        String extractedText = summarizationService.extractText(file);

        
        if (extractedText.startsWith("ERROR:")) {
             throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File extraction failed: " + extractedText);
        }

       
        SummarizeRequest request = new SummarizeRequest();
        request.setText(extractedText);
        request.setSummaryLength(lengthMode);
        request.setMode(mode);
        
        
        SummarizeResponse response = summarizationService.summarizeAndSave(request);
        
        return ResponseEntity.ok(response);
    }
    
   
    @PostMapping("/extract")
    public ResponseEntity<String> extractTextFromFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty() || file.getSize() == 0) {
            return ResponseEntity.badRequest().body("{\"error\": \"File is missing or empty.\"}");
        }
        
        String extractedText = summarizationService.extractText(file);
        
        
        String safeJsonText = extractedText.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
        
        
        if (extractedText.startsWith("ERROR:")) {
            return ResponseEntity.internalServerError().body("{\"error\": \"" + safeJsonText + "\"}");
        }
        
        return ResponseEntity.ok("{\"extractedText\": \"" + safeJsonText + "\"}");
    }
}