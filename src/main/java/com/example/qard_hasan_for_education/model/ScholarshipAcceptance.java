package com.example.qard_hasan_for_education.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

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

    // New risk assessment fields
    @JsonProperty("fundingGapRisk")
    private String fundingGapRisk;

    @JsonProperty("providerCredibility")
    private String providerCredibility;

    @JsonProperty("documentAuthenticity")
    private String documentAuthenticity;

    @JsonProperty("riskFactors")
    private List<String> riskFactors;

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
    public void setisValidScholarship(boolean isValidScholarship) { this.isValidScholarship = isValidScholarship; }

    public String getFundingGapRisk() { return fundingGapRisk; }
    public void setFundingGapRisk(String fundingGapRisk) { this.fundingGapRisk = fundingGapRisk; }

    public String getProviderCredibility() { return providerCredibility; }
    public void setProviderCredibility(String providerCredibility) { this.providerCredibility = providerCredibility; }

    public String getDocumentAuthenticity() { return documentAuthenticity; }
    public void setDocumentAuthenticity(String documentAuthenticity) { this.documentAuthenticity = documentAuthenticity; }

    public List<String> getRiskFactors() { return riskFactors; }
    public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }

    @Override
    public String toString() {
        return "ScholarshipAcceptance{" +
                "scholarshipName='" + scholarshipName + '\'' +
                ", recipientName='" + recipientName + '\'' +
                ", amount=" + amount +
                ", provider='" + provider + '\'' +
                ", academicYear='" + academicYear + '\'' +
                ", isValidScholarship=" + isValidScholarship +
                ", fundingGapRisk='" + fundingGapRisk + '\'' +
                ", documentAuthenticity='" + documentAuthenticity + '\'' +
                '}';
    }
}
