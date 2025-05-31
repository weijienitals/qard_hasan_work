package com.example.qard_hasan_for_education.controller;

import com.example.qard_hasan_for_education.model.*;
import com.example.qard_hasan_for_education.service.VolunteeringService;
import com.example.qard_hasan_for_education.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/volunteering")
@CrossOrigin(origins = "*")
public class VolunteeringController {

    private static final Logger logger = LoggerFactory.getLogger(VolunteeringController.class);

    @Autowired
    private VolunteeringService volunteeringService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Accept mentoring offer after payment
     */
    @PostMapping("/accept-offer")
    public ResponseEntity<?> acceptMentoringOffer(@RequestBody AcceptOfferRequest request) {
        try {
            MentorProfile mentorProfile = volunteeringService.acceptMentoringOffer(
                    request.getOfferId(),
                    request.getAvailableHelpTypes(),
                    request.getBio(),
                    request.getContactPreference()
            );

            // Send welcome notification
            notificationService.sendMentorWelcomeNotification(
                    mentorProfile.getMentorId(),
                    mentorProfile.getName()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Thank you for joining our mentorship program!");
            response.put("mentorProfile", mentorProfile);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error accepting mentoring offer", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Decline mentoring offer
     */
    @PostMapping("/decline-offer")
    public ResponseEntity<?> declineMentoringOffer(@RequestBody DeclineOfferRequest request) {
        try {
            volunteeringService.declineMentoringOffer(request.getOfferId(), request.getReason());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Thank you for your response. You can always volunteer later!");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error declining mentoring offer", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Get potential mentees for a mentor
     */
    @GetMapping("/mentees/{mentorId}")
    public ResponseEntity<?> getPotentialMentees(
            @PathVariable String mentorId,
            @RequestParam(required = false) List<HelpType> helpTypes) {
        try {
            List<MenteeProfile> mentees = volunteeringService.getPotentialMentees(mentorId, helpTypes);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mentees", mentees);
            response.put("totalMentees", mentees.size());
            response.put("message", mentees.isEmpty() ?
                    "No mentees found matching your criteria at the moment. Check back later!" :
                    "Here are students who could benefit from your guidance");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving potential mentees", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error retrieving potential mentees"));
        }
    }

    /**
     * Get mentor statistics and impact
     */
    @GetMapping("/mentor-stats/{mentorId}")
    public ResponseEntity<?> getMentorStats(@PathVariable String mentorId) {
        try {
            Map<String, Object> stats = volunteeringService.getMentorStats(mentorId);

            if (stats.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(Map.of("success", false, "message", "Mentor not found"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            response.put("message", "Your mentoring impact summary");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving mentor stats", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error retrieving mentor statistics"));
        }
    }

    /**
     * Update mentor preferences and availability
     */
    @PutMapping("/mentor-preferences/{mentorId}")
    public ResponseEntity<?> updateMentorPreferences(
            @PathVariable String mentorId,
            @RequestBody UpdateMentorRequest request) {
        try {
            MentorProfile updatedProfile = volunteeringService.updateMentorPreferences(
                    mentorId,
                    request.getHelpTypes(),
                    request.getMaxMentees(),
                    request.getBio(),
                    request.getTimeSlots(),
                    request.isActive()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mentor preferences updated successfully");
            response.put("mentorProfile", updatedProfile);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error updating mentor preferences", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Get mentor profile information
     */
    @GetMapping("/mentor-profile/{mentorId}")
    public ResponseEntity<?> getMentorProfile(@PathVariable String mentorId) {
        try {
            // This would typically call a service method to get mentor profile
            // For now, we'll return stats as that contains profile info
            Map<String, Object> stats = volunteeringService.getMentorStats(mentorId);

            if (stats.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(Map.of("success", false, "message", "Mentor profile not found"));
            }

            return ResponseEntity.ok(Map.of("success", true, "mentorProfile", stats));

        } catch (Exception e) {
            logger.error("Error retrieving mentor profile", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error retrieving mentor profile"));
        }
    }

    /**
     * Get available help types
     */
    @GetMapping("/help-types")
    public ResponseEntity<?> getAvailableHelpTypes() {
        try {
            Map<String, Object> helpTypes = new HashMap<>();
            for (HelpType type : HelpType.values()) {
                helpTypes.put(type.name(), type.getDescription());
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "helpTypes", helpTypes,
                    "message", "Available types of help you can offer"
            ));

        } catch (Exception e) {
            logger.error("Error retrieving help types", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error retrieving help types"));
        }
    }

    /**
     * Get mentoring program information
     */
    @GetMapping("/program-info")
    public ResponseEntity<?> getMentoringProgramInfo() {
        try {
            Map<String, Object> programInfo = new HashMap<>();
            programInfo.put("title", "Indonesian Student Mentorship Program");
            programInfo.put("description", "Help fellow Indonesian students succeed in their studies abroad by sharing your experience and providing guidance.");
            programInfo.put("benefits", List.of(
                    "Make a meaningful impact on someone's educational journey",
                    "Build your leadership and communication skills",
                    "Expand your professional network",
                    "Give back to the Indonesian student community",
                    "Flexible commitment - mentor at your own pace"
            ));
            programInfo.put("commitment", "Flexible - typically 1-2 hours per month per mentee");
            programInfo.put("helpTypes", List.of(
                    "Academic guidance and study tips",
                    "Mental health and emotional support",
                    "Cultural adaptation advice",
                    "Career guidance and networking",
                    "Financial management tips",
                    "General friendship and support"
            ));

            return ResponseEntity.ok(Map.of("success", true, "programInfo", programInfo));

        } catch (Exception e) {
            logger.error("Error retrieving program info", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error retrieving program information"));
        }
    }

    // Request DTOs
    public static class AcceptOfferRequest {
        private String offerId;
        private List<HelpType> availableHelpTypes;
        private String bio;
        private String contactPreference;

        // Getters and setters
        public String getOfferId() { return offerId; }
        public void setOfferId(String offerId) { this.offerId = offerId; }

        public List<HelpType> getAvailableHelpTypes() { return availableHelpTypes; }
        public void setAvailableHelpTypes(List<HelpType> availableHelpTypes) { this.availableHelpTypes = availableHelpTypes; }

        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }

        public String getContactPreference() { return contactPreference; }
        public void setContactPreference(String contactPreference) { this.contactPreference = contactPreference; }
    }

    public static class DeclineOfferRequest {
        private String offerId;
        private String reason;

        // Getters and setters
        public String getOfferId() { return offerId; }
        public void setOfferId(String offerId) { this.offerId = offerId; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class UpdateMentorRequest {
        private List<HelpType> helpTypes;
        private Integer maxMentees;
        private String bio;
        private List<String> timeSlots;
        private boolean active;

        // Getters and setters
        public List<HelpType> getHelpTypes() { return helpTypes; }
        public void setHelpTypes(List<HelpType> helpTypes) { this.helpTypes = helpTypes; }

        public Integer getMaxMentees() { return maxMentees; }
        public void setMaxMentees(Integer maxMentees) { this.maxMentees = maxMentees; }

        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }

        public List<String> getTimeSlots() { return timeSlots; }
        public void setTimeSlots(List<String> timeSlots) { this.timeSlots = timeSlots; }

        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }
}