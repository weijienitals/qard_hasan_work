package com.example.qard_hasan_for_education.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class AffordabilityCheck {

    @JsonProperty("monthlyRepaymentCapacity")
    private BigDecimal monthlyRepaymentCapacity;

    @JsonProperty("estimatedMonthlyPayment")
    private BigDecimal estimatedMonthlyPayment;

    @JsonProperty("affordabilityRatio")
    private BigDecimal affordabilityRatio; // payment/capacity ratio

    @JsonProperty("affordabilityStatus")
    private String affordabilityStatus; // "affordable", "tight", "unaffordable"

}
