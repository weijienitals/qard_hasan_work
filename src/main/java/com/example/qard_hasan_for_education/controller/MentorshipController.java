package com.example.qard_hasan_for_education.controller;

import com.example.qard_hasan_for_education.model.*;
import com.example.qard_hasan_for_education.service.MentorshipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mentorship")
@CrossOrigin(origins = "*")
public class MentorshipController {

    private static final Logger logger = LoggerFactory.getLogger(MentorshipController.class);

    @Autowired
    private MentorshipService mentorshipService;

    /**
     * Create a mentee profile for students seeking help
     */
    @PostMapping("/create-mentee-profile")
    public ResponseEntity<?> createMenteeProfile(@RequestBody CreateMenteeRequest request) {
        try {
            MenteeProfile menteeProfile = mentorshipService.createMenteeProfile(
                    request.getStudentId(),
                    request.getName(),
                    request.getUniversity(),
                    request.getProgram(),
                    request.getCurrentYear(),
                    request.getCurrentCountry(),
                    request.getNeedsHelpWith(),
                    request.getUrgencyLevel(),
                    request.getDescription()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mentee profile created successfully! We'll find suitable mentors for you.");
            response.put("menteeProfile", menteeProfile);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error creating mentee profile", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error creating mentee profile: " + e.getMessage()));
        }
    }

    /**
     * Create a mentorship match between mentor and mentee
     */
    @PostMapping("/create-match")
    public ResponseEntity<?> createMentorshipMatch(@RequestBody CreateMatchRequest request) {
        try {
            MentorshipMatch match = mentorshipService.createMentorshipMatch(
                    request.getMentorId(),
                    request.getMenteeId(),
                    request.getHelpType()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mentorship match created successfully!");
            response.put("match", match);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error creating mentorship match", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Get all matches for a mentor
     */
    @GetMapping("/mentor-matches/{mentorId}")
    public ResponseEntity<?> getMentorMatches(@PathVariable String mentorId) {
        try {
            List<MentorshipMatch> matches = mentorshipService.getMentorMatches(mentorId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("matches", matches);
            response.put("totalMatches", matches.size());
            response.put("activeMatches", matches.stream().filter(m -> m.getStatus() == MentorshipStatus.ACTIVE).count());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving mentor matches", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error retrieving mentor matches"));
        }
    }

    /**
     * Get all matches for a mentee
     */
    @GetMapping("/mentee-matches/{menteeId}")
    public ResponseEntity<?> getMenteeMatches(@PathVariable String menteeId) {
        try {
            List<MentorshipMatch> matches = mentorshipService.getMenteeMatches(menteeId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("matches", matches);
            response.put("totalMatches", matches.size());
            response.put("activeMatches", matches.stream().filter(m -> m.getStatus() == MentorshipStatus.ACTIVE).count());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving mentee matches", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error retrieving mentee matches"));
        }
    }

    /**
     * Record a mentoring session
     */
    @PostMapping("/record-session")
    public ResponseEntity<?> recordSession(@RequestBody RecordSessionRequest request) {
        try {
            MentorshipMatch updatedMatch = mentorshipService.recordSession(
                    request.getMatchId(),
                    request.getSessionNotes()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Session recorded successfully!");
            response.put("match", updatedMatch);
            response.put("totalSessions", updatedMatch.getTotalSessions());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error recording session", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Rate a mentorship
     */
    @PostMapping("/rate")
    public ResponseEntity<?> rateMentorship(@RequestBody RateMentorshipRequest request) {
        try {
            MentorshipMatch updatedMatch = mentorshipService.rateMentorship(
                    request.getMatchId(),
                    request.getRating(),
                    request.isMenteeRating()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Thank you for your rating!");
            response.put("match", updatedMatch);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error rating mentorship", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Complete a mentorship
     */
    @PostMapping("/complete")
    public ResponseEntity<?> completeMentorship(@RequestBody CompleteMentorshipRequest request) {
        try {
            MentorshipMatch completedMatch = mentorshipService.completeMentorship(
                    request.getMatchId(),
                    request.getCompletionReason()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mentorship completed successfully! Thank you for making a difference.");
            response.put("match", completedMatch);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error completing mentorship", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Pause a mentorship
     */
    @PostMapping("/pause")
    public ResponseEntity<?> pauseMentorship(@RequestBody PauseMentorshipRequest request) {
        try {
            MentorshipMatch pausedMatch = mentorshipService.pauseMentorship(
                    request.getMatchId(),
                    request.getReason()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mentorship paused. You can resume it anytime.");
            response.put("match", pausedMatch);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error pausing mentorship", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Resume a paused mentorship
     */
    @PostMapping("/resume/{matchId}")
    public ResponseEntity<?> resumeMentorship(@PathVariable String matchId) {
        try {
            MentorshipMatch resumedMatch = mentorshipService.resumeMentorship(matchId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mentorship resumed successfully!");
            response.put("match", resumedMatch);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error resuming mentorship", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Get mentorship match details
     */
    @GetMapping("/match/{matchId}")
    public ResponseEntity<?> getMentorshipMatch(@PathVariable String matchId) {
        try {
            // This would call a service method to get match details
            // For now, we'll indicate that this would be implemented
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Match details retrieved");
            // response.put("match", match);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving match details", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error retrieving match details"));
        }
    }

    /**
     * Get mentorship statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getMentorshipStatistics() {
        try {
            Map<String, Object> stats = mentorshipService.getMentorshipStatistics();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", stats);
            response.put("message", "Mentorship program statistics");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving mentorship statistics", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error retrieving statistics"));
        }
    }

    // Request DTOs
    public static class CreateMenteeRequest {
        private String studentId;
        private String name;
        private String university;
        private String program;
        private Integer currentYear;
        private String currentCountry;
        private List<HelpType> needsHelpWith;
        private String urgencyLevel;
        private String description;

        // Getters and setters
        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getUniversity() { return university; }
        public void setUniversity(String university) { this.university = university; }

        public String getProgram() { return program; }
        public void setProgram(String program) { this.program = program; }

        public Integer getCurrentYear() { return currentYear; }
        public void setCurrentYear(Integer currentYear) { this.currentYear = currentYear; }

        public String getCurrentCountry() { return currentCountry; }
        public void setCurrentCountry(String currentCountry) { this.currentCountry = currentCountry; }

        public List<HelpType> getNeedsHelpWith() { return needsHelpWith; }
        public void setNeedsHelpWith(List<HelpType> needsHelpWith) { this.needsHelpWith = needsHelpWith; }

        public String getUrgencyLevel() { return urgencyLevel; }
        public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class CreateMatchRequest {
        private String mentorId;
        private String menteeId;
        private HelpType helpType;

        // Getters and setters
        public String getMentorId() { return mentorId; }
        public void setMentorId(String mentorId) { this.mentorId = mentorId; }

        public String getMenteeId() { return menteeId; }
        public void setMenteeId(String menteeId) { this.menteeId = menteeId; }

        public HelpType getHelpType() { return helpType; }
        public void setHelpType(HelpType helpType) { this.helpType = helpType; }
    }

    public static class RecordSessionRequest {
        private String matchId;
        private String sessionNotes;

        // Getters and setters
        public String getMatchId() { return matchId; }
        public void setMatchId(String matchId) { this.matchId = matchId; }

        public String getSessionNotes() { return sessionNotes; }
        public void setSessionNotes(String sessionNotes) { this.sessionNotes = sessionNotes; }
    }

    public static class RateMentorshipRequest {
        private String matchId;
        private int rating;
        private boolean menteeRating;

        // Getters and setters
        public String getMatchId() { return matchId; }
        public void setMatchId(String matchId) { this.matchId = matchId; }

        public int getRating() { return rating; }
        public void setRating(int rating) { this.rating = rating; }

        public boolean isMenteeRating() { return menteeRating; }
        public void setMenteeRating(boolean menteeRating) { this.menteeRating = menteeRating; }
    }

    public static class CompleteMentorshipRequest {
        private String matchId;
        private String completionReason;

        // Getters and setters
        public String getMatchId() { return matchId; }
        public void setMatchId(String matchId) { this.matchId = matchId; }

        public String getCompletionReason() { return completionReason; }
        public void setCompletionReason(String completionReason) { this.completionReason = completionReason; }
    }

    public static class PauseMentorshipRequest {
        private String matchId;
        private String reason;

        // Getters and setters
        public String getMatchId() { return matchId; }
        public void setMatchId(String matchId) { this.matchId = matchId; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}