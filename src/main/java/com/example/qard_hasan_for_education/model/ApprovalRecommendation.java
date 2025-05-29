package com.example.qard_hasan_for_education.model;

public enum ApprovalRecommendation {
    APPROVE("Approve with standard terms"),
    APPROVE_WITH_CONDITIONS("Approve with additional conditions/guarantor"),
    FURTHER_REVIEW("Requires manual review"),
    REJECT("Recommend rejection");

    private final String description;

    ApprovalRecommendation(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}