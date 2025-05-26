package com.example.qard_hasan_for_education.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddNoteRequest extends ApplicationRequest {
    @JsonProperty("note")
    private String note;

    public AddNoteRequest() {}
    public AddNoteRequest(String applicationId, String note) {
        super(applicationId);
        this.note = note;
    }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}