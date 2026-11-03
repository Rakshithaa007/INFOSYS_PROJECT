package com.bugdiagnosis.model;

import java.time.Instant;
import java.util.UUID;

public class BugReport {
    private String id;
    private String title;
    private String description;
    private String rawLog;
    private String stackTrace;
    private String environment;
    private String affectedModuleHint;
    private String language;
    private String submitter;
    private Instant timestamp;

    public BugReport() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.timestamp = Instant.now();
    }

    public BugReport(String title, String description, String rawLog) {
        this();
        this.title = title;
        this.description = description;
        this.rawLog = rawLog;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRawLog() { return rawLog; }
    public void setRawLog(String rawLog) { this.rawLog = rawLog; }

    public String getStackTrace() { return stackTrace; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public String getAffectedModuleHint() { return affectedModuleHint; }
    public void setAffectedModuleHint(String affectedModuleHint) { this.affectedModuleHint = affectedModuleHint; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getSubmitter() { return submitter; }
    public void setSubmitter(String submitter) { this.submitter = submitter; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}