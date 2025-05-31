// LoanStatus.java
package com.example.qard_hasan_for_education.model;

public enum LoanStatus {
    ACTIVE("Active - payments ongoing"),
    COMPLETED("Loan fully repaid"),
    OVERDUE("Payment overdue"),
    SUSPENDED("Loan suspended"),
    DEFAULTED("Loan in default");

    private final String description;

    LoanStatus(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}

