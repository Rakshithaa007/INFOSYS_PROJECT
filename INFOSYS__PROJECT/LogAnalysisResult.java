package com.bugdiagnosis.model;

import java.util.ArrayList;
import java.util.List;

public class LogAnalysisResult {
    private String exceptionType;
    private String errorMessage;
    private FailurePoint failurePoint;
    private List<StackFrame> probableAffectedCodePath;
    private String detectedLogFormat;
    private double confidenceScore;
    private String parsingNotes;
    private boolean hasLogs;
    private int totalFramesParsed;

    public LogAnalysisResult() {
        this.probableAffectedCodePath = new ArrayList<>();
        this.failurePoint = new FailurePoint();
        this.hasLogs = false;
        this.confidenceScore = 0.0;
        this.detectedLogFormat = "None";
    }

    public String getExceptionType() { return exceptionType; }
    public void setExceptionType(String exceptionType) { this.exceptionType = exceptionType; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public FailurePoint getFailurePoint() { return failurePoint; }
    public void setFailurePoint(FailurePoint failurePoint) { this.failurePoint = failurePoint; }

    public List<StackFrame> getProbableAffectedCodePath() { return probableAffectedCodePath; }
    public void setProbableAffectedCodePath(List<StackFrame> probableAffectedCodePath) { this.probableAffectedCodePath = probableAffectedCodePath; }

    public String getDetectedLogFormat() { return detectedLogFormat; }
    public void setDetectedLogFormat(String detectedLogFormat) { this.detectedLogFormat = detectedLogFormat; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getParsingNotes() { return parsingNotes; }
    public void setParsingNotes(String parsingNotes) { this.parsingNotes = parsingNotes; }

    public boolean isHasLogs() { return hasLogs; }
    public void setHasLogs(boolean hasLogs) { this.hasLogs = hasLogs; }

    public int getTotalFramesParsed() { return totalFramesParsed; }
    public void setTotalFramesParsed(int totalFramesParsed) { this.totalFramesParsed = totalFramesParsed; }
}