// ApplicationController.java
package com.example.qard_hasan_for_education.controller;


//import com.example.qard_hasan_for_education.model.ApplicationRiskProfile;
//import com.example.qard_hasan_for_education.model.StudentApplicationData;
//import com.example.qard_hasan_for_education.service.DocumentOrchestrationService;
import com.example.qard_hasan_for_education.model.ApplicationRiskProfile;
import com.example.qard_hasan_for_education.model.ApplicationStatus;
import com.example.qard_hasan_for_education.model.ApprovalRecommendation;
import com.example.qard_hasan_for_education.model.RiskLevel;
import com.example.qard_hasan_for_education.model.StudentApplicationData;
import com.example.qard_hasan_for_education.service.DocumentOrchestrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    @Autowired
    private DocumentOrchestrationService orchestrationService;

    @PostMapping("/submit-complete")
    public ResponseEntity<?> submitCompleteApplication(
            @RequestParam("studentId") String studentId,
            @RequestParam("requestedAmount") BigDecimal requestedAmount,
            @RequestParam("bankStatement") MultipartFile bankStatement,
            @RequestParam("universityLetter") MultipartFile universityLetter,
            @RequestParam("scholarshipLetter") MultipartFile scholarshipLetter,
            @RequestParam("passportImage") MultipartFile passportImage) {

        logger.info("Received complete application submission for student: {}", studentId);

        try {
            // Validate all files first
            orchestrationService.validateFiles(bankStatement, universityLetter, scholarshipLetter, passportImage);

            // Process the complete application with risk assessment
            StudentApplicationData result = orchestrationService.processCompleteApplication(
                    studentId, bankStatement, universityLetter, scholarshipLetter, passportImage);

            logger.info("Complete application processed successfully for student: {}, applicationId: {}, riskLevel: {}",
                    studentId, result.getApplicationId(),
                    result.getRiskProfile() != null ? result.getRiskProfile().getOverallRisk() : "N/A");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error processing complete application for student: {}", studentId, e);
            return ResponseEntity.status(500)
                    .body("Error processing complete application: " + e.getMessage());
        }
    }

    @GetMapping("/status/{applicationId}")
    public ResponseEntity<?> getApplicationStatus(@PathVariable String applicationId) {
        try {
            StudentApplicationData application = orchestrationService.getApplicationStatus(applicationId);

            if (application == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(application);

        } catch (Exception e) {
            logger.error("Error retrieving application status for: {}", applicationId, e);
            return ResponseEntity.status(500)
                    .body("Error retrieving application status: " + e.getMessage());
        }
    }

    @GetMapping("/risk-profile/{applicationId}")
    public ResponseEntity<?> getApplicationRiskProfile(@PathVariable String applicationId) {
        try {
            ApplicationRiskProfile riskProfile = orchestrationService.getApplicationRiskProfile(applicationId);

            if (riskProfile == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(riskProfile);

        } catch (Exception e) {
            logger.error("Error retrieving risk profile for: {}", applicationId, e);
            return ResponseEntity.status(500)
                    .body("Error retrieving risk profile: " + e.getMessage());
        }
    }

    @PostMapping("/reassess-risk/{applicationId}")
    public ResponseEntity<?> reassessRisk(@PathVariable String applicationId) {
        try {
            ApplicationRiskProfile newRiskProfile = orchestrationService.reassessRisk(applicationId);
            return ResponseEntity.ok(newRiskProfile);

        } catch (Exception e) {
            logger.error("Error reassessing risk for: {}", applicationId, e);
            return ResponseEntity.status(500)
                    .body("Error reassessing risk: " + e.getMessage());
        }
    }

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Application Processing API with Risk Assessment is running!");
    }

    // Endpoint to get application summary with risk info
    @GetMapping("/summary/{applicationId}")
    public ResponseEntity<?> getApplicationSummary(@PathVariable String applicationId) {
        try {
            StudentApplicationData application = orchestrationService.getApplicationStatus(applicationId);

            if (application == null) {
                return ResponseEntity.notFound().build();
            }

            // Create a summary response with key information
            ApplicationSummary summary = new ApplicationSummary();
            summary.setApplicationId(application.getApplicationId());
            summary.setStudentId(application.getStudentId());
            summary.setStatus(application.getStatus());
            summary.setSubmissionDate(application.getSubmissionDate());
            summary.setProcessingTimeMs(application.getProcessingTimeMs());

            if (application.getRiskProfile() != null) {
                summary.setRiskLevel(application.getRiskProfile().getOverallRisk());
                summary.setRiskScore(application.getRiskProfile().getRiskScore());
                summary.setApprovalRecommendation(application.getRiskProfile().getApprovalRecommendation());
            }

            if (application.getPassportInfo() != null) {
                summary.setStudentName(application.getPassportInfo().getFullName());
            }

            if (application.getUniversityAcceptance() != null) {
                summary.setUniversityName(application.getUniversityAcceptance().getUniversityName());
                summary.setProgram(application.getUniversityAcceptance().getProgram());
            }

            return ResponseEntity.ok(summary);

        } catch (Exception e) {
            logger.error("Error retrieving application summary for: {}", applicationId, e);
            return ResponseEntity.status(500)
                    .body("Error retrieving application summary: " + e.getMessage());
        }
    }

    // Inner class for application summary
    public static class ApplicationSummary {
        private String applicationId;
        private String studentId;
        private String studentName;
        private String universityName;
        private String program;
        private ApplicationStatus status;
        private java.time.LocalDateTime submissionDate;
        private Long processingTimeMs;
        private RiskLevel riskLevel;
        private Integer riskScore;
        private ApprovalRecommendation approvalRecommendation;

        // Getters and setters
        public String getApplicationId() { return applicationId; }
        public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public String getUniversityName() { return universityName; }
        public void setUniversityName(String universityName) { this.universityName = universityName; }

        public String getProgram() { return program; }
        public void setProgram(String program) { this.program = program; }

        public ApplicationStatus getStatus() { return status; }
        public void setStatus(ApplicationStatus status) { this.status = status; }

        public java.time.LocalDateTime getSubmissionDate() { return submissionDate; }
        public void setSubmissionDate(java.time.LocalDateTime submissionDate) { this.submissionDate = submissionDate; }

        public Long getProcessingTimeMs() { return processingTimeMs; }
        public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }

        public RiskLevel getRiskLevel() { return riskLevel; }
        public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

        public Integer getRiskScore() { return riskScore; }
        public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }

        public ApprovalRecommendation getApprovalRecommendation() { return approvalRecommendation; }
        public void setApprovalRecommendation(ApprovalRecommendation approvalRecommendation) { this.approvalRecommendation = approvalRecommendation; }
    }
}