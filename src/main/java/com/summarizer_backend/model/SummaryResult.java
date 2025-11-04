package com.summarizer_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "summary_results")
public class SummaryResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔑 CRITICAL FIX: Link to the User who created this summary
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) 
    private User user; 

    @Column(columnDefinition = "TEXT") 
    private String originalText;

    @Column(columnDefinition = "TEXT")
    private String summarizedText;

    private LocalDateTime createdDate = LocalDateTime.now(); 
    
    private String summarizationMode; 
    
    
    public SummaryResult() {}

    // --- Getters and Setters ---
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // 🔑 NEW: Getter/Setter for the User relationship
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getSummarizedText() {
        return summarizedText;
    }

    public void setSummarizedText(String summarizedText) {
        this.summarizedText = summarizedText;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getSummarizationMode() {
        return summarizationMode;
    }

    public void setSummarizationMode(String summarizationMode) {
        this.summarizationMode = summarizationMode;
    }
}