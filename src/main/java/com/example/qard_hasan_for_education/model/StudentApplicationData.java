package com.example.qard_hasan_for_education.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class StudentApplicationData {
    @JsonProperty("applicationId")
    private String applicationId;

    @JsonProperty("studentId")
    private String studentId;

    @JsonProperty("submissionDate")
    private LocalDateTime submissionDate;

    @JsonProperty("bankInfo")
    private SimpleBankInfo bankInfo;

    @JsonProperty("universityAcceptance")
    private UniversityAcceptance universityAcceptance;

    @JsonProperty("scholarshipAcceptance")
    private ScholarshipAcceptance scholarshipAcceptance;

    @JsonProperty("passportInfo")
    private PassportInfo passportInfo;

    @JsonProperty("status")
    private ApplicationStatus status;

    @JsonProperty("processingStartTime")
    private LocalDateTime processingStartTime;

    @JsonProperty("processingEndTime")
    private LocalDateTime processingEndTime;

    @JsonProperty("processingTimeMs")
    private Long processingTimeMs;

    // Constructors
    public StudentApplicationData() {}

    public StudentApplicationData(String applicationId, String studentId) {
        this.applicationId = applicationId;
        this.studentId = studentId;
        this.submissionDate = LocalDateTime.now();
        this.status = ApplicationStatus.DOCUMENTS_RECEIVED;
    }

    // Getters and Setters
    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public LocalDateTime getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDateTime submissionDate) { this.submissionDate = submissionDate; }

    public SimpleBankInfo getBankInfo() { return bankInfo; }
    public void setBankInfo(SimpleBankInfo bankInfo) { this.bankInfo = bankInfo; }

    public UniversityAcceptance getUniversityAcceptance() { return universityAcceptance; }
    public void setUniversityAcceptance(UniversityAcceptance universityAcceptance) { this.universityAcceptance = universityAcceptance; }

    public ScholarshipAcceptance getScholarshipAcceptance() { return scholarshipAcceptance; }
    public void setScholarshipAcceptance(ScholarshipAcceptance scholarshipAcceptance) { this.scholarshipAcceptance = scholarshipAcceptance; }

    public PassportInfo getPassportInfo() { return passportInfo; }
    public void setPassportInfo(PassportInfo passportInfo) { this.passportInfo = passportInfo; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public LocalDateTime getProcessingStartTime() { return processingStartTime; }
    public void setProcessingStartTime(LocalDateTime processingStartTime) { this.processingStartTime = processingStartTime; }

    public LocalDateTime getProcessingEndTime() { return processingEndTime; }
    public void setProcessingEndTime(LocalDateTime processingEndTime) { this.processingEndTime = processingEndTime; }

    public Long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }

    // Utility methods
    public boolean isDocumentProcessingComplete() {
        return bankInfo != null && universityAcceptance != null &&
                scholarshipAcceptance != null && passportInfo != null;
    }

    public void calculateProcessingTime() {
        if (processingStartTime != null && processingEndTime != null) {
            this.processingTimeMs = java.time.Duration.between(processingStartTime, processingEndTime).toMillis();
        }
    }

    @Override
    public String toString() {
        return "StudentApplicationData{" +
                "applicationId='" + applicationId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", status=" + status +
                ", submissionDate=" + submissionDate +
                ", processingTimeMs=" + processingTimeMs +
                '}';
    }
}