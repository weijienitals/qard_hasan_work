package com.example.qard_hasan_for_education.model;

public enum PaymentStatus {
    PENDING("Payment initiated, processing"),
    COMPLETED("Payment successful"),
    FAILED("Payment failed"),
    REFUNDED("Payment refunded");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}
