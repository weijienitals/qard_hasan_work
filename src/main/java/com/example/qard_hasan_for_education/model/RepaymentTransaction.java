// RepaymentTransaction.java
package com.example.qard_hasan_for_education.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RepaymentTransaction {
    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("loanId")
    private String loanId;

    @JsonProperty("studentId")
    private String studentId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("paymentDate")
    private LocalDateTime paymentDate;

    @JsonProperty("installmentNumber")
    private Integer installmentNumber;

    @JsonProperty("paymentMethod")
    private String paymentMethod;

    @JsonProperty("status")
    private PaymentStatus status;

    @JsonProperty("referenceNumber")
    private String referenceNumber;

    @JsonProperty("mentoringOfferSent")
    private boolean mentoringOfferSent;

    @JsonProperty("mentoringOfferAccepted")
    private Boolean mentoringOfferAccepted;

    // Constructors
    public RepaymentTransaction() {}

    public RepaymentTransaction(String loanId, String studentId, BigDecimal amount,
                                Integer installmentNumber, String paymentMethod) {
        this.transactionId = generateTransactionId();
        this.loanId = loanId;
        this.studentId = studentId;
        this.amount = amount;
        this.installmentNumber = installmentNumber;
        this.paymentMethod = paymentMethod;
        this.paymentDate = LocalDateTime.now();
        this.status = PaymentStatus.PENDING;
        this.referenceNumber = generateReferenceNumber();
        this.mentoringOfferSent = false;
    }

    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getLoanId() { return loanId; }
    public void setLoanId(String loanId) { this.loanId = loanId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public Integer getInstallmentNumber() { return installmentNumber; }
    public void setInstallmentNumber(Integer installmentNumber) { this.installmentNumber = installmentNumber; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public boolean isMentoringOfferSent() { return mentoringOfferSent; }
    public void setMentoringOfferSent(boolean mentoringOfferSent) { this.mentoringOfferSent = mentoringOfferSent; }

    public Boolean getMentoringOfferAccepted() { return mentoringOfferAccepted; }
    public void setMentoringOfferAccepted(Boolean mentoringOfferAccepted) { this.mentoringOfferAccepted = mentoringOfferAccepted; }

    private String generateTransactionId() {
        return "TXN_" + System.currentTimeMillis() + "_" +
                java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String generateReferenceNumber() {
        return "REF" + System.currentTimeMillis();
    }
}