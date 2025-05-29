// RiskAssessmentService.java
package com.example.qard_hasan_for_education.service;

import com.example.qard_hasan_for_education.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.example.qard_hasan_for_education.service.AmountVerificationService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class RiskAssessmentService {

    private static final Logger logger = LoggerFactory.getLogger(RiskAssessmentService.class);

    public ApplicationRiskProfile assessRisk(StudentApplicationData application) {
        logger.info("Starting risk assessment for application: {}", application.getApplicationId());

        ApplicationRiskProfile riskProfile = new ApplicationRiskProfile();

        try {
            // Assess individual risk categories
            FinancialRisk financialRisk = assessFinancialRisk(application.getBankInfo());
            AcademicRisk academicRisk = assessAcademicRisk(application.getUniversityAcceptance(), application.getScholarshipAcceptance());
            FraudRisk fraudRisk = assessFraudRisk(application);

            riskProfile.setFinancialRisk(financialRisk);
            riskProfile.setAcademicRisk(academicRisk);
            riskProfile.setFraudRisk(fraudRisk);

            // Calculate overall risk and score
            calculateOverallRisk(riskProfile);

            // Generate recommendations
            generateRecommendations(riskProfile);

            logger.info("Risk assessment completed for application: {} - Overall Risk: {}, Score: {}",
                    application.getApplicationId(), riskProfile.getOverallRisk(), riskProfile.getRiskScore());

        } catch (Exception e) {
            logger.error("Error during risk assessment for application: {}", application.getApplicationId(), e);
            // Set default high risk in case of error
            riskProfile.setOverallRisk(RiskLevel.HIGH);
            riskProfile.setRiskScore(90);
            riskProfile.setApprovalRecommendation(ApprovalRecommendation.FURTHER_REVIEW);
            riskProfile.setRecommendations(Arrays.asList("Risk assessment failed - requires manual review"));
        }

        return riskProfile;
    }

    private FinancialRisk assessFinancialRisk(SimpleBankInfo bankInfo) {
        FinancialRisk financialRisk = new FinancialRisk();

        if (bankInfo == null) {
            financialRisk.setRiskLevel(RiskLevel.HIGH);
            financialRisk.setRepaymentCapacity("unknown");
            financialRisk.setRiskFactors(Arrays.asList("No bank information available"));
            return financialRisk;
        }

        List<String> riskFactors = new ArrayList<>(bankInfo.getRiskFactors() != null ? bankInfo.getRiskFactors() : new ArrayList<>());

        // Copy AI-assessed fields
        financialRisk.setIncomeStability(bankInfo.getIncomeStability());
        financialRisk.setExpenseRatio(bankInfo.getExpenseRatio());
        financialRisk.setSavingsTrend(bankInfo.getSavingsTrend());
        financialRisk.setOverdraftCount(bankInfo.getOverdraftCount());
        financialRisk.setRepaymentCapacity(bankInfo.getRepaymentCapacity());

        // Determine risk level based on multiple factors
        int riskScore = 0;

        // Income stability assessment
        if ("irregular".equals(bankInfo.getIncomeStability()) || "declining".equals(bankInfo.getIncomeStability())) {
            riskScore += 30;
            riskFactors.add("Unstable income pattern");
        }

        // Expense ratio assessment
        if (bankInfo.getExpenseRatio() != null) {
            if (bankInfo.getExpenseRatio().compareTo(new BigDecimal("80")) > 0) {
                riskScore += 25;
                riskFactors.add("High expense ratio (>80%)");
            } else if (bankInfo.getExpenseRatio().compareTo(new BigDecimal("60")) > 0) {
                riskScore += 15;
                riskFactors.add("Moderate expense ratio (60-80%)");
            }
        }

        // Savings trend assessment
        if ("decreasing".equals(bankInfo.getSavingsTrend())) {
            riskScore += 20;
            riskFactors.add("Decreasing savings trend");
        }

        // Overdraft assessment
        if (bankInfo.getOverdraftCount() != null && bankInfo.getOverdraftCount() > 0) {
            riskScore += bankInfo.getOverdraftCount() * 10;
            riskFactors.add("Recent overdrafts: " + bankInfo.getOverdraftCount());
        }

        // Current balance assessment
        if (bankInfo.getCurrentBalance() != null && bankInfo.getCurrentBalance().compareTo(BigDecimal.ZERO) <= 0) {
            riskScore += 40;
            riskFactors.add("Zero or negative current balance");
        }

        // NEW: Amount verification risk assessment
        if (application.getAmountVerification() != null) {
            AmountVerification amtVerif = application.getAmountVerification();

            // Check if requested amount is excessive
            if ("excessive".equals(amtVerif.getFundingGapAnalysis().getRequestedVsNeeded())) {
                riskScore += 20;
                riskFactors.add("Requested amount significantly exceeds funding need");
            }

            // Check affordability
            if ("unaffordable".equals(amtVerif.getAffordabilityCheck().getAffordabilityStatus())) {
                riskScore += 25;
                riskFactors.add("Requested amount unaffordable based on income");
            } else if ("tight".equals(amtVerif.getAffordabilityCheck().getAffordabilityStatus())) {
                riskScore += 10;
                riskFactors.add("Requested amount creates tight repayment schedule");
            }
        }

        // Determine risk level
        if (riskScore >= 60) {
            financialRisk.setRiskLevel(RiskLevel.HIGH);
        } else if (riskScore >= 30) {
            financialRisk.setRiskLevel(RiskLevel.MEDIUM);
        } else {
            financialRisk.setRiskLevel(RiskLevel.LOW);
        }

        financialRisk.setRiskFactors(riskFactors);
        return financialRisk;
    }

    private AcademicRisk assessAcademicRisk(UniversityAcceptance university, ScholarshipAcceptance scholarship) {
        AcademicRisk academicRisk = new AcademicRisk();

        if (university == null) {
            academicRisk.setRiskLevel(RiskLevel.HIGH);
            academicRisk.setRiskFactors(Arrays.asList("No university acceptance information"));
            return academicRisk;
        }

        List<String> riskFactors = new ArrayList<>(university.getRiskFactors() != null ? university.getRiskFactors() : new ArrayList<>());

        // Copy AI-assessed fields
        academicRisk.setUniversityTier(university.getUniversityTier());
        academicRisk.setProgramMarketability(university.getProgramMarketability());
        academicRisk.setCompletionProbability(university.getCompletionProbability());

        int riskScore = 0;

        // University tier assessment
        if ("lower-tier".equals(university.getUniversityTier())) {
            riskScore += 20;
            riskFactors.add("Lower-tier university");
        } else if ("mid-tier".equals(university.getUniversityTier())) {
            riskScore += 10;
        }

        // Program marketability assessment
        if ("low".equals(university.getProgramMarketability())) {
            riskScore += 25;
            riskFactors.add("Low program marketability");
        } else if ("medium".equals(university.getProgramMarketability())) {
            riskScore += 10;
        }

        // Completion probability assessment
        if ("low".equals(university.getCompletionProbability())) {
            riskScore += 30;
            riskFactors.add("Low completion probability");
        } else if ("medium".equals(university.getCompletionProbability())) {
            riskScore += 15;
        }

        // Funding gap assessment
        String fundingGapRisk = assessFundingGap(university, scholarship);
        academicRisk.setFundingGapRisk(fundingGapRisk);

        if ("high".equals(fundingGapRisk)) {
            riskScore += 25;
            riskFactors.add("High funding gap risk");
        } else if ("medium".equals(fundingGapRisk)) {
            riskScore += 15;
            riskFactors.add("Medium funding gap risk");
        }

        // Determine risk level
        if (riskScore >= 50) {
            academicRisk.setRiskLevel(RiskLevel.HIGH);
        } else if (riskScore >= 25) {
            academicRisk.setRiskLevel(RiskLevel.MEDIUM);
        } else {
            academicRisk.setRiskLevel(RiskLevel.LOW);
        }

        academicRisk.setRiskFactors(riskFactors);
        return academicRisk;
    }

    private String assessFundingGap(UniversityAcceptance university, ScholarshipAcceptance scholarship) {
        if (scholarship == null || scholarship.getAmount() == null) {
            return "high"; // No scholarship = high funding gap
        }

        // If scholarship covers less than 50% of typical costs, it's high risk
        BigDecimal scholarshipAmount = scholarship.getAmount();
        BigDecimal typicalCost = new BigDecimal("75000"); // Typical annual cost from your presentation

        if (scholarshipAmount.compareTo(typicalCost.multiply(new BigDecimal("0.7"))) >= 0) {
            return "low"; // >70% covered
        } else if (scholarshipAmount.compareTo(typicalCost.multiply(new BigDecimal("0.4"))) >= 0) {
            return "medium"; // 40-70% covered
        } else {
            return "high"; // <40% covered
        }
    }

    private FraudRisk assessFraudRisk(StudentApplicationData application) {
        FraudRisk fraudRisk = new FraudRisk();
        List<String> riskFactors = new ArrayList<>();

        // Name consistency check
        boolean nameConsistent = checkNameConsistency(application);
        fraudRisk.setNameConsistency(nameConsistent);
        if (!nameConsistent) {
            riskFactors.add("Inconsistent names across documents");
        }

        // Date consistency check
        boolean dateConsistent = checkDateConsistency(application);
        fraudRisk.setDateConsistency(dateConsistent);
        if (!dateConsistent) {
            riskFactors.add("Inconsistent or illogical dates");
        }

        // Document authenticity from AI assessment
        String docAuthenticity = "verified"; // Default
        if (application.getScholarshipAcceptance() != null) {
            docAuthenticity = application.getScholarshipAcceptance().getDocumentAuthenticity();
            if ("suspicious".equals(docAuthenticity) || "likely-fake".equals(docAuthenticity)) {
                riskFactors.add("Suspicious document authenticity");
            }
        }
        fraudRisk.setDocumentAuthenticity(docAuthenticity);

        // Institution validation
        String institutionValidation = validateInstitutions(application);
        fraudRisk.setInstitutionValidation(institutionValidation);
        if ("questionable".equals(institutionValidation) || "invalid".equals(institutionValidation)) {
            riskFactors.add("Questionable institution credentials");
        }

        // Determine overall fraud risk
        int fraudScore = riskFactors.size() * 25; // Each risk factor adds 25 points

        if (fraudScore >= 50 || "likely-fake".equals(docAuthenticity)) {
            fraudRisk.setRiskLevel(RiskLevel.HIGH);
        } else if (fraudScore >= 25 || "suspicious".equals(docAuthenticity)) {
            fraudRisk.setRiskLevel(RiskLevel.MEDIUM);
        } else {
            fraudRisk.setRiskLevel(RiskLevel.LOW);
        }

        fraudRisk.setRiskFactors(riskFactors);
        return fraudRisk;
    }

    private boolean checkNameConsistency(StudentApplicationData application) {
        String passportName = application.getPassportInfo() != null ?
                application.getPassportInfo().getFullName() : null;
        String universityName = application.getUniversityAcceptance() != null ?
                application.getUniversityAcceptance().getStudentName() : null;
        String bankName = application.getBankInfo() != null ?
                application.getBankInfo().getAccountHolderName() : null;
        String scholarshipName = application.getScholarshipAcceptance() != null ?
                application.getScholarshipAcceptance().getRecipientName() : null;

        // Simple name consistency check (could be enhanced with fuzzy matching)
        if (passportName != null && universityName != null) {
            if (!normalizeNameForComparison(passportName).equals(normalizeNameForComparison(universityName))) {
                return false;
            }
        }

        return true; // Default to consistent if we can't verify
    }

    private String normalizeNameForComparison(String name) {
        return name != null ? name.toLowerCase().replaceAll("[^a-z]", "") : "";
    }

    private boolean checkDateConsistency(StudentApplicationData application) {
        // Check if acceptance date is before semester start, etc.
        // This is a simplified check - could be enhanced
        return true; // Default to consistent
    }

    private String validateInstitutions(StudentApplicationData application) {
        // This would typically check against a database of accredited institutions
        // For now, return "verified" as default
        return "verified";
    }

    private void calculateOverallRisk(ApplicationRiskProfile riskProfile) {
        int totalScore = 0;
        int highRiskCount = 0;

        // Financial risk weighting (40%)
        if (riskProfile.getFinancialRisk().getRiskLevel() == RiskLevel.HIGH) {
            totalScore += 40;
            highRiskCount++;
        } else if (riskProfile.getFinancialRisk().getRiskLevel() == RiskLevel.MEDIUM) {
            totalScore += 20;
        }

        // Academic risk weighting (35%)
        if (riskProfile.getAcademicRisk().getRiskLevel() == RiskLevel.HIGH) {
            totalScore += 35;
            highRiskCount++;
        } else if (riskProfile.getAcademicRisk().getRiskLevel() == RiskLevel.MEDIUM) {
            totalScore += 17;
        }

        // Fraud risk weighting (25%)
        if (riskProfile.getFraudRisk().getRiskLevel() == RiskLevel.HIGH) {
            totalScore += 25;
            highRiskCount++;
        } else if (riskProfile.getFraudRisk().getRiskLevel() == RiskLevel.MEDIUM) {
            totalScore += 12;
        }

        riskProfile.setRiskScore(totalScore);

        // Determine overall risk level
        if (highRiskCount >= 2 || totalScore >= 70) {
            riskProfile.setOverallRisk(RiskLevel.HIGH);
        } else if (totalScore >= 35) {
            riskProfile.setOverallRisk(RiskLevel.MEDIUM);
        } else {
            riskProfile.setOverallRisk(RiskLevel.LOW);
        }
    }

    private void generateRecommendations(ApplicationRiskProfile riskProfile) {
        List<String> recommendations = new ArrayList<>();

        switch (riskProfile.getOverallRisk()) {
            case LOW:
                recommendations.add("Approve with standard terms");
                recommendations.add("Monitor repayment progress monthly");
                riskProfile.setApprovalRecommendation(ApprovalRecommendation.APPROVE);
                break;

            case MEDIUM:
                recommendations.add("Approve with additional safeguards");
                if (riskProfile.getFinancialRisk().getRiskLevel() == RiskLevel.MEDIUM) {
                    recommendations.add("Require guarantor or co-signer");
                    recommendations.add("Implement bi-weekly repayment monitoring");
                }
                if (riskProfile.getAcademicRisk().getRiskLevel() == RiskLevel.MEDIUM) {
                    recommendations.add("Provide additional academic support resources");
                }
                riskProfile.setApprovalRecommendation(ApprovalRecommendation.APPROVE_WITH_CONDITIONS);
                break;

            case HIGH:
                recommendations.add("High risk - recommend rejection or strict conditions");
                if (riskProfile.getFraudRisk().getRiskLevel() == RiskLevel.HIGH) {
                    recommendations.add("Investigate document authenticity thoroughly");
                    riskProfile.setApprovalRecommendation(ApprovalRecommendation.REJECT);
                } else {
                    recommendations.add("Consider approval only with substantial collateral");
                    recommendations.add("Require multiple guarantors");
                    recommendations.add("Implement weekly monitoring");
                    riskProfile.setApprovalRecommendation(ApprovalRecommendation.FURTHER_REVIEW);
                }
                break;
        }

        riskProfile.setRecommendations(recommendations);
    }




}