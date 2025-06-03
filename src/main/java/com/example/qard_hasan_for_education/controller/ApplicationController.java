// ApplicationController.java - DEBUG VERSION
package com.example.qard_hasan_for_education.controller;

import com.example.qard_hasan_for_education.model.riskAnalysis.ApplicationRiskProfile;
import com.example.qard_hasan_for_education.model.ApplicationStatus;
import com.example.qard_hasan_for_education.model.ApprovalRecommendation;
import com.example.qard_hasan_for_education.model.riskAnalysis.RiskLevel;
import com.example.qard_hasan_for_education.model.StudentApplicationData;
import com.example.qard_hasan_for_education.service.DocumentOrchestrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    @Autowired
    private DocumentOrchestrationService orchestrationService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * DEBUG: Test endpoint to verify controller is working
     */
    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        logger.info("=== TEST ENDPOINT CALLED ===");
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "ApplicationController is working!");
        response.put("timestamp", java.time.LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    /**
     * Submit complete application
     */
    @PostMapping("/submit-complete")
    public ResponseEntity<?> submitCompleteApplication(
            @RequestParam("studentId") String studentId,
            @RequestParam("bankStatement") MultipartFile bankStatement,
            @RequestParam("universityLetter") MultipartFile universityLetter,
            @RequestParam("scholarshipLetter") MultipartFile scholarshipLetter,
            @RequestParam("passportImage") MultipartFile passportImage) {

        logger.info("=== SUBMIT COMPLETE APPLICATION CALLED === Student: {}", studentId);

        try {
            // Validate all files first
            orchestrationService.validateFiles(bankStatement, universityLetter, scholarshipLetter, passportImage);

            // Process the complete application with risk assessment
            StudentApplicationData result = orchestrationService.processCompleteApplication(
                    studentId, bankStatement, universityLetter, scholarshipLetter, passportImage);

            logger.info("Complete application processed successfully for student: {}, applicationId: {}, riskLevel: {}",
                    studentId, result.getApplicationId(),
                    result.getRiskProfile() != null ? result.getRiskProfile().getOverallRisk() : "N/A");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Application processed successfully");
            response.put("application", result);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error processing complete application for student: {}", studentId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error processing complete application: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get application status - POST with request body (as requested by boss)
     */
    @PostMapping("/status")
    public ResponseEntity<?> getApplicationStatus(@RequestBody ApplicationStatusRequest request) {
        logger.info("=== GET APPLICATION STATUS CALLED === ApplicationId: {}", request.getApplicationId());

        try {
            if (request.getApplicationId() == null || request.getApplicationId().trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Application ID is required");
                return ResponseEntity.badRequest().body(response);
            }

            StudentApplicationData application = getApplicationWithFix(request.getApplicationId());

            Map<String, Object> response = new HashMap<>();
            if (application == null) {
                response.put("success", false);
                response.put("message", "Application not found: " + request.getApplicationId());
                return ResponseEntity.status(404).body(response);
            }

            response.put("success", true);
            response.put("message", "Application status retrieved successfully");
            response.put("application", application);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving application status for: {}", request.getApplicationId(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving application status: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get application risk profile - POST with request body (as requested by boss)
     */
    @PostMapping("/risk-profile")
    public ResponseEntity<?> getApplicationRiskProfile(@RequestBody RiskProfileRequest request) {
        logger.info("=== GET RISK PROFILE CALLED === ApplicationId: {}", request.getApplicationId());

        try {
            if (request.getApplicationId() == null || request.getApplicationId().trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Application ID is required");
                return ResponseEntity.badRequest().body(response);
            }

            ApplicationRiskProfile riskProfile = orchestrationService.getApplicationRiskProfile(request.getApplicationId());

            Map<String, Object> response = new HashMap<>();
            if (riskProfile == null) {
                response.put("success", false);
                response.put("message", "Risk profile not found for application: " + request.getApplicationId());
                return ResponseEntity.status(404).body(response);
            }

            response.put("success", true);
            response.put("message", "Risk profile retrieved successfully");
            response.put("riskProfile", riskProfile);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving risk profile for: {}", request.getApplicationId(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving risk profile: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Reassess risk - POST with request body
     */
    @PostMapping("/reassess-risk")
    public ResponseEntity<?> reassessRisk(@RequestBody ReassessRiskRequest request) {
        logger.info("=== REASSESS RISK CALLED === ApplicationId: {}", request.getApplicationId());

        try {
            if (request.getApplicationId() == null || request.getApplicationId().trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Application ID is required");
                return ResponseEntity.badRequest().body(response);
            }

            ApplicationRiskProfile newRiskProfile = orchestrationService.reassessRisk(request.getApplicationId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Risk reassessment completed successfully");
            response.put("riskProfile", newRiskProfile);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error reassessing risk for: {}", request.getApplicationId(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error reassessing risk: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get application summary - POST with request body (as requested by boss)
     */
    @PostMapping("/summary")
    public ResponseEntity<?> getApplicationSummary(@RequestBody ApplicationSummaryRequest request) {
        logger.info("=== GET APPLICATION SUMMARY CALLED === ApplicationId: {}", request.getApplicationId());

        try {
            if (request.getApplicationId() == null || request.getApplicationId().trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Application ID is required");
                return ResponseEntity.badRequest().body(response);
            }

            StudentApplicationData application = getApplicationWithFix(request.getApplicationId());

            Map<String, Object> response = new HashMap<>();
            if (application == null) {
                response.put("success", false);
                response.put("message", "Application not found: " + request.getApplicationId());
                return ResponseEntity.status(404).body(response);
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

            response.put("success", true);
            response.put("message", "Application summary retrieved successfully");
            response.put("summary", summary);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving application summary for: {}", request.getApplicationId(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving application summary: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Helper method to get application with LinkedHashMap conversion fix
     */
    private StudentApplicationData getApplicationWithFix(String applicationId) {
        try {
            String key = "application:" + applicationId;
            Object result = redisTemplate.opsForValue().get(key);

            if (result == null) {
                logger.error("Application not found in Redis: {}", applicationId);
                return null;
            }

            // Handle LinkedHashMap to StudentApplicationData conversion
            if (result instanceof java.util.LinkedHashMap) {
                logger.info("Converting LinkedHashMap to StudentApplicationData for: {}", applicationId);
                return convertMapToStudentApplicationData((java.util.LinkedHashMap<String, Object>) result);
            } else if (result instanceof StudentApplicationData) {
                return (StudentApplicationData) result;
            }

            logger.error("Unable to convert Redis object to StudentApplicationData: {}", result.getClass().getName());
            return null;

        } catch (Exception e) {
            logger.error("Exception retrieving application: {}", applicationId, e);
            return null;
        }
    }

    /**
     * Helper method to convert LinkedHashMap to StudentApplicationData
     */
    @SuppressWarnings("unchecked")
    private StudentApplicationData convertMapToStudentApplicationData(Map<String, Object> map) {
        try {
            StudentApplicationData app = new StudentApplicationData();

            // Set basic fields
            app.setApplicationId((String) map.get("applicationId"));
            app.setStudentId((String) map.get("studentId"));

            // Handle enum field
            if (map.get("status") != null) {
                app.setStatus(ApplicationStatus.valueOf(map.get("status").toString()));
            }

            // Handle Long field
            if (map.get("processingTimeMs") != null) {
                app.setProcessingTimeMs(((Number) map.get("processingTimeMs")).longValue());
            }

            // Handle LocalDateTime fields (they come as [year, month, day, hour, minute, second, nano] arrays)
            if (map.get("submissionDate") instanceof List) {
                List<Integer> dateTimeList = (List<Integer>) map.get("submissionDate");
                app.setSubmissionDate(java.time.LocalDateTime.of(
                        dateTimeList.get(0), dateTimeList.get(1), dateTimeList.get(2),
                        dateTimeList.get(3), dateTimeList.get(4), dateTimeList.get(5),
                        dateTimeList.size() > 6 ? dateTimeList.get(6) : 0
                ));
            }

            if (map.get("processingStartTime") instanceof List) {
                List<Integer> dateTimeList = (List<Integer>) map.get("processingStartTime");
                app.setProcessingStartTime(java.time.LocalDateTime.of(
                        dateTimeList.get(0), dateTimeList.get(1), dateTimeList.get(2),
                        dateTimeList.get(3), dateTimeList.get(4), dateTimeList.get(5),
                        dateTimeList.size() > 6 ? dateTimeList.get(6) : 0
                ));
            }

            if (map.get("processingEndTime") instanceof List) {
                List<Integer> dateTimeList = (List<Integer>) map.get("processingEndTime");
                app.setProcessingEndTime(java.time.LocalDateTime.of(
                        dateTimeList.get(0), dateTimeList.get(1), dateTimeList.get(2),
                        dateTimeList.get(3), dateTimeList.get(4), dateTimeList.get(5),
                        dateTimeList.size() > 6 ? dateTimeList.get(6) : 0
                ));
            }

            // Handle nested objects - these would need their own converters
            // For now, we'll set them as null and log
            if (map.get("bankInfo") != null) {
                logger.info("BankInfo found but conversion not implemented yet");
            }
            if (map.get("universityAcceptance") != null) {
                logger.info("UniversityAcceptance found but conversion not implemented yet");
            }
            if (map.get("scholarshipAcceptance") != null) {
                logger.info("ScholarshipAcceptance found but conversion not implemented yet");
            }
            if (map.get("passportInfo") != null) {
                logger.info("PassportInfo found but conversion not implemented yet");
            }
            if (map.get("riskProfile") != null) {
                logger.info("RiskProfile found but conversion not implemented yet");
            }

            return app;

        } catch (Exception e) {
            logger.error("Error converting map to StudentApplicationData", e);
            return null;
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        logger.info("=== HEALTH CHECK CALLED ===");
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Application Processing API with Risk Assessment is running!");
        response.put("timestamp", java.time.LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    /**
     * DEBUG: Check what applications exist in Redis
     */
    @GetMapping("/debug/list-applications")
    public ResponseEntity<?> listApplications() {
        try {
            logger.info("=== LISTING ALL APPLICATIONS IN REDIS ===");

            // Get all application keys from Redis
            Set<String> applicationKeys = redisTemplate.keys("application:*");
            Set<String> statusKeys = redisTemplate.keys("status:*");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("applicationKeys", applicationKeys);
            response.put("statusKeys", statusKeys);
            response.put("totalApplications", applicationKeys != null ? applicationKeys.size() : 0);
            response.put("totalStatuses", statusKeys != null ? statusKeys.size() : 0);

            // Get details of each application
            if (applicationKeys != null && !applicationKeys.isEmpty()) {
                Map<String, Object> applicationDetails = new HashMap<>();
                for (String key : applicationKeys) {
                    try {
                        Object app = redisTemplate.opsForValue().get(key);
                        if (app instanceof StudentApplicationData) {
                            StudentApplicationData appData = (StudentApplicationData) app;
                            Map<String, Object> appInfo = new HashMap<>();
                            appInfo.put("applicationId", appData.getApplicationId());
                            appInfo.put("studentId", appData.getStudentId());
                            appInfo.put("status", appData.getStatus());
                            appInfo.put("submissionDate", appData.getSubmissionDate());
                            applicationDetails.put(key, appInfo);
                        } else {
                            applicationDetails.put(key, "Type: " + (app != null ? app.getClass().getSimpleName() : "null"));
                        }
                    } catch (Exception e) {
                        applicationDetails.put(key, "Error reading: " + e.getMessage());
                    }
                }
                response.put("applicationDetails", applicationDetails);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error listing applications", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error listing applications: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Request DTOs for the POST endpoints
    public static class ApplicationStatusRequest {
        private String applicationId;

        // Default constructor
        public ApplicationStatusRequest() {}

        public String getApplicationId() { return applicationId; }
        public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    }

    public static class RiskProfileRequest {
        private String applicationId;

        // Default constructor
        public RiskProfileRequest() {}

        public String getApplicationId() { return applicationId; }
        public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    }

    public static class ReassessRiskRequest {
        private String applicationId;

        // Default constructor
        public ReassessRiskRequest() {}

        public String getApplicationId() { return applicationId; }
        public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    }

    public static class ApplicationSummaryRequest {
        private String applicationId;

        // Default constructor
        public ApplicationSummaryRequest() {}

        public String getApplicationId() { return applicationId; }
        public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
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
        public void setApprovalRecommendation(ApprovalRecommendation approvalRecommendation) {
            this.approvalRecommendation = approvalRecommendation;
        }
    }
}