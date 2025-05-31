package com.example.qard_hasan_for_education.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class LoanAccount {
    @JsonProperty("loanId")
    private String loanId;

    @JsonProperty("studentId")
    private String studentId;

    @JsonProperty("applicationId")
    private String applicationId;

    @JsonProperty("principalAmount")
    private BigDecimal principalAmount;

    @JsonProperty("remainingBalance")
    private BigDecimal remainingBalance;

    @JsonProperty("monthlyInstallment")
    private BigDecimal monthlyInstallment;

    @JsonProperty("totalInstallments")
    private Integer totalInstallments;

    @JsonProperty("completedInstallments")
    private Integer completedInstallments;

    @JsonProperty("loanStartDate")
    private LocalDate loanStartDate;

    @JsonProperty("nextPaymentDate")
    private LocalDate nextPaymentDate;

    @JsonProperty("loanStatus")
    private LoanStatus loanStatus;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    // Student information for mentoring eligibility
    @JsonProperty("studentName")
    private String studentName;

    @JsonProperty("universityName")
    private String universityName;

    @JsonProperty("program")
    private String program;

    @JsonProperty("universityCountry")
    private String universityCountry;

    @JsonProperty("nationality")
    private String nationality;

    // Constructors
    public LoanAccount() {}

    public LoanAccount(String studentId, String applicationId, BigDecimal principalAmount,
                       Integer totalInstallments, String studentName, String universityName,
                       String program, String universityCountry, String nationality) {
        this.loanId = generateLoanId();
        this.studentId = studentId;
        this.applicationId = applicationId;
        this.principalAmount = principalAmount;
        this.remainingBalance = principalAmount;
        this.totalInstallments = totalInstallments;
        this.completedInstallments = 0;
        this.monthlyInstallment = principalAmount.divide(BigDecimal.valueOf(totalInstallments), 2, BigDecimal.ROUND_HALF_UP);
        this.loanStartDate = LocalDate.now();
        this.nextPaymentDate = LocalDate.now().plusMonths(1);
        this.loanStatus = LoanStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.studentName = studentName;
        this.universityName = universityName;
        this.program = program;
        this.universityCountry = universityCountry;
        this.nationality = nationality;
    }

    // Getters and Setters
    public String getLoanId() { return loanId; }
    public void setLoanId(String loanId) { this.loanId = loanId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal principalAmount) { this.principalAmount = principalAmount; }

    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }

    public BigDecimal getMonthlyInstallment() { return monthlyInstallment; }
    public void setMonthlyInstallment(BigDecimal monthlyInstallment) { this.monthlyInstallment = monthlyInstallment; }

    public Integer getTotalInstallments() { return totalInstallments; }
    public void setTotalInstallments(Integer totalInstallments) { this.totalInstallments = totalInstallments; }

    public Integer getCompletedInstallments() { return completedInstallments; }
    public void setCompletedInstallments(Integer completedInstallments) { this.completedInstallments = completedInstallments; }

    public LocalDate getLoanStartDate() { return loanStartDate; }
    public void setLoanStartDate(LocalDate loanStartDate) { this.loanStartDate = loanStartDate; }

    public LocalDate getNextPaymentDate() { return nextPaymentDate; }
    public void setNextPaymentDate(LocalDate nextPaymentDate) { this.nextPaymentDate = nextPaymentDate; }

    public LoanStatus getLoanStatus() { return loanStatus; }
    public void setLoanStatus(LoanStatus loanStatus) { this.loanStatus = loanStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getUniversityName() { return universityName; }
    public void setUniversityName(String universityName) { this.universityName = universityName; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public String getUniversityCountry() { return universityCountry; }
    public void setUniversityCountry(String universityCountry) { this.universityCountry = universityCountry; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    // Utility methods
    public boolean isEligibleForMentoring() {
        return "Indonesian".equalsIgnoreCase(nationality) &&
                universityCountry != null &&
                !universityCountry.equalsIgnoreCase("Indonesia");
    }

    public BigDecimal getRepaymentProgress() {
        if (totalInstallments == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(completedInstallments)
                .divide(BigDecimal.valueOf(totalInstallments), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private String generateLoanId() {
        return "LOAN_" + System.currentTimeMillis() + "_" +
                java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    public String toString() {
        return "LoanAccount{" +
                "loanId='" + loanId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", principalAmount=" + principalAmount +
                ", remainingBalance=" + remainingBalance +
                ", completedInstallments=" + completedInstallments +
                ", totalInstallments=" + totalInstallments +
                ", loanStatus=" + loanStatus +
                '}';
    }
}