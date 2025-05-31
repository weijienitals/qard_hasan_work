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

@Service
public class MentorshipService {

    private static final Logger logger = LoggerFactory.getLogger(MentorshipService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private NotificationService notificationService;

    /**
     * Create a mentee profile for students seeking help
     */
    public MenteeProfile createMenteeProfile(String studentId, String name, String university,
                                             String program, Integer currentYear, String currentCountry,
                                             List<HelpType> needsHelpWith, String urgencyLevel,
                                             String description) {
        logger.info("Creating mentee profile for student: {}", studentId);

        MenteeProfile menteeProfile = new MenteeProfile(studentId, name, university, program, currentYear, currentCountry);
        menteeProfile.setNeedsHelpWith(needsHelpWith);
        menteeProfile.setUrgencyLevel(urgencyLevel);
        menteeProfile.setDescription(description);

        storeMenteeProfile(menteeProfile);

        logger.info("Mentee profile created: {} for student: {}", menteeProfile.getMenteeId(), studentId);
        return menteeProfile;
    }

    /**
     * Match mentor with mentee based on preferences and availability
     */
    public MentorshipMatch createMentorshipMatch(String mentorId, String menteeId, HelpType helpType) throws Exception {
        logger.info("Creating mentorship match: mentor={}, mentee={}, helpType={}", mentorId, menteeId, helpType);

        // Validate mentor
        MentorProfile mentor = getMentorProfile(mentorId);
        if (mentor == null) {
            throw new Exception("Mentor not found: " + mentorId);
        }

        if (!mentor.canAcceptMoreMentees()) {
            throw new Exception("Mentor has reached maximum mentee capacity");
        }

        if (!mentor.getAvailableHelpTypes().contains(helpType)) {
            throw new Exception("Mentor does not offer this type of help: " + helpType);
        }

        // Validate mentee
        MenteeProfile mentee = getMenteeProfile(menteeId);
        if (mentee == null) {
            throw new Exception("Mentee not found: " + menteeId);
        }

        if (!mentee.isActive()) {
            throw new Exception("Mentee profile is not active");
        }

        // Check for existing active match
        if (hasActiveMatch(mentorId, menteeId)) {
            throw new Exception("Active mentorship already exists between these users");
        }

        // Create the match
        MentorshipMatch match = new MentorshipMatch(mentorId, menteeId, helpType);
        storeMentorshipMatch(match);

        // Update mentor and mentee statistics
        updateMentorStats(mentor, 1, 0);
        mentee.setLastActiveAt(LocalDateTime.now());
        storeMenteeProfile(mentee);

        // Send notifications
        notificationService.sendMentorshipMatchNotification(mentorId, menteeId, match.getMatchId());

        logger.info("Mentorship match created: {}", match.getMatchId());
        return match;
    }

    /**
     * Get all matches for a mentor
     */
    public List<MentorshipMatch> getMentorMatches(String mentorId) {
        try {
            Set<String> keys = redisTemplate.keys("match:*");
            if (keys == null) return Collections.emptyList();

            return keys.stream()
                    .filter(Objects::nonNull)
                    .map(key -> (MentorshipMatch) redisTemplate.opsForValue().get(key))
                    .filter(Objects::nonNull)
                    .filter(match -> mentorId.equals(match.getMentorId()))
                    .sorted((m1, m2) -> m2.getMatchedAt().compareTo(m1.getMatchedAt()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error retrieving mentor matches for: {}", mentorId, e);
            return Collections.emptyList();
        }
    }

    /**
     * Get all matches for a mentee
     */
    public List<MentorshipMatch> getMenteeMatches(String menteeId) {
        try {
            Set<String> keys = redisTemplate.keys("match:*");
            if (keys == null) return Collections.emptyList();

            return keys.stream()
                    .filter(Objects::nonNull)
                    .map(key -> (MentorshipMatch) redisTemplate.opsForValue().get(key))
                    .filter(Objects::nonNull)
                    .filter(match -> menteeId.equals(match.getMenteeId()))
                    .sorted((m1, m2) -> m2.getMatchedAt().compareTo(m1.getMatchedAt()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error retrieving mentee matches for: {}", menteeId, e);
            return Collections.emptyList();
        }
    }

    /**
     * Record a mentoring session
     */
    public MentorshipMatch recordSession(String matchId, String sessionNotes) throws Exception {
        MentorshipMatch match = getMentorshipMatch(matchId);
        if (match == null) {
            throw new Exception("Mentorship match not found: " + matchId);
        }

        if (match.getStatus() != MentorshipStatus.ACTIVE) {
            throw new Exception("Cannot record session for inactive mentorship");
        }

        // Update match
        match.setTotalSessions(match.getTotalSessions() + 1);
        match.setLastInteractionAt(LocalDateTime.now());
        if (sessionNotes != null && !sessionNotes.trim().isEmpty()) {
            String existingNotes = match.getNotes() != null ? match.getNotes() : "";
            match.setNotes(existingNotes + "\n[" + LocalDateTime.now() + "] " + sessionNotes);
        }

        storeMentorshipMatch(match);

        // Update mentor statistics
        MentorProfile mentor = getMentorProfile(match.getMentorId());
        if (mentor != null) {
            updateMentorStats(mentor, 0, 1);
        }

        logger.info("Session recorded for match: {}, total sessions: {}", matchId, match.getTotalSessions());
        return match;
    }

    /**
     * Rate a mentorship (by mentee rating mentor, or mentor rating mentee)
     */
    public MentorshipMatch rateMentorship(String matchId, int rating, boolean isMenteeRating) throws Exception {
        if (rating < 1 || rating > 5) {
            throw new Exception("Rating must be between 1 and 5");
        }

        MentorshipMatch match = getMentorshipMatch(matchId);
        if (match == null) {
            throw new Exception("Mentorship match not found: " + matchId);
        }

        if (isMenteeRating) {
            match.setMenteeRating(rating);
            logger.info("Mentee rated mentorship: {} - Rating: {}", matchId, rating);
        } else {
            match.setMentorRating(rating);
            logger.info("Mentor rated mentorship: {} - Rating: {}", matchId, rating);
        }

        storeMentorshipMatch(match);

        // Update mentor's average rating if mentee rated
        if (isMenteeRating) {
            updateMentorAverageRating(match.getMentorId());
        }

        return match;
    }

    /**
     * Complete a mentorship
     */
    public MentorshipMatch completeMentorship(String matchId, String completionReason) throws Exception {
        MentorshipMatch match = getMentorshipMatch(matchId);
        if (match == null) {
            throw new Exception("Mentorship match not found: " + matchId);
        }

        match.setStatus(MentorshipStatus.COMPLETED);
        match.setLastInteractionAt(LocalDateTime.now());

        if (completionReason != null) {
            String existingNotes = match.getNotes() != null ? match.getNotes() : "";
            match.setNotes(existingNotes + "\n[COMPLETED] " + completionReason);
        }

        storeMentorshipMatch(match);

        // Update mentor's current mentee count
        MentorProfile mentor = getMentorProfile(match.getMentorId());
        if (mentor != null) {
            updateMentorStats(mentor, -1, 0);
        }

        // Send completion notification
        notificationService.sendMentorshipCompletionNotification(match.getMentorId(), match.getMenteeId(), matchId);

        logger.info("Mentorship completed: {}", matchId);
        return match;
    }

    /**
     * Pause a mentorship temporarily
     */
    public MentorshipMatch pauseMentorship(String matchId, String reason) throws Exception {
        MentorshipMatch match = getMentorshipMatch(matchId);
        if (match == null) {
            throw new Exception("Mentorship match not found: " + matchId);
        }

        match.setStatus(MentorshipStatus.PAUSED);
        match.setLastInteractionAt(LocalDateTime.now());

        if (reason != null) {
            String existingNotes = match.getNotes() != null ? match.getNotes() : "";
            match.setNotes(existingNotes + "\n[PAUSED] " + reason);
        }

        storeMentorshipMatch(match);

        logger.info("Mentorship paused: {} - Reason: {}", matchId, reason);
        return match;
    }

    /**
     * Resume a paused mentorship
     */
    public MentorshipMatch resumeMentorship(String matchId) throws Exception {
        MentorshipMatch match = getMentorshipMatch(matchId);
        if (match == null) {
            throw new Exception("Mentorship match not found: " + matchId);
        }

        if (match.getStatus() != MentorshipStatus.PAUSED) {
            throw new Exception("Can only resume paused mentorships");
        }

        match.setStatus(MentorshipStatus.ACTIVE);
        match.setLastInteractionAt(LocalDateTime.now());

        String existingNotes = match.getNotes() != null ? match.getNotes() : "";
        match.setNotes(existingNotes + "\n[RESUMED] " + LocalDateTime.now());

        storeMentorshipMatch(match);

        logger.info("Mentorship resumed: {}", matchId);
        return match;
    }

    /**
     * Get mentorship statistics for dashboard
     */
    public Map<String, Object> getMentorshipStatistics() {
        try {
            Set<String> matchKeys = redisTemplate.keys("match:*");
            Set<String> mentorKeys = redisTemplate.keys("mentor:*");
            Set<String> menteeKeys = redisTemplate.keys("mentee:*");

            List<MentorshipMatch> allMatches = matchKeys != null ?
                    matchKeys.stream()
                            .filter(Objects::nonNull)
                            .map(key -> (MentorshipMatch) redisTemplate.opsForValue().get(key))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()) : Collections.emptyList();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalMentors", mentorKeys != null ? mentorKeys.size() : 0);
            stats.put("totalMentees", menteeKeys != null ? menteeKeys.size() : 0);
            stats.put("totalMatches", allMatches.size());
            stats.put("activeMatches", allMatches.stream().filter(m -> m.getStatus() == MentorshipStatus.ACTIVE).count());
            stats.put("completedMatches", allMatches.stream().filter(m -> m.getStatus() == MentorshipStatus.COMPLETED).count());
            stats.put("totalSessions", allMatches.stream().mapToInt(MentorshipMatch::getTotalSessions).sum());

            return stats;

        } catch (Exception e) {
            logger.error("Error calculating mentorship statistics", e);
            return Collections.emptyMap();
        }
    }

    /**
     * Check if there's an active match between mentor and mentee
     */
    public boolean hasActiveMatch(String mentorId, String menteeId) {
        try {
            Set<String> keys = redisTemplate.keys("match:*");
            if (keys == null) return false;

            return keys.stream()
                    .filter(Objects::nonNull)
                    .map(key -> (MentorshipMatch) redisTemplate.opsForValue().get(key))
                    .filter(Objects::nonNull)
                    .anyMatch(match -> mentorId.equals(match.getMentorId()) &&
                            menteeId.equals(match.getMenteeId()) &&
                            match.getStatus() == MentorshipStatus.ACTIVE);
        } catch (Exception e) {
            logger.error("Error checking for active match between {} and {}", mentorId, menteeId, e);
            return false;
        }
    }

    // Private helper methods

    private MentorProfile getMentorProfile(String mentorId) {
        try {
            Object result = redisTemplate.opsForValue().get("mentor:" + mentorId);
            return result instanceof MentorProfile ? (MentorProfile) result : null;
        } catch (Exception e) {
            logger.error("Error retrieving mentor profile: {}", mentorId, e);
            return null;
        }
    }

    private MenteeProfile getMenteeProfile(String menteeId) {
        try {
            Object result = redisTemplate.opsForValue().get("mentee:" + menteeId);
            return result instanceof MenteeProfile ? (MenteeProfile) result : null;
        } catch (Exception e) {
            logger.error("Error retrieving mentee profile: {}", menteeId, e);
            return null;
        }
    }

    private MentorshipMatch getMentorshipMatch(String matchId) {
        try {
            Object result = redisTemplate.opsForValue().get("match:" + matchId);
            return result instanceof MentorshipMatch ? (MentorshipMatch) result : null;
        } catch (Exception e) {
            logger.error("Error retrieving mentorship match: {}", matchId, e);
            return null;
        }
    }

    private void storeMenteeProfile(MenteeProfile mentee) {
        try {
            redisTemplate.opsForValue().set(
                    "mentee:" + mentee.getMenteeId(),
                    mentee,
                    Duration.ofDays(365)
            );
        } catch (Exception e) {
            logger.error("Error storing mentee profile: {}", mentee.getMenteeId(), e);
        }
    }

    private void storeMentorshipMatch(MentorshipMatch match) {
        try {
            redisTemplate.opsForValue().set(
                    "match:" + match.getMatchId(),
                    match,
                    Duration.ofDays(365)
            );

            // Index by mentor and mentee for quick lookups
            redisTemplate.opsForList().leftPush("mentor_matches:" + match.getMentorId(), (Object) match.getMatchId());
            redisTemplate.opsForList().leftPush("mentee_matches:" + match.getMenteeId(), (Object) match.getMatchId());
        } catch (Exception e) {
            logger.error("Error storing mentorship match: {}", match.getMatchId(), e);
        }
    }

    private void updateMentorStats(MentorProfile mentor, int menteeCountChange, int sessionCountChange) {
        mentor.setCurrentMentees(Math.max(0, mentor.getCurrentMentees() + menteeCountChange));
        mentor.setTotalMentoringSessions(mentor.getTotalMentoringSessions() + sessionCountChange);
        mentor.setLastActiveAt(LocalDateTime.now());

        try {
            redisTemplate.opsForValue().set(
                    "mentor:" + mentor.getMentorId(),
                    mentor,
                    Duration.ofDays(365)
            );
        } catch (Exception e) {
            logger.error("Error updating mentor stats: {}", mentor.getMentorId(), e);
        }
    }

    private void updateMentorAverageRating(String mentorId) {
        try {
            List<MentorshipMatch> mentorMatches = getMentorMatches(mentorId);

            List<Integer> ratings = mentorMatches.stream()
                    .map(MentorshipMatch::getMenteeRating)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!ratings.isEmpty()) {
                double averageRating = ratings.stream()
                        .mapToInt(Integer::intValue)
                        .average()
                        .orElse(0.0);

                MentorProfile mentor = getMentorProfile(mentorId);
                if (mentor != null) {
                    mentor.setAverageRating(Math.round(averageRating * 100.0) / 100.0);
                    updateMentorStats(mentor, 0, 0);
                }
            }
        } catch (Exception e) {
            logger.error("Error updating mentor average rating: {}", mentorId, e);
        }
    }
}