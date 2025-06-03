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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class RepaymentService {

    private static final Logger logger = LoggerFactory.getLogger(RepaymentService.class);
    private final Map<String, RepaymentTransaction> transactionCache = new ConcurrentHashMap<>();
    // Local cache for workaround
    private final Map<String, LoanAccount> loanCache = new ConcurrentHashMap<>();

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
     * Process a repayment transaction - Updated to return PaymentResult with offerId
     */
    public PaymentResult processRepayment(String loanId, BigDecimal amount, String paymentMethod) throws Exception {
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

            // Trigger mentoring offer asynchronously and get offerId
            String offerId = triggerMentoringOffer(loanAccount, transaction);

            // Create and return PaymentResult
            return new PaymentResult(transaction, transaction.isMentoringOfferSent(), offerId);
        } else {
            transaction.setStatus(PaymentStatus.FAILED);
            storeTransaction(transaction);
            throw new Exception("Payment processing failed");
        }
    }

    /**
     * Get loan account details - WITH WORKAROUND
     */
    public LoanAccount getLoanAccount(String loanId) {
        logger.info("=== Getting loan account for ID: {}", loanId);

        try {
            // First check local cache (immediate workaround)
            LoanAccount cachedLoan = loanCache.get(loanId);
            if (cachedLoan != null) {
                logger.info("=== Retrieved loan from local cache: {}", loanId);
                return cachedLoan;
            }

            // Then try Redis with manual conversion
            String key = "loan:" + loanId;
            Object result = redisTemplate.opsForValue().get(key);

            if (result == null) {
                logger.error("=== Loan not found in Redis: {}", loanId);
                return null;
            }

            // Handle LinkedHashMap to LoanAccount conversion
            if (result instanceof java.util.LinkedHashMap) {
                logger.info("=== Converting LinkedHashMap to LoanAccount for: {}", loanId);
                LoanAccount loanAccount = convertMapToLoanAccount((java.util.LinkedHashMap<String, Object>) result);
                if (loanAccount != null) {
                    // Cache it for future use
                    loanCache.put(loanId, loanAccount);
                    logger.info("=== Successfully converted and cached loan: {}", loanId);
                    return loanAccount;
                }
            } else if (result instanceof LoanAccount) {
                LoanAccount loanAccount = (LoanAccount) result;
                loanCache.put(loanId, loanAccount);
                logger.info("=== Retrieved loan directly from Redis: {}", loanId);
                return loanAccount;
            }

            logger.error("=== Unable to convert Redis object to LoanAccount: {}", result.getClass().getName());
            return null;

        } catch (Exception e) {
            logger.error("=== Exception retrieving loan account: {}", loanId, e);
            return null;
        }
    }

    /**
     * Helper method to convert LinkedHashMap to LoanAccount
     */
    private LoanAccount convertMapToLoanAccount(Map<String, Object> map) {
        try {
            LoanAccount loan = new LoanAccount();

            // Set basic fields
            loan.setLoanId((String) map.get("loanId"));
            loan.setStudentId((String) map.get("studentId"));
            loan.setApplicationId((String) map.get("applicationId"));
            loan.setStudentName((String) map.get("studentName"));
            loan.setUniversityName((String) map.get("universityName"));
            loan.setProgram((String) map.get("program"));
            loan.setUniversityCountry((String) map.get("universityCountry"));
            loan.setNationality((String) map.get("nationality"));

            // Handle BigDecimal fields
            if (map.get("principalAmount") != null) {
                loan.setPrincipalAmount(new BigDecimal(map.get("principalAmount").toString()));
            }
            if (map.get("remainingBalance") != null) {
                loan.setRemainingBalance(new BigDecimal(map.get("remainingBalance").toString()));
            }
            if (map.get("monthlyInstallment") != null) {
                loan.setMonthlyInstallment(new BigDecimal(map.get("monthlyInstallment").toString()));
            }

            // Handle Integer fields
            if (map.get("totalInstallments") != null) {
                loan.setTotalInstallments(((Number) map.get("totalInstallments")).intValue());
            }
            if (map.get("completedInstallments") != null) {
                loan.setCompletedInstallments(((Number) map.get("completedInstallments")).intValue());
            }

            // Handle Enum
            if (map.get("loanStatus") != null) {
                loan.setLoanStatus(LoanStatus.valueOf(map.get("loanStatus").toString()));
            }

            // Handle LocalDate fields (they come as [year, month, day] arrays)
            if (map.get("loanStartDate") instanceof List) {
                List<Integer> dateList = (List<Integer>) map.get("loanStartDate");
                loan.setLoanStartDate(LocalDate.of(dateList.get(0), dateList.get(1), dateList.get(2)));
            }

            if (map.get("nextPaymentDate") instanceof List) {
                List<Integer> dateList = (List<Integer>) map.get("nextPaymentDate");
                loan.setNextPaymentDate(LocalDate.of(dateList.get(0), dateList.get(1), dateList.get(2)));
            }

            // Handle LocalDateTime fields (they come as [year, month, day, hour, minute, second, nano] arrays)
            if (map.get("createdAt") instanceof List) {
                List<Integer> dateTimeList = (List<Integer>) map.get("createdAt");
                loan.setCreatedAt(LocalDateTime.of(
                        dateTimeList.get(0), dateTimeList.get(1), dateTimeList.get(2),
                        dateTimeList.get(3), dateTimeList.get(4), dateTimeList.get(5),
                        dateTimeList.size() > 6 ? dateTimeList.get(6) : 0
                ));
            }

            if (map.get("updatedAt") instanceof List) {
                List<Integer> dateTimeList = (List<Integer>) map.get("updatedAt");
                loan.setUpdatedAt(LocalDateTime.of(
                        dateTimeList.get(0), dateTimeList.get(1), dateTimeList.get(2),
                        dateTimeList.get(3), dateTimeList.get(4), dateTimeList.get(5),
                        dateTimeList.size() > 6 ? dateTimeList.get(6) : 0
                ));
            }

            return loan;

        } catch (Exception e) {
            logger.error("Error converting map to LoanAccount", e);
            return null;
        }
    }

    /**
     * Store loan account with local caching
     */
    private void storeLoanAccount(LoanAccount loanAccount) {
        try {
            String key = "loan:" + loanAccount.getLoanId();
            logger.info("Storing loan account: {}", loanAccount.getLoanId());

            // Store in Redis
            redisTemplate.opsForValue().set(key, loanAccount, Duration.ofDays(365));

            // Store in local cache (workaround)
            loanCache.put(loanAccount.getLoanId(), loanAccount);

            // Store student mapping
            redisTemplate.opsForSet().add("student_loans:" + loanAccount.getStudentId(),
                    (Object) loanAccount.getLoanId());

            logger.info("Successfully stored loan account in Redis and cache: {}", loanAccount.getLoanId());

        } catch (Exception e) {
            logger.error("Error storing loan account: {}", loanAccount.getLoanId(), e);
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
                    .map(this::getLoanAccount) // Use our fixed getLoanAccount method
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
//    public List<RepaymentTransaction> getRepaymentHistory(String loanId) {
//        try {
//            Set<String> keys = redisTemplate.keys("transaction:*");
//            if (keys == null) return Collections.emptyList();
//
//            return keys.stream()
//                    .filter(Objects::nonNull)
//                    .map(key -> (RepaymentTransaction) redisTemplate.opsForValue().get(key))
//                    .filter(Objects::nonNull)
//                    .filter(txn -> loanId.equals(txn.getLoanId()))
//                    .sorted((t1, t2) -> t2.getPaymentDate().compareTo(t1.getPaymentDate()))
//                    .collect(Collectors.toList());
//        } catch (Exception e) {
//            logger.error("Error retrieving repayment history for loan: {}", loanId, e);
//            return Collections.emptyList();
//        }
//    }

    public List<RepaymentTransaction> getRepaymentHistory(String loanId) {
        logger.info("=== Getting repayment history for loan: {}", loanId);

        try {
            Set<String> keys = redisTemplate.keys("transaction:*");
            if (keys == null) {
                logger.warn("No transaction keys found in Redis");
                return Collections.emptyList();
            }

            logger.info("Found {} transaction keys in Redis", keys.size());

            List<RepaymentTransaction> transactions = keys.stream()
                    .filter(Objects::nonNull)
                    .map(key -> {
                        try {
                            // Check cache first
                            String transactionId = key.replace("transaction:", "");
                            RepaymentTransaction cached = transactionCache.get(transactionId);
                            if (cached != null) {
                                return cached;
                            }

                            Object result = redisTemplate.opsForValue().get(key);

                            if (result == null) {
                                logger.warn("Transaction key {} returned null", key);
                                return null;
                            }

                            // Handle LinkedHashMap to RepaymentTransaction conversion
                            if (result instanceof java.util.LinkedHashMap) {
                                logger.debug("Converting LinkedHashMap to RepaymentTransaction for key: {}", key);
                                RepaymentTransaction transaction = convertMapToRepaymentTransaction((java.util.LinkedHashMap<String, Object>) result);
                                if (transaction != null) {
                                    transactionCache.put(transactionId, transaction);
                                }
                                return transaction;
                            } else if (result instanceof RepaymentTransaction) {
                                RepaymentTransaction transaction = (RepaymentTransaction) result;
                                transactionCache.put(transactionId, transaction);
                                return transaction;
                            } else {
                                logger.warn("Unexpected object type for key {}: {}", key, result.getClass().getName());
                                return null;
                            }
                        } catch (Exception e) {
                            logger.error("Error processing transaction key: {}", key, e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .filter(txn -> loanId.equals(txn.getLoanId()))
                    .sorted((t1, t2) -> t2.getPaymentDate().compareTo(t1.getPaymentDate()))
                    .collect(Collectors.toList());

            logger.info("Retrieved {} transactions for loan: {}", transactions.size(), loanId);
            return transactions;

        } catch (Exception e) {
            logger.error("Error retrieving repayment history for loan: {}", loanId, e);
            return Collections.emptyList();
        }
    }


    /**
     * ADD this new method to convert LinkedHashMap to RepaymentTransaction:
     */
    private RepaymentTransaction convertMapToRepaymentTransaction(Map<String, Object> map) {
        try {
            RepaymentTransaction transaction = new RepaymentTransaction();

            // Set basic fields
            transaction.setTransactionId((String) map.get("transactionId"));
            transaction.setLoanId((String) map.get("loanId"));
            transaction.setStudentId((String) map.get("studentId"));
            transaction.setPaymentMethod((String) map.get("paymentMethod"));
            transaction.setReferenceNumber((String) map.get("referenceNumber"));

            // Handle BigDecimal fields
            if (map.get("amount") != null) {
                transaction.setAmount(new BigDecimal(map.get("amount").toString()));
            }

            // Handle Integer fields
            if (map.get("installmentNumber") != null) {
                transaction.setInstallmentNumber(((Number) map.get("installmentNumber")).intValue());
            }

            // Handle Enum fields
            if (map.get("status") != null) {
                transaction.setStatus(PaymentStatus.valueOf(map.get("status").toString()));
            }

            // Handle Boolean fields
            if (map.get("mentoringOfferSent") != null) {
                transaction.setMentoringOfferSent((Boolean) map.get("mentoringOfferSent"));
            }

            if (map.get("mentoringOfferAccepted") != null) {
                transaction.setMentoringOfferAccepted((Boolean) map.get("mentoringOfferAccepted"));
            }

            // Handle LocalDateTime fields (they come as [year, month, day, hour, minute, second, nano] arrays)
            if (map.get("paymentDate") instanceof List) {
                List<Integer> dateTimeList = (List<Integer>) map.get("paymentDate");
                transaction.setPaymentDate(LocalDateTime.of(
                        dateTimeList.get(0), dateTimeList.get(1), dateTimeList.get(2),
                        dateTimeList.get(3), dateTimeList.get(4), dateTimeList.get(5),
                        dateTimeList.size() > 6 ? dateTimeList.get(6) : 0
                ));
            }

            return transaction;

        } catch (Exception e) {
            logger.error("Error converting map to RepaymentTransaction", e);
            return null;
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

        // Update stored loan account (this will update both Redis and cache)
        storeLoanAccount(loanAccount);

        logger.info("Loan account updated: {} - Remaining balance: {}, Completed installments: {}/{}",
                loanAccount.getLoanId(), loanAccount.getRemainingBalance(),
                loanAccount.getCompletedInstallments(), loanAccount.getTotalInstallments());
    }

    /**
     * Trigger mentoring offer and return offerId
     */
    private String triggerMentoringOffer(LoanAccount loanAccount, RepaymentTransaction transaction) {
        try {
            // Check if student is eligible for mentoring (Indonesian studying abroad)
            if (loanAccount.isEligibleForMentoring()) {
                logger.info("Triggering mentoring offer for student: {} after payment: {}",
                        loanAccount.getStudentId(), transaction.getTransactionId());

                // Send mentoring offer through volunteering service and get offerId
                String offerId = volunteeringService.sendMentoringOffer(loanAccount, transaction);

                if (offerId != null) {
                    transaction.setMentoringOfferSent(true);
                    storeTransaction(transaction);

                    // Send notification
                    notificationService.sendMentoringOfferNotification(loanAccount.getStudentId(), transaction);

                    return offerId;
                }
            } else {
                logger.debug("Student not eligible for mentoring: {} - Nationality: {}, University Country: {}",
                        loanAccount.getStudentId(), loanAccount.getNationality(), loanAccount.getUniversityCountry());
            }
        } catch (Exception e) {
            logger.error("Error triggering mentoring offer for loan: {}", loanAccount.getLoanId(), e);
        }

        return null;
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

//    private void storeTransaction(RepaymentTransaction transaction) {
//        try {
//            redisTemplate.opsForValue().set(
//                    "transaction:" + transaction.getTransactionId(),
//                    transaction,
//                    Duration.ofDays(365) // Keep transaction records for 1 year
//            );
//
//            // Store in loan transaction list
//            redisTemplate.opsForList().leftPush(
//                    "loan_transactions:" + transaction.getLoanId(),
//                    (Object) transaction.getTransactionId()
//            );
//        } catch (Exception e) {
//            logger.error("Error storing transaction: {}", transaction.getTransactionId(), e);
//        }
//    }

    /**
     * REPLACE your existing storeTransaction method with this enhanced version:
     */
    private void storeTransaction(RepaymentTransaction transaction) {
        try {
            String key = "transaction:" + transaction.getTransactionId();
            logger.info("Storing transaction: {} for loan: {}", transaction.getTransactionId(), transaction.getLoanId());

            // Store in Redis
            redisTemplate.opsForValue().set(key, transaction, Duration.ofDays(365));

            // Store in local cache
            transactionCache.put(transaction.getTransactionId(), transaction);

            // Store in loan transaction list
            redisTemplate.opsForList().leftPush(
                    "loan_transactions:" + transaction.getLoanId(),
                    (Object) transaction.getTransactionId()
            );

            // Verify storage immediately
            Object stored = redisTemplate.opsForValue().get(key);
            logger.info("Transaction stored successfully: {} (type: {})",
                    transaction.getTransactionId(),
                    stored != null ? stored.getClass().getSimpleName() : "null");

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

    /**
     * PaymentResult class to hold transaction and offerId
     */
    public static class PaymentResult {
        private final RepaymentTransaction transaction;
        private final boolean mentoringOfferSent;
        private final String offerId;

        public PaymentResult(RepaymentTransaction transaction, boolean mentoringOfferSent, String offerId) {
            this.transaction = transaction;
            this.mentoringOfferSent = mentoringOfferSent;
            this.offerId = offerId;
        }

        public RepaymentTransaction getTransaction() {
            return transaction;
        }

        public boolean isMentoringOfferSent() {
            return mentoringOfferSent;
        }

        public String getOfferId() {
            return offerId;
        }
    }
}