package com.example.qard_hasan_for_education.model.riskAnalysis;

import com.example.qard_hasan_for_education.model.ApprovalRecommendation;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ApplicationRiskProfile {
    @JsonProperty("overallRisk")
    private RiskLevel overallRisk;

    @JsonProperty("financialRisk")
    private FinancialRisk financialRisk;

    @JsonProperty("academicRisk")
    private AcademicRisk academicRisk;

    @JsonProperty("fraudRisk")
    private FraudRisk fraudRisk;

    @JsonProperty("riskScore")
    private Integer riskScore; // 0-100, higher = more risky

    @JsonProperty("recommendations")
    private List<String> recommendations;

    @JsonProperty("approvalRecommendation")
    private ApprovalRecommendation approvalRecommendation;

    // Constructors
    public ApplicationRiskProfile() {}

    // Getters and Setters
    public RiskLevel getOverallRisk() { return overallRisk; }
    public void setOverallRisk(RiskLevel overallRisk) { this.overallRisk = overallRisk; }

    public FinancialRisk getFinancialRisk() { return financialRisk; }
    public void setFinancialRisk(FinancialRisk financialRisk) { this.financialRisk = financialRisk; }

    public AcademicRisk getAcademicRisk() { return academicRisk; }
    public void setAcademicRisk(AcademicRisk academicRisk) { this.academicRisk = academicRisk; }

    public FraudRisk getFraudRisk() { return fraudRisk; }
    public void setFraudRisk(FraudRisk fraudRisk) { this.fraudRisk = fraudRisk; }

    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }

    public ApprovalRecommendation getApprovalRecommendation() { return approvalRecommendation; }
    public void setApprovalRecommendation(ApprovalRecommendation approvalRecommendation) { this.approvalRecommendation = approvalRecommendation; }
}
