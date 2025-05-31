// MenteeProfile.java
package com.example.qard_hasan_for_education.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public class MenteeProfile {
    @JsonProperty("menteeId")
    private String menteeId;

    @JsonProperty("studentId")
    private String studentId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("university")
    private String university;

    @JsonProperty("program")
    private String program;

    @JsonProperty("currentYear")
    private Integer currentYear;

    @JsonProperty("currentCountry")
    private String currentCountry;

    @JsonProperty("needsHelpWith")
    private List<HelpType> needsHelpWith;

    @JsonProperty("urgencyLevel")
    private String urgencyLevel; // "low", "medium", "high"

    @JsonProperty("preferredMentorCharacteristics")
    private List<String> preferredMentorCharacteristics;

    @JsonProperty("isActive")
    private boolean isActive;

    @JsonProperty("joinedAt")
    private LocalDateTime joinedAt;

    @JsonProperty("lastActiveAt")
    private LocalDateTime lastActiveAt;

    @JsonProperty("description")
    private String description;

    @JsonProperty("contactPreference")
    private String contactPreference;

    @JsonProperty("availableTimeSlots")
    private List<String> availableTimeSlots;

    // Constructors
    public MenteeProfile() {}

    public MenteeProfile(String studentId, String name, String university, String program,
                         Integer currentYear, String currentCountry) {
        this.menteeId = generateMenteeId();
        this.studentId = studentId;
        this.name = name;
        this.university = university;
        this.program = program;
        this.currentYear = currentYear;
        this.currentCountry = currentCountry;
        this.isActive = true;
        this.joinedAt = LocalDateTime.now();
        this.lastActiveAt = LocalDateTime.now();
        this.urgencyLevel = "medium";
    }

    // Getters and Setters (similar pattern as MentorProfile)
    public String getMenteeId() { return menteeId; }
    public void setMenteeId(String menteeId) { this.menteeId = menteeId; }

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

    public List<String> getPreferredMentorCharacteristics() { return preferredMentorCharacteristics; }
    public void setPreferredMentorCharacteristics(List<String> preferredMentorCharacteristics) {
        this.preferredMentorCharacteristics = preferredMentorCharacteristics;
    }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public LocalDateTime getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(LocalDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContactPreference() { return contactPreference; }
    public void setContactPreference(String contactPreference) { this.contactPreference = contactPreference; }

    public List<String> getAvailableTimeSlots() { return availableTimeSlots; }
    public void setAvailableTimeSlots(List<String> availableTimeSlots) { this.availableTimeSlots = availableTimeSlots; }

    private String generateMenteeId() {
        return "MENTEE_" + System.currentTimeMillis() + "_" +
                java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}