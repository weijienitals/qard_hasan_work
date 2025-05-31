package com.example.qard_hasan_for_education.service;

import com.example.qard_hasan_for_education.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RepaymentService {

    private static final Logger logger = LoggerFactory.getLogger(RepaymentService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private VolunteeringService volunteeringService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Create a new loan account from approved application
     */
    public LoanAccount createLoanAccount(StudentApplicationData application, BigDecimal loanAmount, Integer termMonths) {
        logger.info("Creating loan account for application: {}", application.getApplicationId());

        LoanAccount loanAccount = new LoanAccount(
                application.getStudentId(),
                application.getApplicationId(),
                loanAmount,
                termMonths,
                application.getPassportInfo().getFullName(),
                application.getUniversityAcceptance().getUniversityName(),
                application.getUniversityAcceptance().getProgram(),
                determineUniversityCountry(application.getUniversityAcceptance().getUniversityName()),
                application.getPassportInfo().getNationality()
        );

        // Store loan account
        storeLoanAccount(loanAccount);

        logger.info("Loan account created: {} for student: {}, amount: {}, term: {} months",
                loanAccount.getLoanId(), loanAccount.getStudentId(), loanAmount, termMonths);

        return loanAccount;
    }

    /**
     * Process a repayment transaction
     */
    public RepaymentTransaction processRepayment(String loanId, BigDecimal amount, String paymentMethod) throws Exception {
        logger.info("Processing repayment for loan: {}, amount: {}", loanId, amount);

        LoanAccount loanAccount = getLoanAccount(loanId);
        if (loanAccount == null) {
            throw new Exception("Loan account not found: " + loanId);
        }

        if (loanAccount.getLoanStatus() != LoanStatus.ACTIVE) {
            throw new Exception("Loan is not active. Current status: " + loanAccount.getLoanStatus());
        }

        // Validate payment amount
        if (amount.compareTo(loanAccount.getMonthlyInstallment()) < 0) {
            throw new Exception("Payment amount is less than required installment: " + loanAccount.getMonthlyInstallment());
        }

        // Create transaction record
        RepaymentTransaction transaction = new RepaymentTransaction(
                loanId,
                loanAccount.getStudentId(),
                amount,
                loanAccount.getCompletedInstallments() + 1,
                paymentMethod
        );

        // Simulate payment processing (in real implementation, integrate with payment gateway)
        boolean paymentSuccessful = processPaymentWithGateway(transaction);

        if (paymentSuccessful) {
            transaction.setStatus(PaymentStatus.COMPLETED);

            // Update loan account
            updateLoanAccountAfterPayment(loanAccount, amount);

            // Store transaction
            storeTransaction(transaction);

            logger.info("Repayment successful for loan: {}, transaction: {}", loanId, transaction.getTransactionId());

            // Trigger mentoring offer asynchronously
            triggerMentoringOffer(loanAccount, transaction);

            return transaction;
        } else {
            transaction.setStatus(PaymentStatus.FAILED);
            storeTransaction(transaction);
            throw new Exception("Payment processing failed");
        }
    }

    /**
     * Get loan account details
     */
    public LoanAccount getLoanAccount(String loanId) {
        try {
            Object result = redisTemplate.opsForValue().get("loan:" + loanId);
            return result instanceof LoanAccount ? (LoanAccount) result : null;
        } catch (Exception e) {
            logger.error("Error retrieving loan account: {}", loanId, e);
            return null;
        }
    }

    /**
     * Get loan accounts for a student
     */
    public List<LoanAccount> getStudentLoans(String studentId) {
        try {
            Set<Object> loanIdsObj = redisTemplate.opsForSet().members("student_loans:" + studentId);
            if (loanIdsObj == null || loanIdsObj.isEmpty()) return Collections.emptyList();

            return loanIdsObj.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(loanId -> (LoanAccount) redisTemplate.opsForValue().get("loan:" + loanId))
                    .filter(Objects::nonNull)
                    .filter(loan -> studentId.equals(loan.getStudentId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error retrieving student loans for: {}", studentId, e);
            return Collections.emptyList();
        }
    }

    /**
     * Get repayment history for a loan
     */
    public List<RepaymentTransaction> getRepaymentHistory(String loanId) {
        try {
            Set<String> keys = redisTemplate.keys("transaction:*");
            if (keys == null) return Collections.emptyList();

            return keys.stream()
                    .filter(Objects::nonNull)
                    .map(key -> (RepaymentTransaction) redisTemplate.opsForValue().get(key))
                    .filter(Objects::nonNull)
                    .filter(txn -> loanId.equals(txn.getLoanId()))
                    .sorted((t1, t2) -> t2.getPaymentDate().compareTo(t1.getPaymentDate()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error retrieving repayment history for loan: {}", loanId, e);
            return Collections.emptyList();
        }
    }

    /**
     * Get upcoming payment due date and amount
     */
    public Map<String, Object> getUpcomingPayment(String loanId) {
        LoanAccount loanAccount = getLoanAccount(loanId);
        if (loanAccount == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> paymentInfo = new HashMap<>();
        paymentInfo.put("dueDate", loanAccount.getNextPaymentDate());
        paymentInfo.put("amount", loanAccount.getMonthlyInstallment());
        paymentInfo.put("remainingBalance", loanAccount.getRemainingBalance());
        paymentInfo.put("installmentNumber", loanAccount.getCompletedInstallments() + 1);
        paymentInfo.put("totalInstallments", loanAccount.getTotalInstallments());

        // Calculate days until due
        long daysUntilDue = Duration.between(LocalDate.now().atStartOfDay(),
                loanAccount.getNextPaymentDate().atStartOfDay()).toDays();
        paymentInfo.put("daysUntilDue", daysUntilDue);

        return paymentInfo;
    }

    private void updateLoanAccountAfterPayment(LoanAccount loanAccount, BigDecimal paymentAmount) {
        // Update remaining balance
        loanAccount.setRemainingBalance(loanAccount.getRemainingBalance().subtract(paymentAmount));

        // Update completed installments
        loanAccount.setCompletedInstallments(loanAccount.getCompletedInstallments() + 1);

        // Update next payment date
        loanAccount.setNextPaymentDate(loanAccount.getNextPaymentDate().plusMonths(1));

        // Check if loan is completed
        if (loanAccount.getCompletedInstallments() >= loanAccount.getTotalInstallments() ||
                loanAccount.getRemainingBalance().compareTo(BigDecimal.ZERO) <= 0) {
            loanAccount.setLoanStatus(LoanStatus.COMPLETED);
            loanAccount.setRemainingBalance(BigDecimal.ZERO);
        }

        loanAccount.setUpdatedAt(LocalDateTime.now());

        // Update stored loan account
        storeLoanAccount(loanAccount);

        logger.info("Loan account updated: {} - Remaining balance: {}, Completed installments: {}/{}",
                loanAccount.getLoanId(), loanAccount.getRemainingBalance(),
                loanAccount.getCompletedInstallments(), loanAccount.getTotalInstallments());
    }

    private void triggerMentoringOffer(LoanAccount loanAccount, RepaymentTransaction transaction) {
        try {
            // Check if student is eligible for mentoring (Indonesian studying abroad)
            if (loanAccount.isEligibleForMentoring()) {
                logger.info("Triggering mentoring offer for student: {} after payment: {}",
                        loanAccount.getStudentId(), transaction.getTransactionId());

                // Send mentoring offer through volunteering service
                boolean offerSent = volunteeringService.sendMentoringOffer(loanAccount, transaction);

                if (offerSent) {
                    transaction.setMentoringOfferSent(true);
                    storeTransaction(transaction);

                    // Send notification
                    notificationService.sendMentoringOfferNotification(loanAccount.getStudentId(), transaction);
                }
            } else {
                logger.debug("Student not eligible for mentoring: {} - Nationality: {}, University Country: {}",
                        loanAccount.getStudentId(), loanAccount.getNationality(), loanAccount.getUniversityCountry());
            }
        } catch (Exception e) {
            logger.error("Error triggering mentoring offer for loan: {}", loanAccount.getLoanId(), e);
        }
    }

    private boolean processPaymentWithGateway(RepaymentTransaction transaction) {
        // Simulate payment gateway processing
        // In real implementation, integrate with actual payment providers
        try {
            Thread.sleep(1000); // Simulate processing time

            // Simulate 95% success rate
            return Math.random() < 0.95;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void storeLoanAccount(LoanAccount loanAccount) {
        try {
            redisTemplate.opsForValue().set(
                    "loan:" + loanAccount.getLoanId(),
                    loanAccount,
                    Duration.ofDays(365) // Keep for 1 year after completion
            );

            // Also store by student ID for quick lookups
            redisTemplate.opsForSet().add("student_loans:" + loanAccount.getStudentId(), (Object) loanAccount.getLoanId());
        } catch (Exception e) {
            logger.error("Error storing loan account: {}", loanAccount.getLoanId(), e);
        }
    }

    private void storeTransaction(RepaymentTransaction transaction) {
        try {
            redisTemplate.opsForValue().set(
                    "transaction:" + transaction.getTransactionId(),
                    transaction,
                    Duration.ofDays(365) // Keep transaction records for 1 year
            );

            // Store in loan transaction list
            redisTemplate.opsForList().leftPush(
                    "loan_transactions:" + transaction.getLoanId(),
                    (Object) transaction.getTransactionId()
            );
        } catch (Exception e) {
            logger.error("Error storing transaction: {}", transaction.getTransactionId(), e);
        }
    }

    private String determineUniversityCountry(String universityName) {
        // Simple mapping - in real implementation, use a comprehensive database
        Map<String, String> universityCountryMap = Map.of(
                "Stanford University", "United States",
                "MIT", "United States",
                "Harvard University", "United States",
                "University of Oxford", "United Kingdom",
                "University of Cambridge", "United Kingdom",
                "ETH Zurich", "Switzerland",
                "University of Tokyo", "Japan",
                "National University of Singapore", "Singapore"
        );

        return universityCountryMap.getOrDefault(universityName, "Unknown");
    }
}