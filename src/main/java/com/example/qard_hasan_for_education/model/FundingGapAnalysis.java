package com.example.qard_hasan_for_education.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class FundingGapAnalysis {

    @JsonProperty("estimatedTotalCost")
    private BigDecimal estimatedTotalCost;

    @JsonProperty("scholarshipAmount")
    private BigDecimal scholarshipAmount;

    @JsonProperty("actualFundingNeeded")
    private BigDecimal actualFundingNeeded;

    @JsonProperty("requestedVsNeeded")
    private String requestedVsNeeded; // "adequate", "excessive", "insufficient"


}
