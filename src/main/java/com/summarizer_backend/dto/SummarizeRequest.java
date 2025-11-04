package com.summarizer_backend.dto;

import com.summarizer_backend.model.SummaryLength;
import com.summarizer_backend.model.OutputMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SummarizeRequest {
    @NotBlank(message = "Text cannot be empty.")
    private String text;

    @NotNull
    private SummaryLength summaryLength; 
    
    @NotNull
    private OutputMode mode; 

    

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public SummaryLength getSummaryLength() {
        return summaryLength;
    }

    public void setSummaryLength(SummaryLength summaryLength) {
        this.summaryLength = summaryLength;
    }

    public OutputMode getMode() {
        return mode;
    }

    public void setMode(OutputMode mode) {
        this.mode = mode;
    }
}