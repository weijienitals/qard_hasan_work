package com.example.qard_hasan_for_education.service;

import com.example.qard_hasan_for_education.model.AffordabilityCheck;
import com.example.qard_hasan_for_education.model.AmountVerification;
import com.example.qard_hasan_for_education.model.FundingGapAnalysis;
import com.example.qard_hasan_for_education.model.StudentApplicationData;

import java.math.BigDecimal;

public class AmountVerificationService {
    public AmountVerification verifyRequestedAmount(
            BigDecimal requestedAmount,
            StudentApplicationData application) {

        AmountVerification verification = new AmountVerification();
        verification.setRequestedAmount(requestedAmount);

        // 1. Analyze funding gap
        FundingGapAnalysis gapAnalysis = analyzeFundingGap(requestedAmount, application);
        verification.setFundingGapAnalysis(gapAnalysis);

        // 2. Check affordability
        AffordabilityCheck affordabilityCheck = checkAffordability(requestedAmount, application);
        verification.setAffordabilityCheck(affordabilityCheck);

        // 3. Determine overall verification status
        determineVerificationStatus(verification);

        return verification;
    }

    private FundingGapAnalysis analyzeFundingGap(BigDecimal requested, StudentApplicationData app) {
        FundingGapAnalysis analysis = new FundingGapAnalysis();

        // Estimate total education cost based on university/country
        BigDecimal estimatedCost = estimateEducationCost(app.getUniversityAcceptance());
        analysis.setEstimatedTotalCost(estimatedCost);

        // Get scholarship amount
        BigDecimal scholarshipAmount = app.getScholarshipAcceptance() != null ?
                app.getScholarshipAcceptance().getAmount() : BigDecimal.ZERO;
        analysis.setScholarshipAmount(scholarshipAmount);

        // Calculate actual funding needed
        BigDecimal actualNeeded = estimatedCost.subtract(scholarshipAmount);
        analysis.setActualFundingNeeded(actualNeeded);

        // Compare requested vs needed
        if (requested.compareTo(actualNeeded.multiply(new BigDecimal("1.1"))) > 0) {
            analysis.setRequestedVsNeeded("excessive"); // >110% of needed
        } else if (requested.compareTo(actualNeeded.multiply(new BigDecimal("0.8"))) < 0) {
            analysis.setRequestedVsNeeded("insufficient"); // <80% of needed
        } else {
            analysis.setRequestedVsNeeded("adequate");
        }

        return analysis;
    }

    private AffordabilityCheck checkAffordability(BigDecimal requested, StudentApplicationData app) {
        AffordabilityCheck check = new AffordabilityCheck();

        // Get monthly income from bank info
        BigDecimal monthlyIncome = app.getBankInfo().getMonthlyIncome();
        BigDecimal monthlyExpenses = app.getBankInfo().getMonthlyExpenses();

        // Calculate repayment capacity (30% of disposable income)
        BigDecimal disposableIncome = monthlyIncome.subtract(monthlyExpenses);
        BigDecimal repaymentCapacity = disposableIncome.multiply(new BigDecimal("0.3"));
        check.setMonthlyRepaymentCapacity(repaymentCapacity);

        // Estimate monthly payment (assuming 5-year repayment)
        BigDecimal estimatedPayment = requested.divide(new BigDecimal("60"), 2, BigDecimal.ROUND_HALF_UP);
        check.setEstimatedMonthlyPayment(estimatedPayment);

        // Calculate affordability ratio
        BigDecimal affordabilityRatio = estimatedPayment.divide(repaymentCapacity, 2, BigDecimal.ROUND_HALF_UP);
        check.setAffordabilityRatio(affordabilityRatio);

        // Determine affordability status
        if (affordabilityRatio.compareTo(BigDecimal.ONE) <= 0) {
            check.setAffordabilityStatus("affordable");
        } else if (affordabilityRatio.compareTo(new BigDecimal("1.3")) <= 0) {
            check.setAffordabilityStatus("tight");
        } else {
            check.setAffordabilityStatus("unaffordable");
        }

        return check;
    }
}
