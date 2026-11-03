package com.bugdiagnosis.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class StructuredBugContext {
    private String submissionId;
    private BugReport bugReport;
    private TriageResult triageResult;
    private LogAnalysisResult logAnalysisResult;
    private FixRecommendation fixRecommendation;
    private List<String> warnings;
    private List<String> orchestrationAuditTrail;
    private Instant orchestrationTimestamp;
    private long totalProcessingTimeMs;
    private boolean successful;

    public StructuredBugContext() {
        this.warnings = new ArrayList<>();
        this.orchestrationAuditTrail = new ArrayList<>();
        this.orchestrationTimestamp = Instant.now();
        this.successful = true;
    }

    public StructuredBugContext(BugReport bugReport) {
        this();
        this.bugReport = bugReport;
        this.submissionId = bugReport.getId();
    }

    public String getSubmissionId() { return submissionId; }
    public void setSubmissionId(String submissionId) { this.submissionId = submissionId; }

    public BugReport getBugReport() { return bugReport; }
    public void setBugReport(BugReport bugReport) { this.bugReport = bugReport; }

    public TriageResult getTriageResult() { return triageResult; }
    public void setTriageResult(TriageResult triageResult) { this.triageResult = triageResult; }

    public LogAnalysisResult getLogAnalysisResult() { return logAnalysisResult; }
    public void setLogAnalysisResult(LogAnalysisResult logAnalysisResult) { this.logAnalysisResult = logAnalysisResult; }

    public FixRecommendation getFixRecommendation() { return fixRecommendation; }
    public void setFixRecommendation(FixRecommendation fixRecommendation) { this.fixRecommendation = fixRecommendation; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }

    public void addWarning(String warning) { this.warnings.add(warning); }

    public List<String> getOrchestrationAuditTrail() { return orchestrationAuditTrail; }
    public void setOrchestrationAuditTrail(List<String> orchestrationAuditTrail) { this.orchestrationAuditTrail = orchestrationAuditTrail; }

    public void addAuditLog(String log) {
        this.orchestrationAuditTrail.add("[" + Instant.now().toString() + "] " + log);
    }

    public Instant getOrchestrationTimestamp() { return orchestrationTimestamp; }
    public void setOrchestrationTimestamp(Instant orchestrationTimestamp) { this.orchestrationTimestamp = orchestrationTimestamp; }

    public long getTotalProcessingTimeMs() { return totalProcessingTimeMs; }
    public void setTotalProcessingTimeMs(long totalProcessingTimeMs) { this.totalProcessingTimeMs = totalProcessingTimeMs; }

    public boolean isSuccessful() { return successful; }
    public void setSuccessful(boolean successful) { this.successful = successful; }
}