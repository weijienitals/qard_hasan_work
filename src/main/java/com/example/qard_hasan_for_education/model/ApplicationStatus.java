package com.example.qard_hasan_for_education.model;

public enum ApplicationStatus {
    DOCUMENTS_RECEIVED("Documents received and validated"),
    PROCESSING_DOCUMENTS("Processing documents with AI"),
    DOCUMENTS_PROCESSED("All documents processed successfully"),
    COMPLETED("Application processing completed"),
    FAILED("Application processing failed");

    private final String description;

    ApplicationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return this == COMPLETED || this == FAILED;
    }

    public boolean isSuccessful() {
        return this == COMPLETED;
    }

    public boolean isInProgress() {
        return this == DOCUMENTS_RECEIVED || this == PROCESSING_DOCUMENTS;
    }

    @Override
    public String toString() {
        return name() + ": " + description;
    }
}