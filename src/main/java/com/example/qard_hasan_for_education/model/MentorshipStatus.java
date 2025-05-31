package com.example.qard_hasan_for_education.model;

public enum MentorshipStatus {
    ACTIVE("Active mentorship"),
    COMPLETED("Mentorship completed"),
    PAUSED("Temporarily paused"),
    CANCELLED("Cancelled by either party");

    private final String description;

    MentorshipStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
