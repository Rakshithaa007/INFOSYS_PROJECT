package com.bugdiagnosis.model;

import java.util.ArrayList;
import java.util.List;

public class HistoricalDefect {
    private String defectId;
    private String repository; // "Mozilla", "Apache", "Eclipse"
    private String title;
    private String description;
    private String stackTrace;
    private String component;
    private Severity severity;
    private Priority priority;
    private String rootCause;
    private String resolvedFix;
    private String patchDiff;
    private List<String> tags;
    private double[] embedding;
    private double similarityScore;

    public HistoricalDefect() {
        this.tags = new ArrayList<>();
    }

    public HistoricalDefect(String defectId, String repository, String title, String description,
                            String stackTrace, String component, Severity severity, Priority priority,
                            String rootCause, String resolvedFix, String patchDiff, List<String> tags) {
        this.defectId = defectId;
        this.repository = repository;
        this.title = title;
        this.description = description;
        this.stackTrace = stackTrace;
        this.component = component;
        this.severity = severity;
        this.priority = priority;
        this.rootCause = rootCause;
        this.resolvedFix = resolvedFix;
        this.patchDiff = patchDiff;
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    public String getDefectId() { return defectId; }
    public void setDefectId(String defectId) { this.defectId = defectId; }

    public String getRepository() { return repository; }
    public void setRepository(String repository) { this.repository = repository; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStackTrace() { return stackTrace; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }

    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public String getRootCause() { return rootCause; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }

    public String getResolvedFix() { return resolvedFix; }
    public void setResolvedFix(String resolvedFix) { this.resolvedFix = resolvedFix; }

    public String getPatchDiff() { return patchDiff; }
    public void setPatchDiff(String patchDiff) { this.patchDiff = patchDiff; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public double[] getEmbedding() { return embedding; }
    public void setEmbedding(double[] embedding) { this.embedding = embedding; }

    public double getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(double similarityScore) { this.similarityScore = similarityScore; }

    public String getFullCorpusText() {
        return String.join(" ",
                title != null ? title : "",
                description != null ? description : "",
                component != null ? component : "",
                stackTrace != null ? stackTrace : "",
                rootCause != null ? rootCause : "",
                String.join(" ", tags)
        );
    }
}
