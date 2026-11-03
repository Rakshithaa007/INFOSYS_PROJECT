package com.bugdiagnosis.agent;

import com.bugdiagnosis.model.BugReport;
import com.bugdiagnosis.model.Priority;
import com.bugdiagnosis.model.Severity;
import com.bugdiagnosis.model.TriageResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TriageAgent {

    public TriageResult triage(BugReport report) {
        TriageResult result = new TriageResult();
        List<String> reasons = new ArrayList<>();
        List<String> factors = new ArrayList<>();

        String fullText = ((report.getTitle() != null ? report.getTitle() : "") + " " +
                (report.getDescription() != null ? report.getDescription() : "") + " " +
                (report.getRawLog() != null ? report.getRawLog() : "")).toLowerCase(Locale.ROOT);

        Severity severity = determineSeverity(fullText, report, reasons, factors);
        result.setSeverity(severity);

        Priority priority = determinePriority(severity, fullText, reasons, factors);
        result.setPriority(priority);

        String component = determineComponent(fullText, report, reasons);
        result.setAffectedComponent(component);

        double confidence = calculateTriageConfidence(report, severity, component, fullText);
        result.setConfidenceScore(confidence);

        result.setImpactFactors(factors);
        result.setReasoning(buildReasoningExplanation(severity, priority, component, reasons, factors));

        return result;
    }

    private Severity determineSeverity(String text, BugReport report, List<String> reasons, List<String> factors) {
        if (text.contains("fatal") || text.contains("sigsegv") || text.contains("segmentation fault") ||
                text.contains("deadlock") || text.contains("outofmemory") || text.contains("heap space") ||
                text.contains("system crash") || text.contains("complete outage")) {
            factors.add("Critical operational impact: Crash / Memory Exhaustion / Deadlock detected.");
            reasons.add("Assigned CRITICAL severity due to detected critical failure signatures.");
            return Severity.CRITICAL;
        }

        if (text.contains("nullpointerexception") || text.contains("concurrentmodificationexception") ||
                text.contains("broken") || text.contains("failed to start") || text.contains("timeout") ||
                text.contains("502 bad gateway") || text.contains("sqlexception")) {
            factors.add("High functional impact: Core feature blockage or unhandled exception.");
            reasons.add("Assigned HIGH severity because a primary system operation is blocked.");
            return Severity.HIGH;
        }

        if (text.contains("typo") || text.contains("cosmetic") || text.contains("misspelled") ||
                text.contains("padding") || text.contains("color mismatch") || text.contains("documentation")) {
            factors.add("Minimal operational impact: Cosmetic defect or typo.");
            reasons.add("Assigned LOW severity due to superficial cosmetic flaw.");
            return Severity.LOW;
        }

        factors.add("Moderate impact: Secondary feature malfunction with viable workarounds.");
        reasons.add("Assigned MEDIUM severity as the issue impacts regular workflow without causing full outage.");
        return Severity.MEDIUM;
    }

    private Priority determinePriority(Severity severity, String text, List<String> reasons, List<String> factors) {
        if (severity == Severity.CRITICAL) {
            reasons.add("Priority escalated to P1 - Blocker in direct alignment with Critical severity.");
            return Priority.P1_CRITICAL;
        }
        if (text.contains("urgent") || text.contains("blocker") || text.contains("production") || text.contains("p1")) {
            reasons.add("Priority set to P1 - Blocker due to explicit urgency and production impact.");
            return Priority.P1_CRITICAL;
        }
        if (severity == Severity.HIGH) {
            reasons.add("Priority set to P2 - Urgent to remediate high-severity functional blockage.");
            return Priority.P2_HIGH;
        }
        if (severity == Severity.LOW) {
            reasons.add("Priority marked as P4 - Low for scheduling in future maintenance sprints.");
            return Priority.P4_LOW;
        }
        reasons.add("Priority designated as P3 - Normal for standard release backlog handling.");
        return Priority.P3_MEDIUM;
    }

    private String determineComponent(String text, BugReport report, List<String> reasons) {
        if (report.getAffectedModuleHint() != null && !report.getAffectedModuleHint().trim().isEmpty()) {
            reasons.add("Component aligned with submitter hint: " + report.getAffectedModuleHint());
            return report.getAffectedModuleHint().trim();
        }

        if (text.contains("tomcat") || text.contains("nioendpoint") || text.contains("poller") || text.contains("socket")) {
            reasons.add("Component identified as 'Networking & Connection Handling'.");
            return "Networking / NIO Connector";
        }
        if (text.contains("hadoop") || text.contains("hdfs") || text.contains("namenode") || text.contains("blockmanager")) {
            reasons.add("Component identified as 'Distributed Storage & File System'.");
            return "Distributed Storage / HDFS";
        }
        if (text.contains("lucene") || text.contains("indexwriter") || text.contains("search")) {
            reasons.add("Component identified as 'Search & Indexing Engine'.");
            return "Search & Indexing Engine";
        }
        if (text.contains("dom") || text.contains("presshell") || text.contains("layout") || text.contains("webgl")) {
            reasons.add("Component identified as 'DOM & Rendering Pipeline'.");
            return "DOM & Rendering Pipeline";
        }
        if (text.contains("compiler") || text.contains("astparser") || text.contains("jit") || text.contains("v8")) {
            reasons.add("Component identified as 'Compiler & Runtime Engine'.");
            return "Compiler & Runtime Engine";
        }
        if (text.contains("auth") || text.contains("jwt") || text.contains("security") || text.contains("token")) {
            reasons.add("Component identified as 'Authentication & Security'.");
            return "Authentication & Security";
        }

        reasons.add("Component mapped to 'Core Application Logic'.");
        return "Core Application Logic";
    }

    private double calculateTriageConfidence(BugReport report, Severity severity, String component, String text) {
        double confidence = 0.65;
        if (report.getTitle() != null && report.getTitle().length() > 15) confidence += 0.10;
        if (report.getDescription() != null && report.getDescription().length() > 40) confidence += 0.10;
        if (report.getRawLog() != null && !report.getRawLog().trim().isEmpty()) confidence += 0.10;
        if (!component.equals("Core Application Logic")) confidence += 0.05;
        return Math.min(0.98, Math.max(0.50, Math.round(confidence * 100.0) / 100.0));
    }

    private String buildReasoningExplanation(Severity severity, Priority priority, String component, List<String> reasons, List<String> factors) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Classification: [%s | %s] assigned to [%s].\n", severity.getDisplayName(), priority.getDisplayName(), component));
        sb.append("Key Rationales:\n");
        for (String r : reasons) sb.append("  • ").append(r).append("\n");
        if (!factors.isEmpty()) {
            sb.append("Impact Assessment:\n");
            for (String f : factors) sb.append("  • ").append(f).append("\n");
        }
        return sb.toString().trim();
    }
}