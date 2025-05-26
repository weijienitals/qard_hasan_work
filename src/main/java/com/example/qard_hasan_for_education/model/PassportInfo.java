package com.example.qard_hasan_for_education.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PassportInfo {
    @JsonProperty("fullName")
    private String fullName;

    @JsonProperty("passportNumber")
    private String passportNumber;

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

    public PassportInfo(String fullName, String passportNumber, String nationality,
                        String dateOfBirth, String gender, String expiryDate) {
        this.fullName = fullName;
        this.passportNumber = passportNumber;
        this.nationality = nationality;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.expiryDate = expiryDate;
    }

    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPassportNumber() { return passportNumber; }
    public void setPassportNumber(String passportNumber) { this.passportNumber = passportNumber; }

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
                ", passportNumber='" + passportNumber + '\'' +
                ", nationality='" + nationality + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", gender='" + gender + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                '}';
    }
}