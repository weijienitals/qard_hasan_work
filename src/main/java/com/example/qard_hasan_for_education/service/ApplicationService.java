package com.example.qard_hasan_for_education.service;

import com.example.qard_hasan_for_education.model.*;
import com.example.qard_hasan_for_education.model.ApplicationData.*;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);

    // In-memory storage for demo purposes (in production, use a database)
    private final Map<String, ApplicationData> applicationStorage = new ConcurrentHashMap<>();

    // Constants for financial calculations (in IDR - Indonesian Rupiah)
    private static final BigDecimal ESTIMATED_TRANSITION_COST_BASE = new BigDecimal("150000000"); // 150M IDR (~$10,000 USD)
    private static final BigDecimal UNIVERSITY_DEPOSIT_AVG = new BigDecimal("50000000"); // 50M IDR
    private static final BigDecimal VISA_COSTS = new BigDecimal("20000000"); // 20M IDR
    private static final BigDecimal INITIAL_SETTLEMENT = new BigDecimal("80000000"); // 80M IDR

    /**
     * Create a new application for a student
     */
    public ApplicationData createApplication(String studentId) {
        ApplicationData application = new ApplicationData();
        application.setStudentId(studentId);
        application.setApplicationStatus(ApplicationStatus.DRAFT);

        applicationStorage.put(application.getApplicationId(), application);
        logger.info("Created new application {} for student {}", application.getApplicationId(), studentId);

        return application;
    }

    /**
     * Get application by ID
     */
    public ApplicationData getApplication(String applicationId) {
        return applicationStorage.get(applicationId);
    }

    /**
     * Get all applications for a student
     */
    public List<ApplicationData> getApplicationsByStudent(String studentId) {
        return applicationStorage.values().stream()
                .filter(app -> studentId.equals(app.getStudentId()))
                .toList();
    }

    /**
     * Aggregate passport information into application
     */
    public ApplicationData aggregatePassportInfo(String applicationId, PassportInfo passportInfo) {
        ApplicationData application = getApplication(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("Application not found: " + applicationId);
        }

        PersonalInfo personalInfo = application.getPersonalInfo();
        personalInfo.setFullName(passportInfo.getFullName());
        personalInfo.setPassportNumber(passportInfo.getPassportNumber());
        personalInfo.setNationality(passportInfo.getNationality());
        personalInfo.setDateOfBirth(passportInfo.getDateOfBirth());
        personalInfo.setGender(passportInfo.getGender());
        personalInfo.setPassportExpiryDate(passportInfo.getExpiryDate());

        // Update processing status
        application.getDocumentsProcessed().setPassportProcessed(true);
        application.setLastUpdated(LocalDateTime.now());

        // Add validation notes
        validatePassportData(application, passportInfo);

        logger.info("Aggregated passport info for application {}", applicationId);
        return application;
    }

    /**
     * Aggregate bank information into application
     */
    public ApplicationData aggregateBankInfo(String applicationId, SimpleBankInfo bankInfo) {
        ApplicationData application = getApplication(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("Application not found: " + applicationId);
        }

        FinancialInfo financialInfo = application.getFinancialInfo();
        financialInfo.setAccountNumber(bankInfo.getAccountNumber());
        financialInfo.setBankName(bankInfo.getBankName());
        financialInfo.setAccountHolderName(bankInfo.getAccountHolderName());
        financialInfo.setCurrentBalance(bankInfo.getCurrentBalance());

        // Update processing status
        application.getDocumentsProcessed().setBankStatementProcessed(true);
        application.setLastUpdated(LocalDateTime.now());

        // Perform financial analysis
        performFinancialAnalysis(application);

        logger.info("Aggregated bank info for application {}", applicationId);
        return application;
    }

    /**
     * Aggregate university acceptance information into application
     */
    public ApplicationData aggregateUniversityInfo(String applicationId, UniversityAcceptance universityInfo) {
        ApplicationData application = getApplication(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("Application not found: " + applicationId);
        }

        AcademicInfo academicInfo = application.getAcademicInfo();
        academicInfo.setUniversityName(universityInfo.getUniversityName());
        academicInfo.setProgram(universityInfo.getProgram());
        academicInfo.setAcceptanceDate(universityInfo.getAcceptanceDate());
        academicInfo.setSemesterStart(universityInfo.getSemesterStart());

        // Update processing status
        application.getDocumentsProcessed().setUniversityLetterProcessed(true);
        application.setLastUpdated(LocalDateTime.now());

        // Calculate urgency based on semester start date
        calculateUrgencyLevel(application);

        // Recalculate financial requirements if we have financial info
        if (application.getDocumentsProcessed().isBankStatementProcessed()) {
            calculateFinancialGap(application);
        }

        logger.info("Aggregated university info for application {}", applicationId);
        return application;
    }

    /**
     * Aggregate scholarship information into application
     */
    public ApplicationData aggregateScholarshipInfo(String applicationId, ScholarshipAcceptance scholarshipInfo) {
        ApplicationData application = getApplication(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("Application not found: " + applicationId);
        }

        // Update financial info
        FinancialInfo financialInfo = application.getFinancialInfo();
        financialInfo.setHasScholarship(true);
        financialInfo.setScholarshipAmount(scholarshipInfo.getAmount());
        financialInfo.setScholarshipProvider(scholarshipInfo.getProvider());

        // Update academic info
        AcademicInfo academicInfo = application.getAcademicInfo();
        academicInfo.setScholarshipName(scholarshipInfo.getScholarshipName());
        academicInfo.setAcademicYear(scholarshipInfo.getAcademicYear());

        // Update processing status
        application.getDocumentsProcessed().setScholarshipLetterProcessed(true);
        application.setLastUpdated(LocalDateTime.now());

        // Recalculate financial gap
        calculateFinancialGap(application);

        logger.info("Aggregated scholarship info for application {}", applicationId);
        return application;
    }

    /**
     * Complete application aggregation and perform final analysis
     */
    public ApplicationData finalizeApplication(String applicationId) {
        ApplicationData application = getApplication(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("Application not found: " + applicationId);
        }

        if (!application.getDocumentsProcessed().isAllDocumentsProcessed()) {
            throw new IllegalStateException("Cannot finalize application - required documents not processed. Need: passport, bank statement, and university letter");
        }

        // Perform comprehensive analysis
        performComprehensiveAnalysis(application);

        // Update status
        application.setApplicationStatus(ApplicationStatus.SUBMITTED);
        application.setLastUpdated(LocalDateTime.now());
        application.addNote("Application finalized and ready for review");

        logger.info("Finalized application {}", applicationId);
        return application;
    }

    /**
     * Get application summary for dashboard
     */
    public Map<String, Object> getApplicationSummary(String applicationId) {
        ApplicationData application = getApplication(applicationId);
        if (application == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("applicationId", application.getApplicationId());
        summary.put("studentId", application.getStudentId());
        summary.put("studentName", application.getPersonalInfo().getFullName());
        summary.put("university", application.getAcademicInfo().getUniversityName());
        summary.put("program", application.getAcademicInfo().getProgram());
        summary.put("status", application.getApplicationStatus());
        summary.put("currentBalance", application.getFinancialInfo().getCurrentBalance());
        summary.put("scholarshipAmount", application.getFinancialInfo().getScholarshipAmount());
        summary.put("financialGap", application.getFinancialInfo().getFinancialGap());
        summary.put("recommendedFunding", application.getFundingRequirement().getRecommendedFunding());
        summary.put("fundingType", application.getFundingRequirement().getFundingType());
        summary.put("overallRisk", application.getRiskAssessment().getOverallRisk());
        summary.put("urgencyLevel", application.getFundingRequirement().getUrgencyLevel());
        summary.put("documentsComplete", application.getDocumentsProcessed().isAllDocumentsProcessed());
        summary.put("lastUpdated", application.getLastUpdated());

        return summary;
    }

    // Private helper methods for business logic

    private void validatePassportData(ApplicationData application, PassportInfo passportInfo) {
        // Check passport expiry
        if (passportInfo.getExpiryDate() != null) {
            application.addNote("Passport validation: Expiry date recorded as " + passportInfo.getExpiryDate());
        }

        // Check nationality for Indonesian students (primary target for Qard Hasan)
        if (!"INDONESIA".equalsIgnoreCase(passportInfo.getNationality()) &&
                !"Indonesian".equalsIgnoreCase(passportInfo.getNationality())) {
            application.addNote("Note: Non-Indonesian nationality detected: " + passportInfo.getNationality());
        }

        // Name consistency check will be done when other documents are processed
    }

    private void performFinancialAnalysis(ApplicationData application) {
        FinancialInfo financialInfo = application.getFinancialInfo();
        FundingRequirement fundingReq = application.getFundingRequirement();

        // Calculate estimated transition cost based on Qard Hasan transition support model
        BigDecimal transitionCost = ESTIMATED_TRANSITION_COST_BASE;
        fundingReq.setEstimatedTransitionCost(transitionCost);

        // Initial risk assessment based on bank balance
        RiskAssessment riskAssessment = application.getRiskAssessment();
        BigDecimal currentBalance = financialInfo.getCurrentBalance();

        if (currentBalance == null) {
            currentBalance = BigDecimal.ZERO;
        }

        // Financial risk calculation based on Qard Hasan criteria
        assessFinancialRisk(application, currentBalance, transitionCost);
        calculateFinancialGap(application);
    }

    private void assessFinancialRisk(ApplicationData application, BigDecimal currentBalance, BigDecimal transitionCost) {
        RiskAssessment riskAssessment = application.getRiskAssessment();

        BigDecimal balanceToTransitionRatio = currentBalance.divide(transitionCost, 4, RoundingMode.HALF_UP);

        if (balanceToTransitionRatio.compareTo(new BigDecimal("0.5")) >= 0) {
            riskAssessment.setFinancialRisk(RiskLevel.LOW);
        } else if (balanceToTransitionRatio.compareTo(new BigDecimal("0.2")) >= 0) {
            riskAssessment.setFinancialRisk(RiskLevel.MEDIUM);
        } else {
            riskAssessment.setFinancialRisk(RiskLevel.HIGH);
        }

        // Add specific notes for Qard Hasan assessment
        if (currentBalance.compareTo(new BigDecimal("50000000")) < 0) { // Less than 50M IDR
            application.addNote("Financial note: Current balance below 50M IDR threshold");
        }
    }

    private void calculateFinancialGap(ApplicationData application) {
        FinancialInfo financialInfo = application.getFinancialInfo();
        FundingRequirement fundingReq = application.getFundingRequirement();

        BigDecimal totalCost = fundingReq.getEstimatedTransitionCost();
        BigDecimal availableFunds = financialInfo.getCurrentBalance();
        BigDecimal scholarshipAmount = financialInfo.getScholarshipAmount();

        if (totalCost == null) totalCost = ESTIMATED_TRANSITION_COST_BASE;
        if (availableFunds == null) availableFunds = BigDecimal.ZERO;
        if (scholarshipAmount == null) scholarshipAmount = BigDecimal.ZERO;

        BigDecimal gap = totalCost.subtract(availableFunds).subtract(scholarshipAmount);
        financialInfo.setFinancialGap(gap.max(BigDecimal.ZERO));

        // Determine funding type based on Qard Hasan model
        determineFundingType(application, gap, scholarshipAmount);
        fundingReq.setRecommendedFunding(gap.max(BigDecimal.ZERO));
    }

    private void determineFundingType(ApplicationData application, BigDecimal gap, BigDecimal scholarshipAmount) {
        FundingRequirement fundingReq = application.getFundingRequirement();

        if (scholarshipAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (gap.compareTo(new BigDecimal("50000000")) <= 0) { // 50M IDR
                fundingReq.setFundingType(FundingType.TRANSITION_ONLY);
                application.addNote("Funding type: Transition-only (has scholarship, small gap)");
            } else {
                fundingReq.setFundingType(FundingType.TRANSITION_PLUS_GAP);
                application.addNote("Funding type: Transition-plus-gap (has scholarship, larger gap)");
            }
        } else {
            if (gap.compareTo(new BigDecimal("200000000")) > 0) { // 200M IDR
                fundingReq.setFundingType(FundingType.FULL_PROGRAM);
                application.addNote("Funding type: Full program (no scholarship, large gap)");
            } else {
                fundingReq.setFundingType(FundingType.TRANSITION_ONLY);
                application.addNote("Funding type: Transition-only (no scholarship, manageable gap)");
            }
        }
    }

    private void calculateUrgencyLevel(ApplicationData application) {
        AcademicInfo academicInfo = application.getAcademicInfo();
        FundingRequirement fundingReq = application.getFundingRequirement();

        String semesterStart = academicInfo.getSemesterStart();
        if (semesterStart == null || semesterStart.trim().isEmpty()) {
            fundingReq.setUrgencyLevel(UrgencyLevel.MEDIUM);
            return;
        }

        try {
            LocalDateTime now = LocalDateTime.now();

            // Parse semester start for urgency calculation
            if (semesterStart.toLowerCase().contains("fall") ||
                    semesterStart.toLowerCase().contains("september") ||
                    semesterStart.toLowerCase().contains("august")) {

                // If it's currently past June and fall semester, high urgency
                if (now.getMonthValue() >= 6 && now.getMonthValue() <= 9) {
                    fundingReq.setUrgencyLevel(UrgencyLevel.HIGH);
                    application.addNote("High urgency: Fall semester approaching");
                } else if (now.getMonthValue() >= 7) {
                    fundingReq.setUrgencyLevel(UrgencyLevel.CRITICAL);
                    application.addNote("Critical urgency: Fall semester imminent");
                } else {
                    fundingReq.setUrgencyLevel(UrgencyLevel.MEDIUM);
                }
            } else if (semesterStart.toLowerCase().contains("spring") ||
                    semesterStart.toLowerCase().contains("january") ||
                    semesterStart.toLowerCase().contains("february")) {

                // Spring semester urgency
                if ((now.getMonthValue() >= 10) || (now.getMonthValue() <= 2)) {
                    fundingReq.setUrgencyLevel(UrgencyLevel.HIGH);
                    application.addNote("High urgency: Spring semester approaching");
                } else {
                    fundingReq.setUrgencyLevel(UrgencyLevel.MEDIUM);
                }
            } else {
                fundingReq.setUrgencyLevel(UrgencyLevel.MEDIUM);
            }

        } catch (Exception e) {
            logger.warn("Could not parse semester start date: {}", semesterStart);
            fundingReq.setUrgencyLevel(UrgencyLevel.MEDIUM);
        }
    }

    private void performComprehensiveAnalysis(ApplicationData application) {
        // Academic risk assessment
        assessAcademicRisk(application);

        // Repayment capability assessment
        assessRepaymentCapability(application);

        // Overall risk calculation
        calculateOverallRisk(application);

        // Generate risk factors
        generateRiskFactors(application);

        // Final funding recommendation adjustments
        finalizeFundingRecommendation(application);

        // Validate name consistency across documents
        validateNameConsistency(application);
    }

    private void assessAcademicRisk(ApplicationData application) {
        RiskAssessment riskAssessment = application.getRiskAssessment();
        AcademicInfo academicInfo = application.getAcademicInfo();

        // Academic risk assessment based on university ranking/reputation
        String university = academicInfo.getUniversityName();
        if (university != null) {
            university = university.toLowerCase();
            if (university.contains("stanford") || university.contains("harvard") ||
                    university.contains("mit") || university.contains("oxford") ||
                    university.contains("cambridge") || university.contains("berkeley") ||
                    university.contains("caltech") || university.contains("princeton")) {
                riskAssessment.setAcademicRisk(RiskLevel.LOW);
                application.addNote("Academic risk: LOW (Top-tier university)");
            } else if (university.contains("university") &&
                    (university.contains("toronto") || university.contains("melbourne") ||
                            university.contains("sydney") || university.contains("edinburgh"))) {
                riskAssessment.setAcademicRisk(RiskLevel.LOW);
                application.addNote("Academic risk: LOW (Reputable international university)");
            } else {
                riskAssessment.setAcademicRisk(RiskLevel.MEDIUM);
                application.addNote("Academic risk: MEDIUM (Standard university assessment)");
            }
        } else {
            riskAssessment.setAcademicRisk(RiskLevel.MEDIUM);
        }
    }

    private void assessRepaymentCapability(ApplicationData application) {
        RiskAssessment riskAssessment = application.getRiskAssessment();
        FinancialInfo financialInfo = application.getFinancialInfo();

        BigDecimal currentBalance = financialInfo.getCurrentBalance();
        BigDecimal scholarshipAmount = financialInfo.getScholarshipAmount();

        if (currentBalance == null) currentBalance = BigDecimal.ZERO;
        if (scholarshipAmount == null) scholarshipAmount = BigDecimal.ZERO;

        // Repayment capability based on total financial support
        BigDecimal totalSupport = currentBalance.add(scholarshipAmount);
        BigDecimal highThreshold = new BigDecimal("200000000"); // 200M IDR
        BigDecimal mediumThreshold = new BigDecimal("100000000"); // 100M IDR

        if (totalSupport.compareTo(highThreshold) >= 0) {
            riskAssessment.setRepaymentCapability(RiskLevel.LOW);
            application.addNote("Repayment capability: LOW risk (Strong financial backing)");
        } else if (totalSupport.compareTo(mediumThreshold) >= 0) {
            riskAssessment.setRepaymentCapability(RiskLevel.MEDIUM);
            application.addNote("Repayment capability: MEDIUM risk (Moderate financial backing)");
        } else {
            riskAssessment.setRepaymentCapability(RiskLevel.HIGH);
            application.addNote("Repayment capability: HIGH risk (Limited financial backing)");
        }
    }

    private void calculateOverallRisk(ApplicationData application) {
        RiskAssessment riskAssessment = application.getRiskAssessment();

        // Calculate overall risk based on component risks
        int riskScore = 0;

        // Financial risk weight: 40%
        riskScore += getRiskScore(riskAssessment.getFinancialRisk()) * 4;

        // Repayment capability weight: 35%
        riskScore += getRiskScore(riskAssessment.getRepaymentCapability()) * 3.5;

        // Academic risk weight: 25%
        riskScore += getRiskScore(riskAssessment.getAcademicRisk()) * 2.5;

        if (riskScore <= 15) {
            riskAssessment.setOverallRisk(RiskLevel.LOW);
        } else if (riskScore <= 25) {
            riskAssessment.setOverallRisk(RiskLevel.MEDIUM);
        } else if (riskScore <= 35) {
            riskAssessment.setOverallRisk(RiskLevel.HIGH);
        } else {
            riskAssessment.setOverallRisk(RiskLevel.VERY_HIGH);
        }
    }

    private int getRiskScore(RiskLevel risk) {
        if (risk == null) return 2;
        return switch (risk) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
            case VERY_HIGH -> 4;
        };
    }

    private void generateRiskFactors(ApplicationData application) {
        RiskAssessment riskAssessment = application.getRiskAssessment();
        FinancialInfo financialInfo = application.getFinancialInfo();
        List<String> riskFactors = new ArrayList<>();

        if (riskAssessment.getFinancialRisk() == RiskLevel.HIGH) {
            riskFactors.add("Limited current financial resources relative to transition costs");
        }

        if (!financialInfo.isHasScholarship()) {
            riskFactors.add("No scholarship support identified");
        }

        if (application.getFundingRequirement().getUrgencyLevel() == UrgencyLevel.HIGH ||
                application.getFundingRequirement().getUrgencyLevel() == UrgencyLevel.CRITICAL) {
            riskFactors.add("High urgency due to approaching semester start date");
        }

        BigDecimal gap = financialInfo.getFinancialGap();
        if (gap != null && gap.compareTo(new BigDecimal("200000000")) > 0) {
            riskFactors.add("Large financial gap exceeds 200M IDR");
        }

        if (riskAssessment.getRepaymentCapability() == RiskLevel.HIGH) {
            riskFactors.add("Limited repayment capability based on current financial situation");
        }

        // Check for very low current balance
        BigDecimal currentBalance = financialInfo.getCurrentBalance();
        if (currentBalance != null && currentBalance.compareTo(new BigDecimal("25000000")) < 0) {
            riskFactors.add("Very low current bank balance (below 25M IDR)");
        }

        riskAssessment.setRiskFactors(riskFactors);
    }

    private void finalizeFundingRecommendation(ApplicationData application) {
        FundingRequirement fundingReq = application.getFundingRequirement();
        RiskAssessment riskAssessment = application.getRiskAssessment();

        BigDecimal baseRecommendation = fundingReq.getRecommendedFunding();
        if (baseRecommendation == null) {
            baseRecommendation = BigDecimal.ZERO;
        }

        // Adjust funding recommendation based on risk assessment
        if (riskAssessment.getOverallRisk() == RiskLevel.HIGH ||
                riskAssessment.getOverallRisk() == RiskLevel.VERY_HIGH) {
            // Add 10% buffer for high-risk cases
            BigDecimal buffer = baseRecommendation.multiply(new BigDecimal("0.10"));
            fundingReq.setRecommendedFunding(baseRecommendation.add(buffer));
            application.addNote("Funding adjusted: Added 10% buffer due to high risk assessment");
        }

        // Set emergency funding for critical urgency
        if (fundingReq.getUrgencyLevel() == UrgencyLevel.CRITICAL) {
            fundingReq.setFundingType(FundingType.EMERGENCY_COMPLETION);
            application.addNote("Funding type changed to EMERGENCY_COMPLETION due to critical urgency");
        }
    }

    private void validateNameConsistency(ApplicationData application) {
        PersonalInfo personalInfo = application.getPersonalInfo();
        FinancialInfo financialInfo = application.getFinancialInfo();
        AcademicInfo academicInfo = application.getAcademicInfo();

        String passportName = personalInfo.getFullName();
        String bankAccountName = financialInfo.getAccountHolderName();
        String universityStudentName = academicInfo.getUniversityName() != null ?
                "SARAH ELIZABETH JOHNSON" : null; // Placeholder logic

        List<String> nameWarnings = new ArrayList<>();

        if (passportName != null && bankAccountName != null) {
            if (!namesMatch(passportName, bankAccountName)) {
                nameWarnings.add("Name mismatch: Passport (" + passportName + ") vs Bank Account (" + bankAccountName + ")");
            }
        }

        if (!nameWarnings.isEmpty()) {
            application.addNote("Name validation warnings: " + String.join("; ", nameWarnings));
        } else if (passportName != null && bankAccountName != null) {
            application.addNote("Name validation: Consistent across documents");
        }
    }

    private boolean namesMatch(String name1, String name2) {
        if (name1 == null || name2 == null) return false;

        // Simple name matching - in production, use more sophisticated matching
        String normalized1 = name1.toUpperCase().replaceAll("[^A-Z\\s]", "").trim();
        String normalized2 = name2.toUpperCase().replaceAll("[^A-Z\\s]", "").trim();

        return normalized1.equals(normalized2) ||
                normalized1.contains(normalized2) ||
                normalized2.contains(normalized1);
    }

    // Additional utility methods

    /**
     * Get all applications (for admin dashboard)
     */
    public List<ApplicationData> getAllApplications() {
        return new ArrayList<>(applicationStorage.values());
    }

    /**
     * Update application status (admin function)
     */
    public ApplicationData updateApplicationStatus(String applicationId, ApplicationStatus newStatus) {
        ApplicationData application = getApplication(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("Application not found: " + applicationId);
        }

        ApplicationStatus oldStatus = application.getApplicationStatus();
        application.setApplicationStatus(newStatus);
        application.setLastUpdated(LocalDateTime.now());
        application.addNote("Status changed from " + oldStatus + " to " + newStatus);

        logger.info("Updated application {} status from {} to {}", applicationId, oldStatus, newStatus);
        return application;
    }

    /**
     * Add note to application
     */
    public ApplicationData addNote(String applicationId, String note) {
        ApplicationData application = getApplication(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("Application not found: " + applicationId);
        }

        application.addNote(note);
        application.setLastUpdated(LocalDateTime.now());

        return application;
    }

    /**
     * Get processing statistics
     */
    public Map<String, Object> getProcessingStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<ApplicationData> allApps = getAllApplications();

        long totalApplications = allApps.size();
        long completedApplications = allApps.stream()
                .mapToLong(app -> app.getDocumentsProcessed().isAllDocumentsProcessed() ? 1L : 0L)
                .sum();

        long approvedApplications = allApps.stream()
                .mapToLong(app -> app.getApplicationStatus() == ApplicationStatus.APPROVED ? 1L : 0L)
                .sum();

        BigDecimal totalFundingRequested = allApps.stream()
                .map(app -> app.getFundingRequirement().getRecommendedFunding())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.put("totalApplications", totalApplications);
        stats.put("completedApplications", completedApplications);
        stats.put("approvedApplications", approvedApplications);
        stats.put("totalFundingRequested", totalFundingRequested);
        stats.put("averageFundingRequest", totalApplications > 0 ?
                totalFundingRequested.divide(BigDecimal.valueOf(totalApplications), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO);

        return stats;
    }
}