package com.example.qard_hasan_for_education.model.riskAnalysis;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public class FinancialRisk {
    @JsonProperty("riskLevel")
    private RiskLevel riskLevel;

    @JsonProperty("incomeStability")
    private String incomeStability; // "stable", "irregular", "declining"

    @JsonProperty("expenseRatio")
    private BigDecimal expenseRatio; // percentage of income spent

    @JsonProperty("savingsTrend")
    private String savingsTrend; // "increasing", "stable", "decreasing"

    @JsonProperty("overdraftCount")
    private Integer overdraftCount;

    @JsonProperty("riskFactors")
    private List<String> riskFactors;

    @JsonProperty("repaymentCapacity")
    private String repaymentCapacity; // "excellent", "good", "fair", "poor"

    // Constructors
    public FinancialRisk() {}

    // Getters and Setters
    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public String getIncomeStability() { return incomeStability; }
    public void setIncomeStability(String incomeStability) { this.incomeStability = incomeStability; }

    public BigDecimal getExpenseRatio() { return expenseRatio; }
    public void setExpenseRatio(BigDecimal expenseRatio) { this.expenseRatio = expenseRatio; }

    public String getSavingsTrend() { return savingsTrend; }
    public void setSavingsTrend(String savingsTrend) { this.savingsTrend = savingsTrend; }

    public Integer getOverdraftCount() { return overdraftCount; }
    public void setOverdraftCount(Integer overdraftCount) { this.overdraftCount = overdraftCount; }

    public List<String> getRiskFactors() { return riskFactors; }
    public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }

    public String getRepaymentCapacity() { return repaymentCapacity; }
    public void setRepaymentCapacity(String repaymentCapacity) { this.repaymentCapacity = repaymentCapacity; }
}