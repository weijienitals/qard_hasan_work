package com.example.qard_hasan_for_education.model;

public enum HelpType {
    ACADEMIC("Academic guidance and study tips"),
    MENTAL_HEALTH("Mental health and emotional support"),
    CULTURAL_TRANSITION("Cultural adaptation and lifestyle tips"),
    CAREER_GUIDANCE("Career advice and networking"),
    FINANCIAL_ADVICE("Financial management tips"),
    GENERAL("General guidance and friendship");

    private final String description;

    HelpType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
