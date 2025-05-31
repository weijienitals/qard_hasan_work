package com.example.qard_hasan_for_education.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public class MentorProfile {
    @JsonProperty("mentorId")
    private String mentorId;

    @JsonProperty("studentId")
    private String studentId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("university")
    private String university;

    @JsonProperty("program")
    private String program;

    @JsonProperty("graduationYear")
    private Integer graduationYear;

    @JsonProperty("currentCountry")
    private String currentCountry;

    @JsonProperty("availableHelpTypes")
    private List<HelpType> availableHelpTypes;

    @JsonProperty("maxMentees")
    private Integer maxMentees;

    @JsonProperty("currentMentees")
    private Integer currentMentees;

    @JsonProperty("totalMentoringSessions")
    private Integer totalMentoringSessions;

    @JsonProperty("averageRating")
    private Double averageRating;

    @JsonProperty("isActive")
    private boolean isActive;

    @JsonProperty("joinedAt")
    private LocalDateTime joinedAt;

    @JsonProperty("lastActiveAt")
    private LocalDateTime lastActiveAt;

    @JsonProperty("bio")
    private String bio;

    @JsonProperty("contactPreference")
    private String contactPreference; // "whatsapp", "email", "platform"

    @JsonProperty("availableTimeSlots")
    private List<String> availableTimeSlots;

    // Constructors
    public MentorProfile() {}

    public MentorProfile(String studentId, String name, String university, String program,
                         Integer graduationYear, String currentCountry) {
        this.mentorId = generateMentorId();
        this.studentId = studentId;
        this.name = name;
        this.university = university;
        this.program = program;
        this.graduationYear = graduationYear;
        this.currentCountry = currentCountry;
        this.maxMentees = 3; // Default max mentees
        this.currentMentees = 0;
        this.totalMentoringSessions = 0;
        this.averageRating = 0.0;
        this.isActive = true;
        this.joinedAt = LocalDateTime.now();
        this.lastActiveAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getMentorId() { return mentorId; }
    public void setMentorId(String mentorId) { this.mentorId = mentorId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }

    public String getCurrentCountry() { return currentCountry; }
    public void setCurrentCountry(String currentCountry) { this.currentCountry = currentCountry; }

    public List<HelpType> getAvailableHelpTypes() { return availableHelpTypes; }
    public void setAvailableHelpTypes(List<HelpType> availableHelpTypes) { this.availableHelpTypes = availableHelpTypes; }

    public Integer getMaxMentees() { return maxMentees; }
    public void setMaxMentees(Integer maxMentees) { this.maxMentees = maxMentees; }

    public Integer getCurrentMentees() { return currentMentees; }
    public void setCurrentMentees(Integer currentMentees) { this.currentMentees = currentMentees; }

    public Integer getTotalMentoringSessions() { return totalMentoringSessions; }
    public void setTotalMentoringSessions(Integer totalMentoringSessions) { this.totalMentoringSessions = totalMentoringSessions; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public LocalDateTime getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(LocalDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getContactPreference() { return contactPreference; }
    public void setContactPreference(String contactPreference) { this.contactPreference = contactPreference; }

    public List<String> getAvailableTimeSlots() { return availableTimeSlots; }
    public void setAvailableTimeSlots(List<String> availableTimeSlots) { this.availableTimeSlots = availableTimeSlots; }

    // Utility methods
    public boolean canAcceptMoreMentees() {
        return currentMentees < maxMentees && isActive;
    }

    private String generateMentorId() {
        return "MENTOR_" + System.currentTimeMillis() + "_" +
                java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}