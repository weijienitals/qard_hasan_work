package com.example.qard_hasan_for_education.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateStatusRequest extends ApplicationRequest {
    @JsonProperty("status")
    private String status;

    public UpdateStatusRequest() {}
    public UpdateStatusRequest(String applicationId, String status) {
        super(applicationId);
        this.status = status;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}