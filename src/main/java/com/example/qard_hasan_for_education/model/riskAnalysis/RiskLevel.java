package com.example.qard_hasan_for_education.model.riskAnalysis;

public enum RiskLevel {
    LOW("Low risk - proceed with standard terms"),
    MEDIUM("Medium risk - require additional safeguards"),
    HIGH("High risk - recommend rejection or strict conditions");

    private final String description;

    RiskLevel(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}

