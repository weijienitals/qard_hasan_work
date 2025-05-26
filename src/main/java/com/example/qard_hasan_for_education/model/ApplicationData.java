package com.example.qard_hasan_for_education.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class ApplicationData {
    @JsonProperty("applicationId")
    private String applicationId;

    @JsonProperty("studentId")
    private String studentId;

    @JsonProperty("applicationStatus")
    private ApplicationStatus applicationStatus;

    @JsonProperty("submissionDate")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime submissionDate;

    @JsonProperty("lastUpdated")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastUpdated;

    // Document processing status
    @JsonProperty("documentsProcessed")
    private DocumentProcessingStatus documentsProcessed;

    // Aggregated data from all documents
    @JsonProperty("personalInfo")
    private PersonalInfo personalInfo;

    @JsonProperty("financialInfo")
    private FinancialInfo financialInfo;

    @JsonProperty("academicInfo")
    private AcademicInfo academicInfo;

    @JsonProperty("fundingRequirement")
    private FundingRequirement fundingRequirement;

    @JsonProperty("riskAssessment")
    private RiskAssessment riskAssessment;

    @JsonProperty("notes")
    private List<String> notes;

    // Constructors
    public ApplicationData() {
        this.applicationId = java.util.UUID.randomUUID().toString();
        this.applicationStatus = ApplicationStatus.DRAFT;
        this.submissionDate = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.documentsProcessed = new DocumentProcessingStatus();
        this.personalInfo = new PersonalInfo();
        this.financialInfo = new FinancialInfo();
        this.academicInfo = new AcademicInfo();
        this.fundingRequirement = new FundingRequirement();
        this.riskAssessment = new RiskAssessment();
        this.notes = new ArrayList<>();
    }

    // Nested classes for organized data structure
    public static class DocumentProcessingStatus {
        @JsonProperty("passportProcessed")
        private boolean passportProcessed = false;

        @JsonProperty("bankStatementProcessed")
        private boolean bankStatementProcessed = false;

        @JsonProperty("universityLetterProcessed")
        private boolean universityLetterProcessed = false;

        @JsonProperty("scholarshipLetterProcessed")
        private boolean scholarshipLetterProcessed = false;

        @JsonProperty("allDocumentsProcessed")
        private boolean allDocumentsProcessed = false;

        // Getters and setters
        public boolean isPassportProcessed() { return passportProcessed; }
        public void setPassportProcessed(boolean passportProcessed) {
            this.passportProcessed = passportProcessed;
            updateAllDocumentsStatus();
        }

        public boolean isBankStatementProcessed() { return bankStatementProcessed; }
        public void setBankStatementProcessed(boolean bankStatementProcessed) {
            this.bankStatementProcessed = bankStatementProcessed;
            updateAllDocumentsStatus();
        }

        public boolean isUniversityLetterProcessed() { return universityLetterProcessed; }
        public void setUniversityLetterProcessed(boolean universityLetterProcessed) {
            this.universityLetterProcessed = universityLetterProcessed;
            updateAllDocumentsStatus();
        }

        public boolean isScholarshipLetterProcessed() { return scholarshipLetterProcessed; }
        public void setScholarshipLetterProcessed(boolean scholarshipLetterProcessed) {
            this.scholarshipLetterProcessed = scholarshipLetterProcessed;
            updateAllDocumentsStatus();
        }

        public boolean isAllDocumentsProcessed() { return allDocumentsProcessed; }
        public void setAllDocumentsProcessed(boolean allDocumentsProcessed) { this.allDocumentsProcessed = allDocumentsProcessed; }

        public void updateAllDocumentsStatus() {
            // For Qard Hasan, we need at least passport, bank, and university (scholarship is optional)
            this.allDocumentsProcessed = passportProcessed && bankStatementProcessed && universityLetterProcessed;
        }
    }

    public static class PersonalInfo {
        @JsonProperty("fullName")
        private String fullName;

        @JsonProperty("passportNumber")
        private String passportNumber;

        @JsonProperty("nationality")
        private String nationality;

        @JsonProperty("dateOfBirth")
        private String dateOfBirth;

        @JsonProperty("gender")
        private String gender;

        @JsonProperty("passportExpiryDate")
        private String passportExpiryDate;

        // Getters and setters
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getPassportNumber() { return passportNumber; }
        public void setPassportNumber(String passportNumber) { this.passportNumber = passportNumber; }

        public String getNationality() { return nationality; }
        public void setNationality(String nationality) { this.nationality = nationality; }

        public String getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public String getPassportExpiryDate() { return passportExpiryDate; }
        public void setPassportExpiryDate(String passportExpiryDate) { this.passportExpiryDate = passportExpiryDate; }
    }

    public static class FinancialInfo {
        @JsonProperty("accountNumber")
        private String accountNumber;

        @JsonProperty("bankName")
        private String bankName;

        @JsonProperty("accountHolderName")
        private String accountHolderName;

        @JsonProperty("currentBalance")
        private BigDecimal currentBalance;

        @JsonProperty("hasScholarship")
        private boolean hasScholarship = false;

        @JsonProperty("scholarshipAmount")
        private BigDecimal scholarshipAmount;

        @JsonProperty("scholarshipProvider")
        private String scholarshipProvider;

        @JsonProperty("financialGap")
        private BigDecimal financialGap;

        // Getters and setters
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

        public String getBankName() { return bankName; }
        public void setBankName(String bankName) { this.bankName = bankName; }

        public String getAccountHolderName() { return accountHolderName; }
        public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }

        public BigDecimal getCurrentBalance() { return currentBalance; }
        public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }

        public boolean isHasScholarship() { return hasScholarship; }
        public void setHasScholarship(boolean hasScholarship) { this.hasScholarship = hasScholarship; }

        public BigDecimal getScholarshipAmount() { return scholarshipAmount; }
        public void setScholarshipAmount(BigDecimal scholarshipAmount) {
            this.scholarshipAmount = scholarshipAmount;
            this.hasScholarship = scholarshipAmount != null && scholarshipAmount.compareTo(BigDecimal.ZERO) > 0;
        }

        public String getScholarshipProvider() { return scholarshipProvider; }
        public void setScholarshipProvider(String scholarshipProvider) { this.scholarshipProvider = scholarshipProvider; }

        public BigDecimal getFinancialGap() { return financialGap; }
        public void setFinancialGap(BigDecimal financialGap) { this.financialGap = financialGap; }
    }

    public static class AcademicInfo {
        @JsonProperty("universityName")
        private String universityName;

        @JsonProperty("program")
        private String program;

        @JsonProperty("acceptanceDate")
        private String acceptanceDate;

        @JsonProperty("semesterStart")
        private String semesterStart;

        @JsonProperty("scholarshipName")
        private String scholarshipName;

        @JsonProperty("academicYear")
        private String academicYear;

        // Getters and setters
        public String getUniversityName() { return universityName; }
        public void setUniversityName(String universityName) { this.universityName = universityName; }

        public String getProgram() { return program; }
        public void setProgram(String program) { this.program = program; }

        public String getAcceptanceDate() { return acceptanceDate; }
        public void setAcceptanceDate(String acceptanceDate) { this.acceptanceDate = acceptanceDate; }

        public String getSemesterStart() { return semesterStart; }
        public void setSemesterStart(String semesterStart) { this.semesterStart = semesterStart; }

        public String getScholarshipName() { return scholarshipName; }
        public void setScholarshipName(String scholarshipName) { this.scholarshipName = scholarshipName; }

        public String getAcademicYear() { return academicYear; }
        public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    }

    public static class FundingRequirement {
        @JsonProperty("estimatedTransitionCost")
        private BigDecimal estimatedTransitionCost;

        @JsonProperty("recommendedFunding")
        private BigDecimal recommendedFunding;

        @JsonProperty("fundingType")
        private FundingType fundingType;

        @JsonProperty("urgencyLevel")
        private UrgencyLevel urgencyLevel;

        // Getters and setters
        public BigDecimal getEstimatedTransitionCost() { return estimatedTransitionCost; }
        public void setEstimatedTransitionCost(BigDecimal estimatedTransitionCost) { this.estimatedTransitionCost = estimatedTransitionCost; }

        public BigDecimal getRecommendedFunding() { return recommendedFunding; }
        public void setRecommendedFunding(BigDecimal recommendedFunding) { this.recommendedFunding = recommendedFunding; }

        public FundingType getFundingType() { return fundingType; }
        public void setFundingType(FundingType fundingType) { this.fundingType = fundingType; }

        public UrgencyLevel getUrgencyLevel() { return urgencyLevel; }
        public void setUrgencyLevel(UrgencyLevel urgencyLevel) { this.urgencyLevel = urgencyLevel; }
    }

    public static class RiskAssessment {
        @JsonProperty("overallRisk")
        private RiskLevel overallRisk;

        @JsonProperty("repaymentCapability")
        private RiskLevel repaymentCapability;

        @JsonProperty("academicRisk")
        private RiskLevel academicRisk;

        @JsonProperty("financialRisk")
        private RiskLevel financialRisk;

        @JsonProperty("riskFactors")
        private List<String> riskFactors = new ArrayList<>();

        // Getters and setters
        public RiskLevel getOverallRisk() { return overallRisk; }
        public void setOverallRisk(RiskLevel overallRisk) { this.overallRisk = overallRisk; }

        public RiskLevel getRepaymentCapability() { return repaymentCapability; }
        public void setRepaymentCapability(RiskLevel repaymentCapability) { this.repaymentCapability = repaymentCapability; }

        public RiskLevel getAcademicRisk() { return academicRisk; }
        public void setAcademicRisk(RiskLevel academicRisk) { this.academicRisk = academicRisk; }

        public RiskLevel getFinancialRisk() { return financialRisk; }
        public void setFinancialRisk(RiskLevel financialRisk) { this.financialRisk = financialRisk; }

        public List<String> getRiskFactors() { return riskFactors; }
        public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }
    }

    // Enums
    public enum ApplicationStatus {
        DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, PENDING_DOCUMENTS
    }

    public enum FundingType {
        TRANSITION_ONLY, TRANSITION_PLUS_GAP, FULL_PROGRAM, EMERGENCY_COMPLETION
    }

    public enum UrgencyLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum RiskLevel {
        LOW, MEDIUM, HIGH, VERY_HIGH
    }

    // Main getters and setters
    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public ApplicationStatus getApplicationStatus() { return applicationStatus; }
    public void setApplicationStatus(ApplicationStatus applicationStatus) { this.applicationStatus = applicationStatus; }

    public LocalDateTime getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDateTime submissionDate) { this.submissionDate = submissionDate; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public DocumentProcessingStatus getDocumentsProcessed() { return documentsProcessed; }
    public void setDocumentsProcessed(DocumentProcessingStatus documentsProcessed) { this.documentsProcessed = documentsProcessed; }

    public PersonalInfo getPersonalInfo() { return personalInfo; }
    public void setPersonalInfo(PersonalInfo personalInfo) { this.personalInfo = personalInfo; }

    public FinancialInfo getFinancialInfo() { return financialInfo; }
    public void setFinancialInfo(FinancialInfo financialInfo) { this.financialInfo = financialInfo; }

    public AcademicInfo getAcademicInfo() { return academicInfo; }
    public void setAcademicInfo(AcademicInfo academicInfo) { this.academicInfo = academicInfo; }

    public FundingRequirement getFundingRequirement() { return fundingRequirement; }
    public void setFundingRequirement(FundingRequirement fundingRequirement) { this.fundingRequirement = fundingRequirement; }

    public RiskAssessment getRiskAssessment() { return riskAssessment; }
    public void setRiskAssessment(RiskAssessment riskAssessment) { this.riskAssessment = riskAssessment; }

    public List<String> getNotes() { return notes; }
    public void setNotes(List<String> notes) { this.notes = notes; }

    public void addNote(String note) {
        if (this.notes == null) {
            this.notes = new ArrayList<>();
        }
        this.notes.add(LocalDateTime.now() + ": " + note);
    }

    @Override
    public String toString() {
        return "ApplicationData{" +
                "applicationId='" + applicationId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", applicationStatus=" + applicationStatus +
                ", submissionDate=" + submissionDate +
                ", lastUpdated=" + lastUpdated +
                ", documentsProcessed=" + documentsProcessed +
                ", personalInfo=" + personalInfo +
                ", financialInfo=" + financialInfo +
                ", academicInfo=" + academicInfo +
                ", fundingRequirement=" + fundingRequirement +
                ", riskAssessment=" + riskAssessment +
                ", notes=" + notes +
                '}';
    }
}