package com.example.qard_hasan_for_education.controller;

import com.example.qard_hasan_for_education.model.*;
import com.example.qard_hasan_for_education.service.ApplicationService;
import com.example.qard_hasan_for_education.service.DocumentProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private DocumentProcessor documentProcessor;

    // ============= APPLICATION MANAGEMENT =============

    /**
     * Create new application for a student
     */
    @PostMapping("/create")
    public ResponseEntity<?> createApplication(@RequestParam("studentId") String studentId) {
        try {
            ApplicationData application = applicationService.createApplication(studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Application created successfully");
            response.put("application", application);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creating application for student {}: {}", studentId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(createErrorResponse("Error creating application: " + e.getMessage()));
        }
    }

    /**
     * Get application by ID - Returns complete aggregated data
     */
    @GetMapping("/{applicationId}")
    public ResponseEntity<?> getApplication(@PathVariable String applicationId) {
        try {
            ApplicationData application = applicationService.getApplication(applicationId);
            if (application == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("application", application);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving application {}: {}", applicationId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(createErrorResponse("Error retrieving application: " + e.getMessage()));
        }
    }

    /**
     * Get applications by student ID
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getApplicationsByStudent(@PathVariable String studentId) {
        try {
            List<ApplicationData> applications = applicationService.getApplicationsByStudent(studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("applications", applications);
            response.put("count", applications.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving applications for student {}: {}", studentId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(createErrorResponse("Error retrieving applications: " + e.getMessage()));
        }
    }

    // ============= DOCUMENT PROCESSING & AGGREGATION =============

    /**
     * Process passport document and aggregate into application
     */
    @PostMapping("/{applicationId}/passport")
    public ResponseEntity<?> processAndAggregatePassport(
            @PathVariable String applicationId,
            @RequestParam("file") MultipartFile file) {
        try {
            // Validate file type
            if (!isImageFile(file.getContentType())) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Only image files (JPEG, PNG, WEBP) are supported for passport processing"));
            }

            // Process the document using existing DocumentProcessor
            PassportInfo passportInfo = documentProcessor.processPassportImage(file);

            // Aggregate into application
            ApplicationData updatedApplication = applicationService.aggregatePassportInfo(applicationId, passportInfo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Passport processed and aggregated successfully");
            response.put("extractedData", passportInfo);
            response.put("application", updatedApplication);
            response.put("documentsComplete", updatedApplication.getDocumentsProcessed().isAllDocumentsProcessed());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing passport for application {}: {}", applicationId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(createErrorResponse("Error processing passport: " + e.getMessage()));
        }
    }

    /**
     * Process bank statement and aggregate into application
     */
    @PostMapping("/{applicationId}/bank-statement")
    public ResponseEntity<?> processAndAggregateBankStatement(
            @PathVariable String applicationId,
            @RequestParam("file") MultipartFile file) {
        try {
            // Validate file type
            if (!isPdfFile(file.getContentType())) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Only PDF files are supported for bank statement processing"));
            }

            // Process the document
            SimpleBankInfo bankInfo = documentProcessor.processBankDocument(file);

            // Aggregate into application
            ApplicationData updatedApplication = applicationService.aggregateBankInfo(applicationId, bankInfo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Bank statement processed and aggregated successfully");
            response.put("extractedData", bankInfo);
            response.put("application", updatedApplication);
            response.put("documentsComplete", updatedApplication.getDocumentsProcessed().isAllDocumentsProcessed());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing bank statement for application {}: {}", applicationId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(createErrorResponse("Error processing bank statement: " + e.getMessage()));
        }
    }

    /**
     * Process university acceptance letter and aggregate into application
     */
    @PostMapping("/{applicationId}/university-letter")
    public ResponseEntity<?> processAndAggregateUniversityLetter(
            @PathVariable String applicationId,
            @RequestParam("file") MultipartFile file) {
        try {
            // Validate file type
            if (!isPdfFile(file.getContentType())) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Only PDF files are supported for university letter processing"));
            }

            // Process the document
            UniversityAcceptance universityInfo = documentProcessor.processUniversityLetter(file);

            // Aggregate into application
            ApplicationData updatedApplication = applicationService.aggregateUniversityInfo(applicationId, universityInfo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "University letter processed and aggregated successfully");
            response.put("extractedData", universityInfo);
            response.put("application", updatedApplication);
            response.put("documentsComplete", updatedApplication.getDocumentsProcessed().isAllDocumentsProcessed());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing university letter for application {}: {}", applicationId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(createErrorResponse("Error processing university letter: " + e.getMessage()));
        }
    }

    /**
     * Process scholarship letter and aggregate into application
     */
    @PostMapping("/{applicationId}/scholarship-letter")
    public ResponseEntity<?> processAndAggregateScholarshipLetter(
            @PathVariable String applicationId,
            @RequestParam("file") MultipartFile file) {
        try {
            // Validate file type
            if (!isPdfFile(file.getContentType())) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Only PDF files are supported for scholarship letter processing"));
            }

            // Process the document
            ScholarshipAcceptance scholarshipInfo = documentProcessor.processScholarshipLetter(file);

            // Aggregate into application
            ApplicationData updatedApplication = applicationService.aggregateScholarshipInfo(applicationId, scholarshipInfo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Scholarship letter processed and aggregated successfully");
            response.put("extractedData", scholarshipInfo);
            response.put("application", updatedApplication);
            response.put("documentsComplete", updatedApplication.getDocumentsProcessed().isAllDocumentsProcessed());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing scholarship letter for application {}: {}", applicationId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(createErrorResponse("Error processing scholarship letter: " + e.getMessage()));
        }
    }

    /**
     * Batch upload - process multiple documents at once
     */
    @PostMapping("/{applicationId}/batch-upload")
    public ResponseEntity<?> batchUploadDocuments(
            @PathVariable String applicationId,
            @RequestParam(value = "passport", required = false) MultipartFile passportFile,
            @RequestParam(value = "bankStatement", required = false) MultipartFile bankFile,
            @RequestParam(value = "universityLetter", required = false) MultipartFile universityFile,
            @RequestParam(value = "scholarshipLetter", required = false) MultipartFile scholarshipFile) {

        try {
            Map<String, Object> processedDocuments = new HashMap<>();
            ApplicationData application = applicationService.getApplication(applicationId);

            if (application == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Application not found: " + applicationId));
            }

            // Process each document if provided
            if (passportFile != null && !passportFile.isEmpty()) {
                try {
                    PassportInfo passportInfo = documentProcessor.processPassportImage(passportFile);
                    application = applicationService.aggregatePassportInfo(applicationId, passportInfo);
                    processedDocuments.put("passport", Map.of("success", true, "data", passportInfo));
                } catch (Exception e) {
                    processedDocuments.put("passport", Map.of("success", false, "error", e.getMessage()));
                }
            }

            if (bankFile != null && !bankFile.isEmpty()) {
                try {
                    SimpleBankInfo bankInfo = documentProcessor.processBankDocument(bankFile);
                    application = applicationService.aggregateBankInfo(applicationId, bankInfo);
                    processedDocuments.put("bankStatement", Map.of("success", true, "data", bankInfo));
                } catch (Exception e) {
                    processedDocuments.put("bankStatement", Map.of("success", false, "error", e.getMessage()));
                }
            }

            if (universityFile != null && !universityFile.isEmpty()) {
                try {
                    UniversityAcceptance universityInfo = documentProcessor.processUniversityLetter(universityFile);
                    application = applicationService.aggregateUniversityInfo(applicationId, universityInfo);
                    processedDocuments.put("universityLetter", Map.of("success", true, "data", universityInfo));
                } catch (Exception e) {
                    processedDocuments.put("universityLetter", Map.of("success", false, "error", e.getMessage()));
                }
            }

            if (scholarshipFile != null && !scholarshipFile.isEmpty()) {
                try {
                    ScholarshipAcceptance scholarshipInfo = documentProcessor.processScholarshipLetter(scholarshipFile);
                    application = applicationService.aggregateScholarshipInfo(applicationId, scholarshipInfo);
                    processedDocuments.put("scholarshipLetter", Map.of("success", true, "data", scholarshipInfo));
                } catch (Exception e) {
                    processedDocuments.put("scholarshipLetter", Map.of("success", false, "error", e.getMessage()));
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Batch upload completed");
            response.put("processedDocuments", processedDocuments);
            response.put("application", application);
            response.put("documentsComplete", application.getDocumentsProcessed().isAllDocumentsProcessed());
            response.put("readyForFinalization", application.getDocumentsProcessed().isAllDocumentsProcessed());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in batch upload for application {}: {}", applicationId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(createErrorResponse("Error in batch upload: " + e.getMessage()));
        }
    }

    // ============= APPLICATION FINALIZATION =============

    /**
     * Finalize application after all required documents are processed
     */
    @PostMapping("/{applicationId}/finalize")
    public ResponseEntity<?> finalizeApplication(@PathVariable String applicationId) {
        try {
            ApplicationData finalizedApplication = applicationService.finalizeApplication(applicationId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Application finalized successfully");
            response.put("application", finalizedApplication);
            response.put("status", finalizedApplication.getApplicationStatus());
            response.put("overallRisk", finalizedApplication.getRiskAssessment().getOverallRisk());
            response.put("recommendedFunding", finalizedApplication.getFundingRequirement().getRecommendedFunding());
            response.put("fundingType", finalizedApplication.getFundingRequirement().getFundingType());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error finalizing application {}: {}", applicationId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(createErrorResponse("Error finalizing application: " + e.getMessage()));
        }
    }

    // ============= STATUS & MONITORING =============

    /**
     * Get application summary for dashboard
     */
    @GetMapping("/{applicationId}/summary")
    public ResponseEntity<?> getApplicationSummary(@PathVariable String applicationId) {
        try {
            Map<String, Object> summary = applicationService.getApplicationSummary(applicationId);
            if (summary.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("summary", summary);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting application summary {}: {}", applicationId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(createErrorResponse("Error getting application summary: " + e.getMessage()));
        }
    }

    /**
     * Get application progress/status
     */
    @GetMapping("/{applicationId}/progress")
    public ResponseEntity<?> getApplicationProgress(@PathVariable String applicationId) {
        try {
            ApplicationData application = applicationService.getApplication(applicationId);
            if (application == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> progress = new HashMap<>();
            progress.put("applicationId", applicationId);
            progress.put("status", application.getApplicationStatus());
            progress.put("documentsProcessed", application.getDocumentsProcessed());
            progress.put("lastUpdated", application.getLastUpdated());
            progress.put("completionPercentage", calculateCompletionPercentage(application));
            progress.put("nextSteps", getNextSteps(application));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("progress", progress);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting application progress {}: {}", applicationId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(createErrorResponse("Error getting application progress: " + e.getMessage()));
        }
    }

    /**
     * Update application status (admin function)
     */
    @PutMapping("/{applicationId}/status")
    public ResponseEntity<?> updateApplicationStatus(
            @PathVariable String applicationId,
            @RequestParam("status") String status) {
        try {
            ApplicationData.ApplicationStatus newStatus = ApplicationData.ApplicationStatus.valueOf(status.toUpperCase());
            ApplicationData updatedApplication = applicationService.updateApplicationStatus(applicationId, newStatus);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Application status updated successfully");
            response.put("application", updatedApplication);
            response.put("newStatus", newStatus);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Invalid status or application not found: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating application status {}: {}", applicationId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(createErrorResponse("Error updating application status: " + e.getMessage()));
        }
    }

    /**
     * Add note to application
     */
    @PostMapping("/{applicationId}/notes")
    public ResponseEntity<?> addNote(
            @PathVariable String applicationId,
            @RequestParam("note") String note) {
        try {
            ApplicationData updatedApplication = applicationService.addNote(applicationId, note);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Note added successfully");
            response.put("application", updatedApplication);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error adding note to application {}: {}", applicationId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(createErrorResponse("Error adding note: " + e.getMessage()));
        }
    }

    // ============= ADMIN FUNCTIONS =============

    /**
     * Get all applications (admin dashboard)
     */
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllApplications() {
        try {
            List<ApplicationData> applications = applicationService.getAllApplications();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("applications", applications);
            response.put("count", applications.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving all applications: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(createErrorResponse("Error retrieving applications: " + e.getMessage()));
        }
    }

    /**
     * Get processing statistics (admin dashboard)
     */
    @GetMapping("/admin/statistics")
    public ResponseEntity<?> getProcessingStatistics() {
        try {
            Map<String, Object> statistics = applicationService.getProcessingStatistics();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving processing statistics: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(createErrorResponse("Error retrieving statistics: " + e.getMessage()));
        }
    }

    // ============= UTILITY METHODS =============

    private boolean isImageFile(String contentType) {
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/webp")
        );
    }

    private boolean isPdfFile(String contentType) {
        return contentType != null && contentType.equals("application/pdf");
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        error.put("timestamp", java.time.LocalDateTime.now());
        return error;
    }

    private int calculateCompletionPercentage(ApplicationData application) {
        ApplicationData.DocumentProcessingStatus docs = application.getDocumentsProcessed();
        int completed = 0;
        int total = 3; // passport, bank, university (scholarship is optional)

        if (docs.isPassportProcessed()) completed++;
        if (docs.isBankStatementProcessed()) completed++;
        if (docs.isUniversityLetterProcessed()) completed++;

        // Bonus for scholarship (makes it 4 total if present)
        if (docs.isScholarshipLetterProcessed()) {
            completed++;
            total = 4;
        }

        return (completed * 100) / total;
    }

    private List<String> getNextSteps(ApplicationData application) {
        ApplicationData.DocumentProcessingStatus docs = application.getDocumentsProcessed();
        List<String> nextSteps = new java.util.ArrayList<>();

        if (!docs.isPassportProcessed()) {
            nextSteps.add("Upload passport image");
        }
        if (!docs.isBankStatementProcessed()) {
            nextSteps.add("Upload bank statement PDF");
        }
        if (!docs.isUniversityLetterProcessed()) {
            nextSteps.add("Upload university acceptance letter PDF");
        }
        if (!docs.isScholarshipLetterProcessed()) {
            nextSteps.add("Upload scholarship letter PDF (optional)");
        }

        if (docs.isAllDocumentsProcessed() &&
                application.getApplicationStatus() == ApplicationData.ApplicationStatus.DRAFT) {
            nextSteps.add("Finalize application for review");
        }

        if (nextSteps.isEmpty()) {
            nextSteps.add("Application is complete and ready for processing");
        }

        return nextSteps;
    }

    // ============= HEALTH CHECK =============

    /**
     * Health check for the application service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "Qard Hasan Application Aggregation Service");
        status.put("status", "running");
        status.put("timestamp", java.time.LocalDateTime.now());
        status.put("version", "1.0.0");

        try {
            // Test basic functionality
            Map<String, Object> stats = applicationService.getProcessingStatistics();
            status.put("totalApplications", stats.get("totalApplications"));
            status.put("serviceHealthy", true);
        } catch (Exception e) {
            status.put("serviceHealthy", false);
            status.put("error", e.getMessage());
        }

        return ResponseEntity.ok(status);
    }
}