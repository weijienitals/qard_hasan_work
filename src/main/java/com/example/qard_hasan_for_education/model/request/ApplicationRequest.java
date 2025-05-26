package com.example.qard_hasan_for_education.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApplicationRequest {
    @JsonProperty("applicationId")
    private String applicationId;

    public ApplicationRequest() {}
    public ApplicationRequest(String applicationId) { this.applicationId = applicationId; }

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
}