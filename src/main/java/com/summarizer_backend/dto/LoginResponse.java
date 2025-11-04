package com.summarizer_backend.dto;

public record LoginResponse(String token, String type, Long id, String username, String role) {}
