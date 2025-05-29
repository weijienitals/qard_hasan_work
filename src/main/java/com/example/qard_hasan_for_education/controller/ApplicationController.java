package com.example.qard_hasan_for_education.controller;

import com.example.qard_hasan_for_education.model.StudentApplicationData;
import com.example.qard_hasan_for_education.service.DocumentOrchestrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

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
            @RequestParam("bankStatement") MultipartFile bankStatement,
            @RequestParam("universityLetter") MultipartFile universityLetter,
            @RequestParam("scholarshipLetter") MultipartFile scholarshipLetter,
            @RequestParam("passport") MultipartFile passportImage) {

        logger.info("Received complete application submission for student: {}", studentId);

        try {
            // Validate all required parameters
            if (studentId == null || studentId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Student ID is required"));
            }

            // Validate file uploads
            orchestrationService.validateFiles(bankStatement, universityLetter,
                    scholarshipLetter, passportImage);

            // Process and aggregate documents
            StudentApplicationData result = orchestrationService.processCompleteApplication(
                    studentId, bankStatement, universityLetter, scholarshipLetter, passportImage
            );

            logger.info("Successfully processed and aggregated documents for student: {}, applicationId: {}, status: {}",
                    studentId, result.getApplicationId(), result.getStatus());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error processing complete application for student: {}", studentId, e);
            return ResponseEntity.status(500)
                    .body(createErrorResponse("Error processing application: " + e.getMessage()));
        }
    }

    @GetMapping("/status/{applicationId}")
    public ResponseEntity<?> getApplicationStatus(@PathVariable String applicationId) {
        logger.info("Getting application status for: {}", applicationId);

        try {
            if (applicationId == null || applicationId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Application ID is required"));
            }

            StudentApplicationData application = orchestrationService.getApplicationStatus(applicationId);

            if (application == null) {
                logger.warn("Application not found: {}", applicationId);
                return ResponseEntity.notFound().build();
            }

            logger.info("Retrieved application status: {}, status: {}", applicationId, application.getStatus());
            return ResponseEntity.ok(application);

        } catch (Exception e) {
            logger.error("Error retrieving application status for: {}", applicationId, e);
            return ResponseEntity.status(500)
                    .body(createErrorResponse("Error retrieving application status: " + e.getMessage()));
        }
    }

    @GetMapping("/status/{applicationId}/summary")
    public ResponseEntity<?> getApplicationSummary(@PathVariable String applicationId) {
        logger.info("Getting application summary for: {}", applicationId);

        try {
            StudentApplicationData application = orchestrationService.getApplicationStatus(applicationId);

            if (application == null) {
                return ResponseEntity.notFound().build();
            }

            // Create a summary response with key information
            Map<String, Object> summary = new HashMap<>();
            summary.put("applicationId", application.getApplicationId());
            summary.put("studentId", application.getStudentId());
            summary.put("status", application.getStatus());
            summary.put("submissionDate", application.getSubmissionDate());
            summary.put("processingStartTime", application.getProcessingStartTime());
            summary.put("processingEndTime", application.getProcessingEndTime());
            summary.put("processingTimeMs", application.getProcessingTimeMs());

            // Add document processing status
            summary.put("documentsProcessed", application.isDocumentProcessingComplete());

            // Document count summary
            Map<String, Object> documentsSummary = new HashMap<>();
            documentsSummary.put("bankStatement", application.getBankInfo() != null);
            documentsSummary.put("universityLetter", application.getUniversityAcceptance() != null);
            documentsSummary.put("scholarshipLetter", application.getScholarshipAcceptance() != null);
            documentsSummary.put("passport", application.getPassportInfo() != null);
            summary.put("documentsStatus", documentsSummary);

            return ResponseEntity.ok(summary);

        } catch (Exception e) {
            logger.error("Error retrieving application summary for: {}", applicationId, e);
            return ResponseEntity.status(500)
                    .body(createErrorResponse("Error retrieving application summary: " + e.getMessage()));
        }
    }

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Document Aggregation API");
        health.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(health);
    }

    // Get system info
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("serviceName", "Qard Hasan Education - Document Aggregation Service");
        info.put("version", "1.0.0");
        info.put("features", java.util.List.of(
                "Document Processing with AI",
                "Multi-document Aggregation",
                "Real-time Status Tracking",
                "Concurrent Processing"
        ));
        info.put("supportedDocuments", java.util.List.of(
                "Bank Statements (PDF)",
                "University Acceptance Letters (PDF)",
                "Scholarship Letters (PDF)",
                "Passport Images (JPEG, PNG, WEBP)"
        ));
        info.put("note", "Verification functionality disabled - aggregation only");
        return ResponseEntity.ok(info);
    }

    // Helper methods for response formatting
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        response.put("timestamp", java.time.LocalDateTime.now());
        return response;
    }
}