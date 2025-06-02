package com.example.qard_hasan_for_education.service;

import com.example.qard_hasan_for_education.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.time.format.DateTimeFormatter;

@Service
public class VolunteeringService {

    private static final Logger logger = LoggerFactory.getLogger(VolunteeringService.class);
    private final Map<String, MentoringOffer> offerCache = new ConcurrentHashMap<>();

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private MentorshipService mentorshipService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Send mentoring offer to student after successful payment
     */
    public boolean sendMentoringOffer(LoanAccount loanAccount, RepaymentTransaction transaction) {
        try {
            logger.info("Sending mentoring offer to student: {} after payment: {}",
                    loanAccount.getStudentId(), transaction.getTransactionId());

            // Create mentoring offer record
            MentoringOffer offer = new MentoringOffer(
                    loanAccount.getStudentId(),
                    transaction.getTransactionId(),
                    loanAccount.getLoanId(),
                    transaction.getInstallmentNumber()
            );

            // Store the offer
            storeMentoringOffer(offer);

            logger.info("Mentoring offer sent successfully: {}", offer.getOfferId());
            return true;

        } catch (Exception e) {
            logger.error("Error sending mentoring offer to student: {}", loanAccount.getStudentId(), e);
            return false;
        }
    }

    /**
     * Student accepts mentoring offer
     */
    public MentorProfile acceptMentoringOffer(String offerId, List<HelpType> availableHelpTypes,
                                              String bio, String contactPreference) throws Exception {
        MentoringOffer offer = getMentoringOffer(offerId);
        if (offer == null) {
            throw new Exception("Mentoring offer not found: " + offerId);
        }

        if (offer.isExpired()) {
            throw new Exception("Mentoring offer has expired");
        }

        if (offer.getResponse() != null) {
            throw new Exception("Mentoring offer already responded to");
        }

        // Mark offer as accepted
        offer.setResponse(MentoringOfferResponse.ACCEPTED);
        offer.setRespondedAt(LocalDateTime.now());
        storeMentoringOffer(offer);

        // Get loan account for student details
        LoanAccount loanAccount = getLoanAccountForStudent(offer.getStudentId());
        if (loanAccount == null) {
            throw new Exception("Loan account not found for student: " + offer.getStudentId());
        }

        // Create or update mentor profile
        MentorProfile mentorProfile = getOrCreateMentorProfile(loanAccount, availableHelpTypes, bio, contactPreference);

        // Update repayment transaction
        updateTransactionMentoringResponse(offer.getTransactionId(), true);

        logger.info("Mentoring offer accepted by student: {}, mentor profile: {}",
                offer.getStudentId(), mentorProfile.getMentorId());

        return mentorProfile;
    }

    /**
     * Student declines mentoring offer
     */
    public void declineMentoringOffer(String offerId, String reason) throws Exception {
        MentoringOffer offer = getMentoringOffer(offerId);
        if (offer == null) {
            throw new Exception("Mentoring offer not found: " + offerId);
        }

        if (offer.getResponse() != null) {
            throw new Exception("Mentoring offer already responded to");
        }

        // Mark offer as declined
        offer.setResponse(MentoringOfferResponse.DECLINED);
        offer.setRespondedAt(LocalDateTime.now());
        offer.setDeclineReason(reason);
        storeMentoringOffer(offer);

        // Update repayment transaction
        updateTransactionMentoringResponse(offer.getTransactionId(), false);

        logger.info("Mentoring offer declined by student: {}, reason: {}", offer.getStudentId(), reason);
    }

    /**
     * Get potential mentees for a mentor
     */
    public List<MenteeProfile> getPotentialMentees(String mentorId, List<HelpType> helpTypes) {
        MentorProfile mentor = getMentorProfile(mentorId);
        if (mentor == null || !mentor.canAcceptMoreMentees()) {
            return Collections.emptyList();
        }

        List<MenteeProfile> allMentees = getAllActiveMentees();

        return allMentees.stream()
                .filter(mentee -> !mentorshipService.hasActiveMatch(mentorId, mentee.getMenteeId()))
                .filter(mentee -> hasMatchingHelpTypes(mentee.getNeedsHelpWith(), helpTypes))
                .sorted(this::compareMenteesForMatching)
                .limit(10) // Return top 10 matches
                .collect(Collectors.toList());
    }

    /**
     * Get mentor's volunteering statistics
     */
    public Map<String, Object> getMentorStats(String mentorId) {
        MentorProfile mentor = getMentorProfile(mentorId);
        if (mentor == null) {
            return Collections.emptyMap();
        }

        List<MentorshipMatch> matches = mentorshipService.getMentorMatches(mentorId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMentees", matches.size());
        stats.put("activeMentees", matches.stream().filter(m -> m.getStatus() == MentorshipStatus.ACTIVE).count());
        stats.put("completedMentorships", matches.stream().filter(m -> m.getStatus() == MentorshipStatus.COMPLETED).count());
        stats.put("totalSessions", mentor.getTotalMentoringSessions());
        stats.put("averageRating", mentor.getAverageRating());
        stats.put("joinedAt", mentor.getJoinedAt());
        stats.put("lastActiveAt", mentor.getLastActiveAt());

        return stats;
    }

    /**
     * Update mentor availability and preferences
     */
    public MentorProfile updateMentorPreferences(String mentorId, List<HelpType> helpTypes,
                                                 Integer maxMentees, String bio,
                                                 List<String> timeSlots, boolean isActive) throws Exception {
        MentorProfile mentor = getMentorProfile(mentorId);
        if (mentor == null) {
            throw new Exception("Mentor profile not found: " + mentorId);
        }

        mentor.setAvailableHelpTypes(helpTypes);
        mentor.setMaxMentees(maxMentees);
        mentor.setBio(bio);
        mentor.setAvailableTimeSlots(timeSlots);
        mentor.setActive(isActive);
        mentor.setLastActiveAt(LocalDateTime.now());

        storeMentorProfile(mentor);

        logger.info("Mentor preferences updated: {}", mentorId);
        return mentor;
    }

    // Private helper methods

    private MentorProfile getOrCreateMentorProfile(LoanAccount loanAccount, List<HelpType> helpTypes,
                                                   String bio, String contactPreference) {
        // Check if mentor profile already exists
        MentorProfile existingProfile = getMentorProfileByStudentId(loanAccount.getStudentId());

        if (existingProfile != null) {
            // Update existing profile
            existingProfile.setAvailableHelpTypes(helpTypes);
            existingProfile.setBio(bio);
            existingProfile.setContactPreference(contactPreference);
            existingProfile.setActive(true);
            existingProfile.setLastActiveAt(LocalDateTime.now());
            storeMentorProfile(existingProfile);
            return existingProfile;
        }

        // Create new mentor profile
        MentorProfile newProfile = new MentorProfile(
                loanAccount.getStudentId(),
                loanAccount.getStudentName(),
                loanAccount.getUniversityName(),
                loanAccount.getProgram(),
                null, // Will be updated if needed
                loanAccount.getUniversityCountry()
        );

        newProfile.setAvailableHelpTypes(helpTypes);
        newProfile.setBio(bio);
        newProfile.setContactPreference(contactPreference);

        storeMentorProfile(newProfile);
        return newProfile;
    }

    private MentorProfile getMentorProfile(String mentorId) {
        try {
            Object result = redisTemplate.opsForValue().get("mentor:" + mentorId);
            return result instanceof MentorProfile ? (MentorProfile) result : null;
        } catch (Exception e) {
            logger.error("Error retrieving mentor profile: {}", mentorId, e);
            return null;
        }
    }

    private MentorProfile getMentorProfileByStudentId(String studentId) {
        try {
            Set<String> keys = redisTemplate.keys("mentor:*");
            if (keys == null) return null;

            return keys.stream()
                    .filter(Objects::nonNull)
                    .map(key -> (MentorProfile) redisTemplate.opsForValue().get(key))
                    .filter(Objects::nonNull)
                    .filter(mentor -> studentId.equals(mentor.getStudentId()))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            logger.error("Error retrieving mentor profile by student ID: {}", studentId, e);
            return null;
        }
    }

    private List<MenteeProfile> getAllActiveMentees() {
        try {
            Set<String> keys = redisTemplate.keys("mentee:*");
            if (keys == null) return Collections.emptyList();

            return keys.stream()
                    .filter(Objects::nonNull)
                    .map(key -> (MenteeProfile) redisTemplate.opsForValue().get(key))
                    .filter(Objects::nonNull)
                    .filter(MenteeProfile::isActive)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error retrieving active mentees", e);
            return Collections.emptyList();
        }
    }

    private boolean hasMatchingHelpTypes(List<HelpType> needed, List<HelpType> available) {
        if (needed == null || available == null) return false;
        return needed.stream().anyMatch(available::contains);
    }

    private int compareMenteesForMatching(MenteeProfile a, MenteeProfile b) {
        // Prioritize by urgency level first
        Map<String, Integer> urgencyPriority = Map.of("high", 3, "medium", 2, "low", 1);
        int urgencyCompare = urgencyPriority.getOrDefault(b.getUrgencyLevel(), 0) -
                urgencyPriority.getOrDefault(a.getUrgencyLevel(), 0);

        if (urgencyCompare != 0) return urgencyCompare;

        // Then by how recently they joined (newer first)
        return b.getJoinedAt().compareTo(a.getJoinedAt());
    }

    private void storeMentorProfile(MentorProfile mentor) {
        try {
            redisTemplate.opsForValue().set(
                    "mentor:" + mentor.getMentorId(),
                    mentor,
                    Duration.ofDays(365)
            );

            // Index by student ID for quick lookups
            redisTemplate.opsForValue().set(
                    "mentor_by_student:" + mentor.getStudentId(),
                    (Object) mentor.getMentorId(),
                    Duration.ofDays(365)
            );
        } catch (Exception e) {
            logger.error("Error storing mentor profile: {}", mentor.getMentorId(), e);
        }
    }

//    private void storeMentoringOffer(MentoringOffer offer) {
//        try {
//            redisTemplate.opsForValue().set(
//                    "offer:" + offer.getOfferId(),
//                    offer,
//                    Duration.ofDays(30) // Keep offers for 30 days
//            );
//        } catch (Exception e) {
//            logger.error("Error storing mentoring offer: {}", offer.getOfferId(), e);
//        }
//    }

//    private MentoringOffer getMentoringOffer(String offerId) {
//        try {
//            Object result = redisTemplate.opsForValue().get("offer:" + offerId);
//            return result instanceof MentoringOffer ? (MentoringOffer) result : null;
//        } catch (Exception e) {
//            logger.error("Error retrieving mentoring offer: {}", offerId, e);
//            return null;
//        }
//    }

    private MentoringOffer getMentoringOffer(String offerId) {
        logger.info("=== Getting mentoring offer for ID: {}", offerId);

        try {
            // First check local cache
            MentoringOffer cachedOffer = offerCache.get(offerId);
            if (cachedOffer != null) {
                logger.info("=== Retrieved offer from local cache: {}", offerId);
                return cachedOffer;
            }

            // Then try Redis with manual conversion
            String key = "offer:" + offerId;
            Object result = redisTemplate.opsForValue().get(key);

            if (result == null) {
                logger.error("=== Mentoring offer not found in Redis: {}", offerId);
                return null;
            }

            // Handle LinkedHashMap to MentoringOffer conversion
            if (result instanceof java.util.LinkedHashMap) {
                logger.info("=== Converting LinkedHashMap to MentoringOffer for: {}", offerId);
                MentoringOffer offer = convertMapToMentoringOffer((java.util.LinkedHashMap<String, Object>) result);
                if (offer != null) {
                    // Cache it for future use
                    offerCache.put(offerId, offer);
                    logger.info("=== Successfully converted and cached offer: {}", offerId);
                    return offer;
                }
            } else if (result instanceof MentoringOffer) {
                MentoringOffer offer = (MentoringOffer) result;
                offerCache.put(offerId, offer);
                logger.info("=== Retrieved offer directly from Redis: {}", offerId);
                return offer;
            }

            logger.error("=== Unable to convert Redis object to MentoringOffer: {}", result.getClass().getName());
            return null;

        } catch (Exception e) {
            logger.error("=== Exception retrieving mentoring offer: {}", offerId, e);
            return null;
        }
    }

    // Add this helper method to convert LinkedHashMap to MentoringOffer:
    private MentoringOffer convertMapToMentoringOffer(Map<String, Object> map) {
        try {
            MentoringOffer offer = new MentoringOffer();

            // Set basic fields
            offer.setOfferId((String) map.get("offerId"));
            offer.setStudentId((String) map.get("studentId"));
            offer.setTransactionId((String) map.get("transactionId"));
            offer.setLoanId((String) map.get("loanId"));

            // Handle Integer fields
            if (map.get("installmentNumber") != null) {
                offer.setInstallmentNumber(((Number) map.get("installmentNumber")).intValue());
            }

            // Handle Enum fields
            if (map.get("response") != null) {
                offer.setResponse(MentoringOfferResponse.valueOf(map.get("response").toString()));
            }

            offer.setDeclineReason((String) map.get("declineReason"));

            // Handle LocalDateTime fields (they come as [year, month, day, hour, minute, second, nano] arrays)
            if (map.get("createdAt") instanceof List) {
                List<Integer> dateTimeList = (List<Integer>) map.get("createdAt");
                offer.setCreatedAt(LocalDateTime.of(
                        dateTimeList.get(0), dateTimeList.get(1), dateTimeList.get(2),
                        dateTimeList.get(3), dateTimeList.get(4), dateTimeList.get(5),
                        dateTimeList.size() > 6 ? dateTimeList.get(6) : 0
                ));
            }

            if (map.get("expiresAt") instanceof List) {
                List<Integer> dateTimeList = (List<Integer>) map.get("expiresAt");
                offer.setExpiresAt(LocalDateTime.of(
                        dateTimeList.get(0), dateTimeList.get(1), dateTimeList.get(2),
                        dateTimeList.get(3), dateTimeList.get(4), dateTimeList.get(5),
                        dateTimeList.size() > 6 ? dateTimeList.get(6) : 0
                ));
            }

            if (map.get("respondedAt") instanceof List) {
                List<Integer> dateTimeList = (List<Integer>) map.get("respondedAt");
                offer.setRespondedAt(LocalDateTime.of(
                        dateTimeList.get(0), dateTimeList.get(1), dateTimeList.get(2),
                        dateTimeList.get(3), dateTimeList.get(4), dateTimeList.get(5),
                        dateTimeList.size() > 6 ? dateTimeList.get(6) : 0
                ));
            }

            return offer;

        } catch (Exception e) {
            logger.error("Error converting map to MentoringOffer", e);
            return null;
        }
    }

    // Also update the storeMentoringOffer method to include caching:
    private void storeMentoringOffer(MentoringOffer offer) {
        try {
            String key = "offer:" + offer.getOfferId();
            logger.info("Storing mentoring offer: {}", offer.getOfferId());

            // Store in Redis
            redisTemplate.opsForValue().set(key, offer, Duration.ofDays(30));

            // Store in local cache (workaround)
            offerCache.put(offer.getOfferId(), offer);

            logger.info("Successfully stored mentoring offer in Redis and cache: {}", offer.getOfferId());

        } catch (Exception e) {
            logger.error("Error storing mentoring offer: {}", offer.getOfferId(), e);
        }
    }

    private LoanAccount getLoanAccountForStudent(String studentId) {
        try {
            Set<Object> loanIdsObj = redisTemplate.opsForSet().members("student_loans:" + studentId);
            if (loanIdsObj == null || loanIdsObj.isEmpty()) return null;

            // Convert Object set to String set and return the most recent active loan
            return loanIdsObj.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(loanId -> (LoanAccount) redisTemplate.opsForValue().get("loan:" + loanId))
                    .filter(Objects::nonNull)
                    .filter(loan -> loan.getLoanStatus() == LoanStatus.ACTIVE)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            logger.error("Error retrieving loan account for student: {}", studentId, e);
            return null;
        }
    }

    private void updateTransactionMentoringResponse(String transactionId, boolean accepted) {
        try {
            RepaymentTransaction transaction = (RepaymentTransaction) redisTemplate.opsForValue()
                    .get("transaction:" + transactionId);
            if (transaction != null) {
                transaction.setMentoringOfferAccepted(accepted);
                redisTemplate.opsForValue().set("transaction:" + transactionId, transaction, Duration.ofDays(365));
            }
        } catch (Exception e) {
            logger.error("Error updating transaction mentoring response: {}", transactionId, e);
        }
    }

    // Inner classes for mentoring offer management

    public static class MentoringOffer {
        private String offerId;
        private String studentId;
        private String transactionId;
        private String loanId;
        private Integer installmentNumber;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private MentoringOfferResponse response;
        private LocalDateTime respondedAt;
        private String declineReason;

        public MentoringOffer() {}

        public MentoringOffer(String studentId, String transactionId, String loanId, Integer installmentNumber) {
            this.offerId = "OFFER_" + System.currentTimeMillis() + "_" +
                    UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            this.studentId = studentId;
            this.transactionId = transactionId;
            this.loanId = loanId;
            this.installmentNumber = installmentNumber;
            this.createdAt = LocalDateTime.now();
            this.expiresAt = LocalDateTime.now().plusDays(7); // Offer expires in 7 days
        }

        // Getters and setters
        public String getOfferId() { return offerId; }
        public void setOfferId(String offerId) { this.offerId = offerId; }

        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }

        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

        public String getLoanId() { return loanId; }
        public void setLoanId(String loanId) { this.loanId = loanId; }

        public Integer getInstallmentNumber() { return installmentNumber; }
        public void setInstallmentNumber(Integer installmentNumber) { this.installmentNumber = installmentNumber; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

        public MentoringOfferResponse getResponse() { return response; }
        public void setResponse(MentoringOfferResponse response) { this.response = response; }

        public LocalDateTime getRespondedAt() { return respondedAt; }
        public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }

        public String getDeclineReason() { return declineReason; }
        public void setDeclineReason(String declineReason) { this.declineReason = declineReason; }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
    }

    public enum MentoringOfferResponse {
        ACCEPTED,
        DECLINED
    }
}