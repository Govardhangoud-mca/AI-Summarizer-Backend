package com.summarizer_backend.dto;

// Using a record for simplicity
public record JwtResponse(
    String token,
    String type,
    Long id,
    String username,
    String role
) {
    // Constructor to easily create the response, setting the type to "Bearer"
    public JwtResponse(String token, Long id, String username, String role) {
        this(token, "Bearer", id, username, role);
    }
}