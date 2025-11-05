package com.summarizer_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.summarizer_backend.service.AdminService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "https://ai-summarizer-frontend-ten.vercel.app", allowCredentials = "true")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 💡 UPDATED ENDPOINT: Handles history retrieval and accepts the timeFilter query parameter.
     * Maps to: GET /api/v1/admin/history?timeFilter={HOUR/DAY/MONTH}
     */
    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getFilteredSummaries(
            @RequestParam(defaultValue = "DAY") String timeFilter) {

        // Use the new service method to fetch history filtered by time
        List<Map<String, Object>> filteredHistory = adminService.fetchSummariesByTimeFilter(timeFilter);
        return ResponseEntity.ok(filteredHistory);
    }
    
    // Removed the old @GetMapping("/history/all") to replace it with the filtered endpoint.

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> users = adminService.getAllUsersDetails();
        return ResponseEntity.ok(users);
    }
    
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        // Assuming MessageResponse has a constructor that takes a String
        return ResponseEntity.ok(new MessageResponse("User deleted successfully.")); 
    }
}