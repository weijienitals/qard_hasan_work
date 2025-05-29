package com.example.qard_hasan_for_education.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PassportInfo {
    @JsonProperty("fullName")
    private String fullName;

    @JsonProperty("identification")
    private String identification;

    @JsonProperty("nationality")
    private String nationality;

    @JsonProperty("dateOfBirth")
    private String dateOfBirth;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("expiryDate")
    private String expiryDate;

    // Constructors
    public PassportInfo() {}

    public PassportInfo(String fullName, String identification, String nationality,
                        String dateOfBirth, String gender, String expiryDate) {
        this.fullName = fullName;
        this.identification = identification;
        this.nationality = nationality;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.expiryDate = expiryDate;
    }

    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPassportNumber() { return identification; }
    public void setPassportNumber(String identification) { this.identification = identification; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    @Override
    public String toString() {
        return "PassportInfo{" +
                "fullName='" + fullName + '\'' +
                ", identification='" + identification + '\'' +
                ", nationality='" + nationality + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", gender='" + gender + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                '}';
    }
}