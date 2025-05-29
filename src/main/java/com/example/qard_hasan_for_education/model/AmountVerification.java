package com.example.qard_hasan_for_education.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public class AmountVerification {
    @JsonProperty("requestedAmount")
    private BigDecimal requestedAmount;

    @JsonProperty("recommendedAmount")
    private BigDecimal recommendedAmount;

    @JsonProperty("verificationStatus")
    private String verificationStatus; // "reasonable", "too-high", "too-low"

    @JsonProperty("fundingGapAnalysis")
    private FundingGapAnalysis fundingGapAnalysis;

    @JsonProperty("affordabilityCheck")
    private AffordabilityCheck affordabilityCheck;

    @JsonProperty("verificationIssues")
    private List<String> verificationIssues;


}