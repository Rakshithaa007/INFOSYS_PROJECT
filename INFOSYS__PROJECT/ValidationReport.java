package com.bugdiagnosis.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ValidationReport {
    private Instant runTimestamp;
    private int totalTestCases;
    private int passedCount;
    private int failedCount;

    private double severityAccuracy;
    private double priorityAccuracy;
    private double componentAccuracy;
    private double overallTriageAccuracy;

    private double exceptionAccuracy;
    private double failurePointAccuracy;
    private double codePathAccuracy;
    private double overallLogAccuracy;
    private double overallSystemAccuracy;

    private List<TestEvaluationDetail> details;
    private List<FailureAnalysisCase> failureCases;

    public ValidationReport() {
        this.runTimestamp = Instant.now();
        this.details = new ArrayList<>();
        this.failureCases = new ArrayList<>();
    }

    public static class TestEvaluationDetail {
        public String testId;
        public String testName;
        public String category;
        public boolean severityMatch;
        public boolean priorityMatch;
        public boolean componentMatch;
        public boolean exceptionMatch;
        public boolean failurePointMatch;
        public String actualSeverity;
        public String expectedSeverity;
        public String actualPriority;
        public String expectedPriority;
        public String actualComponent;
        public String expectedComponent;
        public String actualException;
        public String expectedException;
        public String actualFailurePoint;
        public String expectedFailurePoint;
        public boolean fullyPassed;
    }

    public static class FailureAnalysisCase {
        public String testId;
        public String testName;
        public String failedDimension;
        public String expectedValue;
        public String actualValue;
        public String rootCauseAnalysis;
        public String recommendedPromptImprovement;

        public FailureAnalysisCase(String testId, String testName, String failedDimension, String expectedValue,
                                   String actualValue, String rootCauseAnalysis, String recommendedPromptImprovement) {
            this.testId = testId;
            this.testName = testName;
            this.failedDimension = failedDimension;
            this.expectedValue = expectedValue;
            this.actualValue = actualValue;
            this.rootCauseAnalysis = rootCauseAnalysis;
            this.recommendedPromptImprovement = recommendedPromptImprovement;
        }
    }

    public Instant getRunTimestamp() { return runTimestamp; }
    public int getTotalTestCases() { return totalTestCases; }
    public void setTotalTestCases(int totalTestCases) { this.totalTestCases = totalTestCases; }
    public int getPassedCount() { return passedCount; }
    public void setPassedCount(int passedCount) { this.passedCount = passedCount; }
    public int getFailedCount() { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }

    public double getSeverityAccuracy() { return severityAccuracy; }
    public void setSeverityAccuracy(double v) { this.severityAccuracy = v; }
    public double getPriorityAccuracy() { return priorityAccuracy; }
    public void setPriorityAccuracy(double v) { this.priorityAccuracy = v; }
    public double getComponentAccuracy() { return componentAccuracy; }
    public void setComponentAccuracy(double v) { this.componentAccuracy = v; }
    public double getOverallTriageAccuracy() { return overallTriageAccuracy; }
    public void setOverallTriageAccuracy(double v) { this.overallTriageAccuracy = v; }

    public double getExceptionAccuracy() { return exceptionAccuracy; }
    public void setExceptionAccuracy(double v) { this.exceptionAccuracy = v; }
    public double getFailurePointAccuracy() { return failurePointAccuracy; }
    public void setFailurePointAccuracy(double v) { this.failurePointAccuracy = v; }
    public double getCodePathAccuracy() { return codePathAccuracy; }
    public void setCodePathAccuracy(double v) { this.codePathAccuracy = v; }
    public double getOverallLogAccuracy() { return overallLogAccuracy; }
    public void setOverallLogAccuracy(double v) { this.overallLogAccuracy = v; }
    public double getOverallSystemAccuracy() { return overallSystemAccuracy; }
    public void setOverallSystemAccuracy(double v) { this.overallSystemAccuracy = v; }

    public List<TestEvaluationDetail> getDetails() { return details; }
    public List<FailureAnalysisCase> getFailureCases() { return failureCases; }
}