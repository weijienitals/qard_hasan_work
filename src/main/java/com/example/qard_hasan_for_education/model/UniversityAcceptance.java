package com.example.qard_hasan_for_education.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class UniversityAcceptance {

    @JsonProperty("universityName")
    private String universityName;

    @JsonProperty("studentName")
    private String studentName;

    @JsonProperty("program")
    private String program;

    @JsonProperty("acceptanceDate")
    private String acceptanceDate;

    @JsonProperty("semesterStart")
    private String semesterStart;

    // New risk assessment fields
    @JsonProperty("universityTier")
    private String universityTier;

    @JsonProperty("programMarketability")
    private String programMarketability;

    @JsonProperty("completionProbability")
    private String completionProbability;

    @JsonProperty("universityRanking")
    private String universityRanking;

    @JsonProperty("riskFactors")
    private List<String> riskFactors;

    // Constructors
    public UniversityAcceptance() {}

    public UniversityAcceptance(String universityName, String studentName, String program, String acceptanceDate, String semesterStart) {
        this.universityName = universityName;
        this.studentName = studentName;
        this.program = program;
        this.acceptanceDate = acceptanceDate;
        this.semesterStart = semesterStart;
    }

    // Getters and Setters
    public String getUniversityName() { return universityName; }
    public void setUniversityName(String universityName) { this.universityName = universityName; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public String getAcceptanceDate() { return acceptanceDate; }
    public void setAcceptanceDate(String acceptanceDate) { this.acceptanceDate = acceptanceDate; }

    public String getSemesterStart() { return semesterStart; }
    public void setSemesterStart(String semesterStart) { this.semesterStart = semesterStart; }

    public String getUniversityTier() { return universityTier; }
    public void setUniversityTier(String universityTier) { this.universityTier = universityTier; }

    public String getProgramMarketability() { return programMarketability; }
    public void setProgramMarketability(String programMarketability) { this.programMarketability = programMarketability; }

    public String getCompletionProbability() { return completionProbability; }
    public void setCompletionProbability(String completionProbability) { this.completionProbability = completionProbability; }

    public String getUniversityRanking() { return universityRanking; }
    public void setUniversityRanking(String universityRanking) { this.universityRanking = universityRanking; }

    public List<String> getRiskFactors() { return riskFactors; }
    public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }

    @Override
    public String toString() {
        return "UniversityAcceptance{" +
                "universityName='" + universityName + '\'' +
                ", studentName='" + studentName + '\'' +
                ", program='" + program + '\'' +
                ", acceptanceDate='" + acceptanceDate + '\'' +
                ", semesterStart='" + semesterStart + '\'' +
                ", universityTier='" + universityTier + '\'' +
                ", programMarketability='" + programMarketability + '\'' +
                '}';
    }
}