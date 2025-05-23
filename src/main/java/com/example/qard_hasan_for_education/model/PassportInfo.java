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

    // Constructors, getters, setters, toString - same pattern as your other models
}