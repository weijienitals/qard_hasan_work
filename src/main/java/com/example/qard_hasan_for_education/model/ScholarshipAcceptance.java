package com.example.qard_hasan_for_education.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class ScholarshipAcceptance {
    @JsonProperty("scholarshipName")
    private String scholarshipName;

    @JsonProperty("recipientName")
    private String recipientName;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("provider")
    private String provider;

    @JsonProperty("academicYear")
    private String academicYear;

    @JsonProperty("isValidScholarship")
    private boolean isValidScholarship;

    // Constructors
    public ScholarshipAcceptance() {}

    public ScholarshipAcceptance(String scholarshipName, String recipientName, BigDecimal amount, String provider, String academicYear) {
        this.scholarshipName = scholarshipName;
        this.recipientName = recipientName;
        this.amount = amount;
        this.provider = provider;
        this.academicYear = academicYear;
        this.isValidScholarship = true;
    }

    // Getters and Setters
    public String getScholarshipName() { return scholarshipName; }
    public void setScholarshipName(String scholarshipName) { this.scholarshipName = scholarshipName; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Boolean getisValidScholarship() { return isValidScholarship; }
    public void getisValidScholarship(boolean isValidScholarship) { this.isValidScholarship = isValidScholarship; }

    @Override
    public String toString() {
        return "ScholarshipAcceptance{" +
                "scholarshipName='" + scholarshipName + '\'' +
                ", recipientName='" + recipientName + '\'' +
                ", amount=" + amount +
                ", provider='" + provider + '\'' +
                ", academicYear='" + academicYear + '\'' +
                ", isValidScholarship=" + isValidScholarship +
                '}';
    }
}