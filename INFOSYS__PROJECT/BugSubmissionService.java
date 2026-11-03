package com.bugdiagnosis.ingestion;

import com.bugdiagnosis.model.BugReport;

import java.util.regex.Pattern;

public class BugSubmissionService {

    private static final Pattern STACK_TRACE_INDICATOR = Pattern.compile(
            "(?:at\\s+[a-zA-Z0-9_$.]+\\.[a-zA-Z0-9_$]+\\([a-zA-Z0-9_$.-]+\\.java:\\d+\\)|Traceback \\(most recent call last\\)|TypeError:|NullPointerException|Exception in thread)",
            Pattern.CASE_INSENSITIVE
    );

    public BugReport processSubmission(String title, String description, String rawLog, String moduleHint, String environment) {
        BugReport report = new BugReport();

        String cleanTitle = title != null ? title.trim() : "";
        String cleanDesc = description != null ? description.trim() : "";
        String cleanLog = rawLog != null ? rawLog.trim() : "";

        if (cleanLog.isEmpty() && !cleanDesc.isEmpty()) {
            ExtractedParts parts = splitDescriptionAndTrace(cleanDesc);
            cleanDesc = parts.description;
            cleanLog = parts.stackTrace;
        }

        if (cleanTitle.isEmpty()) {
            cleanTitle = deriveTitle(cleanDesc, cleanLog);
        }

        report.setTitle(cleanTitle);
        report.setDescription(cleanDesc);
        report.setRawLog(cleanLog);
        report.setStackTrace(cleanLog);
        report.setAffectedModuleHint(moduleHint != null ? moduleHint.trim() : "");
        report.setEnvironment(environment != null && !environment.trim().isEmpty() ? environment.trim() : "Production / Standard");

        return report;
    }

    private ExtractedParts splitDescriptionAndTrace(String fullText) {
        String[] lines = fullText.split("\\r?\\n");
        StringBuilder descBuilder = new StringBuilder();
        StringBuilder traceBuilder = new StringBuilder();
        boolean inTrace = false;

        for (String line : lines) {
            if (!inTrace && STACK_TRACE_INDICATOR.matcher(line).find()) {
                inTrace = true;
            }
            if (inTrace) {
                traceBuilder.append(line).append("\n");
            } else {
                descBuilder.append(line).append("\n");
            }
        }

        return new ExtractedParts(descBuilder.toString().trim(), traceBuilder.toString().trim());
    }

    private String deriveTitle(String desc, String log) {
        if (!desc.isEmpty()) {
            String firstLine = desc.split("\\r?\\n")[0].trim();
            if (firstLine.length() > 80) return firstLine.substring(0, 77) + "...";
            return firstLine;
        }
        if (!log.isEmpty()) {
            String firstLine = log.split("\\r?\\n")[0].trim();
            if (firstLine.length() > 80) return firstLine.substring(0, 77) + "...";
            return firstLine;
        }
        return "Untitled Defect Report";
    }

    private static class ExtractedParts {
        final String description;
        final String stackTrace;

        ExtractedParts(String description, String stackTrace) {
            this.description = description;
            this.stackTrace = stackTrace;
        }
    }
}