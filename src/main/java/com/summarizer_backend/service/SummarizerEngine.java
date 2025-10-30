package com.summarizer_backend.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class SummarizerEngine {

    /**
     * Main method to summarize and format text.
     */
    public String summarizeAndFormat(String text, int lengthId, int modeId) {
        
        // 1. CLEANUP AND PRE-PROCESSING: Remove code, HTML, and escape sequences.
        String cleanedText = cleanText(text);

        // 2. SENTENCE IDENTIFICATION: Use regex to split text into distinct sentences.
        List<String> sentences = splitIntoSentences(cleanedText);

        // 3. SELECTION: Select the most relevant sentences based on lengthId.
        List<String> selectedSentences = selectSentences(sentences, lengthId);

        // 4. FORMATTING: Apply bullet points or paragraph formatting.
        return formatSummary(selectedSentences, modeId);
    }

    // ----------------------------------------------------------------------
    // STEP 1: TEXT CLEANUP
    // ----------------------------------------------------------------------
    private String cleanText(String text) {
        if (text == null) return "";
        
        // 1. Remove common HTML tags, React components, and JS imports (crude cleanup for code)
        // This removes content between <...> including the tags themselves.
        String noCode = text.replaceAll("<[^>]*>", " ");
        
        // 2. Remove common code-related noise (e.g., imports, function signatures, console logs)
        noCode = noCode.replaceAll("import [^;]*;", " ");
        noCode = noCode.replaceAll("function [^{]*\\{", " ");
        noCode = noCode.replaceAll("const [^=]*=", " ");
        noCode = noCode.replaceAll("console\\.log[^;]*;", " ");

        // 3. Replace common JSON/Java escape sequences with spaces
        String noEscapes = noCode.replaceAll("\\s+", " ")
                                 .replaceAll("\\r|\\n|\\t", " ")
                                 .trim();
        
        return noEscapes;
    }

    // ----------------------------------------------------------------------
    // STEP 2: SENTENCE IDENTIFICATION
    // ----------------------------------------------------------------------
    private List<String> splitIntoSentences(String text) {
        // Simple heuristic: split by punctuation followed by a space
        Pattern pattern = Pattern.compile("(?<=[.?!])\\s+(?=[A-Z0-9])");
        return Arrays.stream(pattern.split(text))
                     .filter(s -> s.length() > 5) // Ignore very short fragments
                     .map(String::trim)
                     .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    // ----------------------------------------------------------------------
    // STEP 3: SENTENCE SELECTION (Simple Heuristic Summarization)
    // ----------------------------------------------------------------------
    private List<String> selectSentences(List<String> sentences, int lengthId) {
        if (sentences.isEmpty()) {
            return new ArrayList<>();
        }
        
        int totalSentences = sentences.size();
        int targetCount;

        switch (lengthId) {
            case 1: // SHORT: Approx 10%
                targetCount = Math.max(1, totalSentences / 10);
                break;
            case 3: // LONG: Approx 40%
                targetCount = totalSentences * 4 / 10;
                break;
            case 2: // MEDIUM: Approx 20%
            default:
                targetCount = totalSentences * 2 / 10;
                break;
        }

        // Ensure targetCount doesn't exceed totalSentences
        targetCount = Math.min(targetCount, totalSentences);
        
        // For simplicity, select the first N sentences (the most common quick heuristic)
        return sentences.subList(0, targetCount);
    }

    // ----------------------------------------------------------------------
    // STEP 4: FORMATTING
    // ----------------------------------------------------------------------
    private String formatSummary(List<String> sentences, int modeId) {
        if (sentences.isEmpty()) {
            return "No meaningful text was extracted for summarization.";
        }
        
        StringBuilder sb = new StringBuilder();
        
        switch (modeId) {
            case 2: // BULLET_POINT
                for (String sentence : sentences) {
                    sb.append("• ").append(sentence).append("\n");
                }
                return sb.toString().trim();
                
            case 3: // CUSTOM (e.g., numbered list)
                for (int i = 0; i < sentences.size(); i++) {
                    sb.append(i + 1).append(". ").append(sentences.get(i)).append("\n");
                }
                return sb.toString().trim();

            case 1: // PARAGRAPH
            default:
                // Join all sentences with a single space
                return String.join(" ", sentences);
        }
    }
}