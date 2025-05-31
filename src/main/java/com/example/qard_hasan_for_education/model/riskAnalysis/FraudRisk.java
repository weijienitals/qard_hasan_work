package com.example.qard_hasan_for_education.model.riskAnalysis;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class FraudRisk {
    @JsonProperty("riskLevel")
    private RiskLevel riskLevel;

    @JsonProperty("documentAuthenticity")
    private String documentAuthenticity; // "verified", "suspicious", "likely-fake"

    @JsonProperty("nameConsistency")
    private Boolean nameConsistency;

    @JsonProperty("dateConsistency")
    private Boolean dateConsistency;

    @JsonProperty("institutionValidation")
    private String institutionValidation; // "verified", "questionable", "invalid"

    @JsonProperty("riskFactors")
    private List<String> riskFactors;

    // Constructors
    public FraudRisk() {}

    // Getters and Setters
    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public String getDocumentAuthenticity() { return documentAuthenticity; }
    public void setDocumentAuthenticity(String documentAuthenticity) { this.documentAuthenticity = documentAuthenticity; }

    public Boolean getNameConsistency() { return nameConsistency; }
    public void setNameConsistency(Boolean nameConsistency) { this.nameConsistency = nameConsistency; }

    public Boolean getDateConsistency() { return dateConsistency; }
    public void setDateConsistency(Boolean dateConsistency) { this.dateConsistency = dateConsistency; }

    public String getInstitutionValidation() { return institutionValidation; }
    public void setInstitutionValidation(String institutionValidation) { this.institutionValidation = institutionValidation; }

    public List<String> getRiskFactors() { return riskFactors; }
    public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }
}
