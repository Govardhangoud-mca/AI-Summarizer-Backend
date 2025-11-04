package com.summarizer_backend.service;

import com.summarizer_backend.model.User;
import com.summarizer_backend.repository.UserRepository;
import com.summarizer_backend.repository.SummaryResultRepository;
import com.summarizer_backend.model.SummaryResult;

// Ensure ALL these imports are present
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime; // 💡 NEW: Crucial import for time calculation
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SummaryResultRepository summaryResultRepository; 

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ----------------------------------------------------------------------
    
    /**
     * 💡 NEW METHOD: Handles filtering of summarized data based on time.
     */
    public List<Map<String, Object>> fetchSummariesByTimeFilter(String timeFilter) {

        LocalDateTime startTime = calculateStartTime(timeFilter);
        List<SummaryResult> filteredSummaries;

        if (startTime != null) {
            // Use the new Repository method for filtered results
            filteredSummaries = summaryResultRepository.findByCreatedDateAfter(startTime);
        } else {
            // If "ALL" is requested (timeFilter=ALL), use the optimized fetch method
            filteredSummaries = summaryResultRepository.findAllWithUser();
        }

        return filteredSummaries.stream().map(summary -> {
            
            User user = summary.getUser(); 

            // Safely map data
            String username = user != null ? user.getUsername() : "N/A";
            Long userId = user != null ? user.getId() : Long.valueOf(-1); 
            String role = (user != null && user.getRole() != null) ? user.getRole().name() : "N/A";
            
            // Map keys updated for consistency with frontend structure
            Map<String, Object> map = new HashMap<>();
            map.put("id", summary.getId()); // Changed from summaryId to id
            map.put("summaryText", summary.getSummarizedText());
            map.put("inputText", summary.getOriginalText()); // Changed from originalText to inputText
            map.put("username", username);
            map.put("userId", userId);
            map.put("role", role);
            map.put("mode", summary.getSummarizationMode());
            map.put("timestamp", summary.getCreatedDate().format(FORMATTER));
            return map;

        }).collect(Collectors.toList());
    }
    
    /**
     * 💡 NEW HELPER: Calculates the time cutoff point based on the filter string.
     */
    private LocalDateTime calculateStartTime(String timeFilter) {
        LocalDateTime now = LocalDateTime.now();

        return switch (timeFilter.toUpperCase()) {
            case "MINUTE" -> now.minusMinutes(1);
            case "HOUR" -> now.minusHours(1);
            case "DAY" -> now.minusDays(1);
            case "WEEK" -> now.minusWeeks(1); 
            case "MONTH" -> now.minusMonths(1);
            case "ALL" -> null; // No filter needed
            default -> now.minusDays(1); // Default to DAY
        };
    }
    
    // ----------------------------------------------------------------------
    // The previous fetchAllSummariesWithUserDetails method is removed/replaced.
    // ----------------------------------------------------------------------

    /**
     * Fetches details for all registered users (User Management).
     * ⚠️ NO CHANGES TO THIS METHOD, it remains exactly as you provided it.
     */
    public List<Map<String, Object>> getAllUsersDetails() {
        List<User> allUsers = userRepository.findAll();

        return allUsers.stream().map(user -> {
            String roleName = user.getRole().name();
            
            // 🔑 FIX: Using HashMap for maximum compatibility
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("role", roleName);
            // The email field is NOT included here, matching your request.
            return map;
        }).collect(Collectors.toList());
    }

    // ----------------------------------------------------------------------

    /**
     * Deletes a user by ID and cleans up all associated summary history.
     */
    @Transactional
    public void deleteUser(Long userId) {
        // 1. Verify user existence
        userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, 
                "User not found with ID: " + userId
            ));

        // 2. Delete associated SummaryResults first 
        summaryResultRepository.deleteAllByUserId(userId); 
        
        // 3. Delete the user
        userRepository.deleteById(userId); 
    }
}