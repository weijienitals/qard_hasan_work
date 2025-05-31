// MentorshipMatch.java
package com.example.qard_hasan_for_education.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class MentorshipMatch {
    @JsonProperty("matchId")
    private String matchId;

    @JsonProperty("mentorId")
    private String mentorId;

    @JsonProperty("menteeId")
    private String menteeId;

    @JsonProperty("helpType")
    private HelpType helpType;

    @JsonProperty("status")
    private MentorshipStatus status;

    @JsonProperty("matchedAt")
    private LocalDateTime matchedAt;

    @JsonProperty("lastInteractionAt")
    private LocalDateTime lastInteractionAt;

    @JsonProperty("totalSessions")
    private Integer totalSessions;

    @JsonProperty("menteeRating")
    private Integer menteeRating; // 1-5 stars

    @JsonProperty("mentorRating")
    private Integer mentorRating; // 1-5 stars

    @JsonProperty("notes")
    private String notes;

    // Constructors
    public MentorshipMatch() {}

    public MentorshipMatch(String mentorId, String menteeId, HelpType helpType) {
        this.matchId = generateMatchId();
        this.mentorId = mentorId;
        this.menteeId = menteeId;
        this.helpType = helpType;
        this.status = MentorshipStatus.ACTIVE;
        this.matchedAt = LocalDateTime.now();
        this.lastInteractionAt = LocalDateTime.now();
        this.totalSessions = 0;
    }

    // Getters and Setters
    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }

    public String getMentorId() { return mentorId; }
    public void setMentorId(String mentorId) { this.mentorId = mentorId; }

    public String getMenteeId() { return menteeId; }
    public void setMenteeId(String menteeId) { this.menteeId = menteeId; }

    public HelpType getHelpType() { return helpType; }
    public void setHelpType(HelpType helpType) { this.helpType = helpType; }

    public MentorshipStatus getStatus() { return status; }
    public void setStatus(MentorshipStatus status) { this.status = status; }

    public LocalDateTime getMatchedAt() { return matchedAt; }
    public void setMatchedAt(LocalDateTime matchedAt) { this.matchedAt = matchedAt; }

    public LocalDateTime getLastInteractionAt() { return lastInteractionAt; }
    public void setLastInteractionAt(LocalDateTime lastInteractionAt) { this.lastInteractionAt = lastInteractionAt; }

    public Integer getTotalSessions() { return totalSessions; }
    public void setTotalSessions(Integer totalSessions) { this.totalSessions = totalSessions; }

    public Integer getMenteeRating() { return menteeRating; }
    public void setMenteeRating(Integer menteeRating) { this.menteeRating = menteeRating; }

    public Integer getMentorRating() { return mentorRating; }
    public void setMentorRating(Integer mentorRating) { this.mentorRating = mentorRating; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    private String generateMatchId() {
        return "MATCH_" + System.currentTimeMillis() + "_" +
                java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}

