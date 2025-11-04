package com.summarizer_backend.dto;


public class AuthResponse {

 private String token;
 private Long userId;
 private String message;

 public AuthResponse(String token, Long userId, String message) {
     this.token = token;
     this.userId = userId;
     this.message = message;
 }

 
 public String getToken() {
     return token;
 }

 public void setToken(String token) {
     this.token = token;
 }

 public Long getUserId() {
     return userId;
 }

 public void setUserId(Long userId) {
     this.userId = userId;
 }

 public String getMessage() {
     return message;
 }

 public void setMessage(String message) {
     this.message = message;
 }
}
