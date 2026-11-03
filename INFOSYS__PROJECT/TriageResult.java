package com.bugdiagnosis.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TriageResult {
    private Severity severity;
    private Priority priority;
    private String affectedComponent;
    private double confidenceScore;
    private String reasoning;
    private List<String> impactFactors;
    private Instant triageTimestamp;

    public TriageResult() {
        this.impactFactors = new ArrayList<>();
        this.triageTimestamp = Instant.now();
    }

    public TriageResult(Severity severity, Priority priority, String affectedComponent, double confidenceScore, String reasoning) {
        this();
        this.severity = severity;
        this.priority = priority;
        this.affectedComponent = affectedComponent;
        this.confidenceScore = confidenceScore;
        this.reasoning = reasoning;
    }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public String getAffectedComponent() { return affectedComponent; }
    public void setAffectedComponent(String affectedComponent) { this.affectedComponent = affectedComponent; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }

    public List<String> getImpactFactors() { return impactFactors; }
    public void setImpactFactors(List<String> impactFactors) { this.impactFactors = impactFactors; }

    public Instant getTriageTimestamp() { return triageTimestamp; }
    public void setTriageTimestamp(Instant triageTimestamp) { this.triageTimestamp = triageTimestamp; }
}