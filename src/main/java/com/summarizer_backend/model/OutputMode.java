package com.summarizer_backend.model;

public enum OutputMode {
    
    PARAGRAPH(1),
    BULLET_POINT(2),
    CUSTOM(3);

    private final int id;

    OutputMode(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}