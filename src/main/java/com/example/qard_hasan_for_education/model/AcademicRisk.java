package com.example.qard_hasan_for_education.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class AcademicRisk {
    @JsonProperty("riskLevel")
    private RiskLevel riskLevel;

    @JsonProperty("universityTier")
    private String universityTier; // "top-tier", "mid-tier", "lower-tier"

    @JsonProperty("programMarketability")
    private String programMarketability; // "high", "medium", "low"

    @JsonProperty("fundingGapRisk")
    private String fundingGapRisk; // "none", "low", "medium", "high"

    @JsonProperty("completionProbability")
    private String completionProbability; // "very-high", "high", "medium", "low"

    @JsonProperty("riskFactors")
    private List<String> riskFactors;

    // Constructors
    public AcademicRisk() {}

    // Getters and Setters
    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public String getUniversityTier() { return universityTier; }
    public void setUniversityTier(String universityTier) { this.universityTier = universityTier; }

    public String getProgramMarketability() { return programMarketability; }
    public void setProgramMarketability(String programMarketability) { this.programMarketability = programMarketability; }

    public String getFundingGapRisk() { return fundingGapRisk; }
    public void setFundingGapRisk(String fundingGapRisk) { this.fundingGapRisk = fundingGapRisk; }

    public String getCompletionProbability() { return completionProbability; }
    public void setCompletionProbability(String completionProbability) { this.completionProbability = completionProbability; }

    public List<String> getRiskFactors() { return riskFactors; }
    public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }
}
