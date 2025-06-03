package com.example.qard_hasan_for_education.controller;

import com.example.qard_hasan_for_education.model.*;
import com.example.qard_hasan_for_education.model.individual.*;
import com.example.qard_hasan_for_education.service.RepaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/repayment")
@CrossOrigin(origins = "*")
public class RepaymentController {

    private static final Logger logger = LoggerFactory.getLogger(RepaymentController.class);


    @Autowired
    private RepaymentService repaymentService;

    /**
     * Create a new loan account from approved application
     */
    @PostMapping("/create-loan")
    public ResponseEntity<?> createLoanAccount(@RequestBody CreateLoanRequest request) {
        try {
            // In a real implementation, you would validate the application first
            // For now, we'll create a simplified version

            StudentApplicationData mockApplication = createMockApplication(request);

            LoanAccount loanAccount = repaymentService.createLoanAccount(
                    mockApplication,
                    request.getLoanAmount(),
                    request.getTermMonths()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Loan account created successfully");
            response.put("loanAccount", loanAccount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error creating loan account", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error creating loan account: " + e.getMessage()));
        }
    }

    /**
     * Process a repayment
     */
    @PostMapping("/pay")
    public ResponseEntity<?> makeRepayment(@RequestBody PaymentRequest request) {
        try {
            RepaymentService.PaymentResult paymentResult = repaymentService.processRepayment(
                    request.getLoanId(),
                    request.getAmount(),
                    request.getPaymentMethod()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment processed successfully");
            response.put("transaction", paymentResult.getTransaction());
            response.put("mentoringOfferSent", paymentResult.isMentoringOfferSent());

            // Include offerId if mentoring offer was sent
            if (paymentResult.getOfferId() != null) {
                response.put("offerId", paymentResult.getOfferId());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error processing payment", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Get loan account details - Changed to POST with request body
     */
    @PostMapping("/loan")
    public ResponseEntity<?> getLoanAccount(@RequestBody GetLoanRequest request) {
        try {
            LoanAccount loanAccount = repaymentService.getLoanAccount(request.getLoanId());

            if (loanAccount == null) {
                return ResponseEntity.status(404)
                        .body(Map.of("success", false, "message", "Loan account not found"));
            }

            return ResponseEntity.ok(Map.of("success", true, "loanAccount", loanAccount));

        } catch (Exception e) {
            logger.error("Error retrieving loan account", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error retrieving loan account"));
        }
    }

    /**
     * Get all loans for a student - Changed to POST with request body
     */
    @PostMapping("/student")
    public ResponseEntity<?> getStudentLoans(@RequestBody GetStudentLoansRequest request) {
        try {
            List<LoanAccount> loans = repaymentService.getStudentLoans(request.getStudentId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("loans", loans);
            response.put("totalLoans", loans.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving student loans", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error retrieving student loans"));
        }
    }

    /**
     * Get repayment history for a loan - Changed to POST with request body
     */
    @PostMapping("/history")
    public ResponseEntity<?> getRepaymentHistory(@RequestBody GetRepaymentHistoryRequest request) {
        try {
            List<RepaymentTransaction> history = repaymentService.getRepaymentHistory(request.getLoanId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("transactions", history);
            response.put("totalTransactions", history.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving repayment history", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error retrieving repayment history"));
        }
    }

    /**
     * Get upcoming payment information - Changed to POST with request body
     */
    @PostMapping("/upcoming")
    public ResponseEntity<?> getUpcomingPayment(@RequestBody GetUpcomingPaymentRequest request) {
        try {
            Map<String, Object> paymentInfo = repaymentService.getUpcomingPayment(request.getLoanId());

            if (paymentInfo.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(Map.of("success", false, "message", "Loan not found"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("upcomingPayment", paymentInfo);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving upcoming payment info", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error retrieving payment information"));
        }
    }

    /**
     * Get loan dashboard summary for a student - Changed to POST with request body
     */
    @PostMapping("/dashboard")
    public ResponseEntity<?> getLoanDashboard(@RequestBody GetDashboardRequest request) {
        try {
            List<LoanAccount> loans = repaymentService.getStudentLoans(request.getStudentId());

            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("totalLoans", loans.size());

            // Calculate totals
            BigDecimal totalPrincipal = BigDecimal.ZERO;
            BigDecimal totalRemaining = BigDecimal.ZERO;
            int totalCompletedPayments = 0;
            int totalUpcomingPayments = 0;

            for (LoanAccount loan : loans) {
                totalPrincipal = totalPrincipal.add(loan.getPrincipalAmount());
                totalRemaining = totalRemaining.add(loan.getRemainingBalance());
                totalCompletedPayments += loan.getCompletedInstallments();
                totalUpcomingPayments += (loan.getTotalInstallments() - loan.getCompletedInstallments());
            }

            dashboard.put("totalPrincipal", totalPrincipal);
            dashboard.put("totalRemaining", totalRemaining);
            dashboard.put("totalPaid", totalPrincipal.subtract(totalRemaining));
            dashboard.put("completedPayments", totalCompletedPayments);
            dashboard.put("upcomingPayments", totalUpcomingPayments);
            dashboard.put("loans", loans);

            // Calculate repayment progress
            if (totalPrincipal.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal progressPercentage = totalPrincipal.subtract(totalRemaining)
                        .divide(totalPrincipal, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                dashboard.put("repaymentProgress", progressPercentage);
            } else {
                dashboard.put("repaymentProgress", BigDecimal.ZERO);
            }

            return ResponseEntity.ok(Map.of("success", true, "dashboard", dashboard));

        } catch (Exception e) {
            logger.error("Error generating loan dashboard", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error generating dashboard"));
        }
    }

    // Helper method to create mock application for testing
    private StudentApplicationData createMockApplication(CreateLoanRequest request) {
        StudentApplicationData application = new StudentApplicationData(
                "APP_MOCK_" + System.currentTimeMillis(),
                request.getStudentId()
        );

        // Create mock passport info
        PassportInfo passportInfo = new PassportInfo();
        passportInfo.setFullName(request.getStudentName());
        passportInfo.setPassportNumber("MOCK123456");
        passportInfo.setNationality("Indonesian");
        application.setPassportInfo(passportInfo);

        // Create mock university acceptance
        UniversityAcceptance universityAcceptance = new UniversityAcceptance();
        universityAcceptance.setUniversityName(request.getUniversityName());
        universityAcceptance.setStudentName(request.getStudentName());
        universityAcceptance.setProgram(request.getProgram());
        application.setUniversityAcceptance(universityAcceptance);

        return application;
    }

    // Request DTOs
    public static class CreateLoanRequest {
        private String studentId;
        private String studentName;
        private String universityName;
        private String program;
        private BigDecimal loanAmount;
        private Integer termMonths;

        // Getters and setters
        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public String getUniversityName() { return universityName; }
        public void setUniversityName(String universityName) { this.universityName = universityName; }

        public String getProgram() { return program; }
        public void setProgram(String program) { this.program = program; }

        public BigDecimal getLoanAmount() { return loanAmount; }
        public void setLoanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; }

        public Integer getTermMonths() { return termMonths; }
        public void setTermMonths(Integer termMonths) { this.termMonths = termMonths; }
    }

    public static class PaymentRequest {
        private String loanId;
        private BigDecimal amount;
        private String paymentMethod;

        // Getters and setters
        public String getLoanId() { return loanId; }
        public void setLoanId(String loanId) { this.loanId = loanId; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    }

    // New request DTOs for the converted GET endpoints
    public static class GetLoanRequest {
        private String loanId;

        public String getLoanId() { return loanId; }
        public void setLoanId(String loanId) { this.loanId = loanId; }
    }

    public static class GetStudentLoansRequest {
        private String studentId;

        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
    }

    public static class GetRepaymentHistoryRequest {
        private String loanId;

        public String getLoanId() { return loanId; }
        public void setLoanId(String loanId) { this.loanId = loanId; }
    }

    public static class GetUpcomingPaymentRequest {
        private String loanId;

        public String getLoanId() { return loanId; }
        public void setLoanId(String loanId) { this.loanId = loanId; }
    }

    public static class GetDashboardRequest {
        private String studentId;

        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
    }
}