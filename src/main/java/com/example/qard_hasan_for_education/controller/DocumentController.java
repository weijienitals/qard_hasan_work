package com.example.qard_hasan_for_education.controller;

import com.example.qard_hasan_for_education.model.SimpleBankInfo;
import com.example.qard_hasan_for_education.model.UniversityAcceptance;
import com.example.qard_hasan_for_education.model.ScholarshipAcceptance;
import com.example.qard_hasan_for_education.service.DocumentProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
public class DocumentController {

    @Autowired
    private DocumentProcessor processor;

    @PostMapping("/bank-info")
    public ResponseEntity<?> processBankDocument(
            @RequestParam("file") MultipartFile file) {
        try {
            if (!file.getContentType().equals("application/pdf")) {
                return ResponseEntity.badRequest()
                        .body("Only PDF files are supported");
            }

            SimpleBankInfo result = processor.processBankDocument(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Error processing bank document: " + e.getMessage());
        }
    }

    @PostMapping("/university-acceptance")
    public ResponseEntity<?> processUniversityLetter(
            @RequestParam("file") MultipartFile file) {
        try {
            if (!file.getContentType().equals("application/pdf")) {
                return ResponseEntity.badRequest()
                        .body("Only PDF files are supported");
            }

            UniversityAcceptance result = processor.processUniversityLetter(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Error processing university letter: " + e.getMessage());
        }
    }

    @PostMapping("/scholarship-acceptance")
    public ResponseEntity<?> processScholarshipLetter(
            @RequestParam("file") MultipartFile file) {
        try {
            if (!file.getContentType().equals("application/pdf")) {
                return ResponseEntity.badRequest()
                        .body("Only PDF files are supported");
            }

            ScholarshipAcceptance result = processor.processScholarshipLetter(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Error processing scholarship letter: " + e.getMessage());
        }
    }

    // Generic endpoint for any document type
    @PostMapping("/process")
    public ResponseEntity<String> processAnyDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType) {
        try {
            if (!file.getContentType().equals("application/pdf")) {
                return ResponseEntity.badRequest()
                        .body("Only PDF files are supported");
            }

            Object result;
            switch (documentType.toLowerCase()) {
                case "bank":
                    result = processor.processBankDocument(file);
                    break;
                case "university":
                    result = processor.processUniversityLetter(file);
                    break;
                case "scholarship":
                    result = processor.processScholarshipLetter(file);
                    break;
                default:
                    return ResponseEntity.badRequest()
                            .body("Unsupported document type: " + documentType);
            }

            ObjectMapper mapper = new ObjectMapper();
            return ResponseEntity.ok(mapper.writeValueAsString(result));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Error: " + e.getMessage());
        }
    }

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Document Processing API is running!");
    }
}