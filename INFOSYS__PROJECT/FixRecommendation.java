package com.bugdiagnosis.model;

import java.util.ArrayList;
import java.util.List;

public class FixRecommendation {
    private String rootCauseAnalysis;
    private String summaryRecommendation;
    private List<String> fixSteps;
    private String codePatchDiff;
    private List<HistoricalDefect> similarHistoricalDefects;
    private List<String> preventionChecklist;
    private double confidenceScore;
    private String targetFile;

    public FixRecommendation() {
        this.fixSteps = new ArrayList<>();
        this.similarHistoricalDefects = new ArrayList<>();
        this.preventionChecklist = new ArrayList<>();
        this.confidenceScore = 0.0;
    }

    public String getRootCauseAnalysis() { return rootCauseAnalysis; }
    public void setRootCauseAnalysis(String rootCauseAnalysis) { this.rootCauseAnalysis = rootCauseAnalysis; }

    public String getSummaryRecommendation() { return summaryRecommendation; }
    public void setSummaryRecommendation(String summaryRecommendation) { this.summaryRecommendation = summaryRecommendation; }

    public List<String> getFixSteps() { return fixSteps; }
    public void setFixSteps(List<String> fixSteps) { this.fixSteps = fixSteps; }

    public String getCodePatchDiff() { return codePatchDiff; }
    public void setCodePatchDiff(String codePatchDiff) { this.codePatchDiff = codePatchDiff; }

    public List<HistoricalDefect> getSimilarHistoricalDefects() { return similarHistoricalDefects; }
    public void setSimilarHistoricalDefects(List<HistoricalDefect> similarHistoricalDefects) { this.similarHistoricalDefects = similarHistoricalDefects; }

    public List<String> getPreventionChecklist() { return preventionChecklist; }
    public void setPreventionChecklist(List<String> preventionChecklist) { this.preventionChecklist = preventionChecklist; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getTargetFile() { return targetFile; }
    public void setTargetFile(String targetFile) { this.targetFile = targetFile; }
}