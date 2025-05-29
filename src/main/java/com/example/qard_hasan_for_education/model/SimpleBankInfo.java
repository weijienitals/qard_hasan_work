package com.example.qard_hasan_for_education.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

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

    @Override
    public String toString() {
        return "SimpleBankInfo{" +
                "accountNumber='" + accountNumber + '\'' +
                ", bankName='" + bankName + '\'' +
                ", accountHolderName='" + accountHolderName + '\'' +
                ", currentBalance=" + currentBalance +
                ", purchasingPower=" + purchasingPower +
                '}';
    }
}