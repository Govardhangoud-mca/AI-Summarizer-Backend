package com.summarizer_backend.model;

public enum SummaryLength {
    
    SHORT(1),
    MEDIUM(2),
    LONG(3);

    private final int id;

    SummaryLength(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}