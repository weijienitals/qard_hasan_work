package com.example.qard_hasan_for_education.model.individual;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public class SimpleBankInfo {
    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("bankName")
    private String bankName;

    @JsonProperty("accountHolderName")
    private String accountHolderName;

    @JsonProperty("currentBalance")
    private BigDecimal currentBalance;

    @JsonProperty("purchasingPower")
    private String purchasingPower;

    // New risk assessment fields
    @JsonProperty("incomeStability")
    private String incomeStability;

    @JsonProperty("expenseRatio")
    private BigDecimal expenseRatio;

    @JsonProperty("savingsTrend")
    private String savingsTrend;

    @JsonProperty("overdraftCount")
    private Integer overdraftCount;

    @JsonProperty("repaymentCapacity")
    private String repaymentCapacity;

    @JsonProperty("riskFactors")
    private List<String> riskFactors;

    @JsonProperty("monthlyIncome")
    private BigDecimal monthlyIncome;

    @JsonProperty("monthlyExpenses")
    private BigDecimal monthlyExpenses;

    @JsonProperty("transactions")
    private List<String> transactions;

    // Constructors
    public SimpleBankInfo() {}

    public SimpleBankInfo(String accountNumber, String bankName, String accountHolderName, BigDecimal currentBalance, String purchasingPower) {
        this.accountNumber = accountNumber;
        this.bankName = bankName;
        this.accountHolderName = accountHolderName;
        this.currentBalance = currentBalance;
        this.purchasingPower = purchasingPower;
    }

    // Getters and Setters
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }

    public String getPurchasingPower() { return purchasingPower; }
    public void setPurchasingPower(String purchasingPower) { this.purchasingPower = purchasingPower; }

    public String getIncomeStability() { return incomeStability; }
    public void setIncomeStability(String incomeStability) { this.incomeStability = incomeStability; }

    public BigDecimal getExpenseRatio() { return expenseRatio; }
    public void setExpenseRatio(BigDecimal expenseRatio) { this.expenseRatio = expenseRatio; }

    public String getSavingsTrend() { return savingsTrend; }
    public void setSavingsTrend(String savingsTrend) { this.savingsTrend = savingsTrend; }

    public Integer getOverdraftCount() { return overdraftCount; }
    public void setOverdraftCount(Integer overdraftCount) { this.overdraftCount = overdraftCount; }

    public String getRepaymentCapacity() { return repaymentCapacity; }
    public void setRepaymentCapacity(String repaymentCapacity) { this.repaymentCapacity = repaymentCapacity; }

    public List<String> getRiskFactors() { return riskFactors; }
    public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }

    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public BigDecimal getMonthlyExpenses() { return monthlyExpenses; }
    public void setMonthlyExpenses(BigDecimal monthlyExpenses) { this.monthlyExpenses = monthlyExpenses; }

    public List<String> getTransactions() { return transactions; }
    public void setTransactions(List<String> transactions) { this.transactions = transactions; }

    @Override
    public String toString() {
        return "SimpleBankInfo{" +
                "accountNumber='" + accountNumber + '\'' +
                ", bankName='" + bankName + '\'' +
                ", accountHolderName='" + accountHolderName + '\'' +
                ", currentBalance=" + currentBalance +
                ", purchasingPower=" + purchasingPower +
                ", incomeStability='" + incomeStability + '\'' +
                ", repaymentCapacity='" + repaymentCapacity + '\'' +
                '}';
    }
}
