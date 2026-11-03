package com.bugdiagnosis.server;

import com.bugdiagnosis.model.*;
import com.bugdiagnosis.validation.ValidationReport;

import java.util.List;

public class JsonHelper {

    public static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String toJson(StructuredBugContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"submissionId\":\"").append(escapeJson(ctx.getSubmissionId())).append("\",");
        sb.append("\"successful\":").append(ctx.isSuccessful()).append(",");
        sb.append("\"totalProcessingTimeMs\":").append(ctx.getTotalProcessingTimeMs()).append(",");
        sb.append("\"bugReport\":").append(toJson(ctx.getBugReport())).append(",");
        sb.append("\"triageResult\":").append(toJson(ctx.getTriageResult())).append(",");
        sb.append("\"logAnalysisResult\":").append(toJson(ctx.getLogAnalysisResult())).append(",");
        sb.append("\"fixRecommendation\":").append(toJson(ctx.getFixRecommendation())).append(",");
        sb.append("\"warnings\":[");
        for (int i = 0; i < ctx.getWarnings().size(); i++) {
            sb.append("\"").append(escapeJson(ctx.getWarnings().get(i))).append("\"");
            if (i < ctx.getWarnings().size() - 1) sb.append(",");
        }
        sb.append("],");
        sb.append("\"orchestrationAuditTrail\":[");
        for (int i = 0; i < ctx.getOrchestrationAuditTrail().size(); i++) {
            sb.append("\"").append(escapeJson(ctx.getOrchestrationAuditTrail().get(i))).append("\"");
            if (i < ctx.getOrchestrationAuditTrail().size() - 1) sb.append(",");
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    public static String toJson(BugReport r) {
        if (r == null) return "null";
        return String.format("{\"id\":\"%s\",\"title\":\"%s\",\"description\":\"%s\",\"rawLog\":\"%s\",\"environment\":\"%s\",\"affectedModuleHint\":\"%s\"}",
                escapeJson(r.getId()), escapeJson(r.getTitle()), escapeJson(r.getDescription()), escapeJson(r.getRawLog()),
                escapeJson(r.getEnvironment()), escapeJson(r.getAffectedModuleHint()));
    }

    public static String toJson(TriageResult t) {
        if (t == null) return "null";
        return String.format("{\"severity\":\"%s\",\"severityDisplay\":\"%s\",\"priority\":\"%s\",\"priorityDisplay\":\"%s\",\"affectedComponent\":\"%s\",\"confidenceScore\":%s,\"reasoning\":\"%s\"}",
                t.getSeverity().name(), escapeJson(t.getSeverity().getDisplayName()), t.getPriority().name(),
                escapeJson(t.getPriority().getDisplayName()), escapeJson(t.getAffectedComponent()), t.getConfidenceScore(),
                escapeJson(t.getReasoning()));
    }

    public static String toJson(LogAnalysisResult l) {
        if (l == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("{\"hasLogs\":%b,\"detectedLogFormat\":\"%s\",\"exceptionType\":\"%s\",\"errorMessage\":\"%s\",\"confidenceScore\":%s,\"totalFramesParsed\":%d,",
                l.isHasLogs(), escapeJson(l.getDetectedLogFormat()), escapeJson(l.getExceptionType()), escapeJson(l.getErrorMessage()), l.getConfidenceScore(), l.getTotalFramesParsed()));
        sb.append("\"failurePoint\":{");
        if (l.getFailurePoint() != null) {
            sb.append(String.format("\"fileName\":\"%s\",\"className\":\"%s\",\"methodName\":\"%s\",\"lineNumber\":%d,\"display\":\"%s\"",
                    escapeJson(l.getFailurePoint().getFileName()), escapeJson(l.getFailurePoint().getClassName()),
                    escapeJson(l.getFailurePoint().getMethodName()), l.getFailurePoint().getLineNumber(),
                    escapeJson(l.getFailurePoint().toDisplayString())));
        }
        sb.append("},\"probableAffectedCodePath\":[");
        List<StackFrame> frames = l.getProbableAffectedCodePath();
        for (int i = 0; i < frames.size(); i++) {
            StackFrame f = frames.get(i);
            sb.append(String.format("{\"frameIndex\":%d,\"callPath\":\"%s\",\"isUserCode\":%b}",
                    f.getFrameIndex(), escapeJson(f.toCallPathEntry()), f.isUserCode()));
            if (i < frames.size() - 1) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }

    public static String toJson(FixRecommendation f) {
        if (f == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("{\"targetFile\":\"%s\",\"confidenceScore\":%s,\"rootCauseAnalysis\":\"%s\",\"summaryRecommendation\":\"%s\",\"codePatchDiff\":\"%s\",",
                escapeJson(f.getTargetFile()), f.getConfidenceScore(), escapeJson(f.getRootCauseAnalysis()),
                escapeJson(f.getSummaryRecommendation()), escapeJson(f.getCodePatchDiff())));
        sb.append("\"fixSteps\":[");
        for (int i = 0; i < f.getFixSteps().size(); i++) {
            sb.append("\"").append(escapeJson(f.getFixSteps().get(i))).append("\"");
            if (i < f.getFixSteps().size() - 1) sb.append(",");
        }
        sb.append("],\"preventionChecklist\":[");
        for (int i = 0; i < f.getPreventionChecklist().size(); i++) {
            sb.append("\"").append(escapeJson(f.getPreventionChecklist().get(i))).append("\"");
            if (i < f.getPreventionChecklist().size() - 1) sb.append(",");
        }
        sb.append("],\"similarHistoricalDefects\":[");
        for (int i = 0; i < f.getSimilarHistoricalDefects().size(); i++) {
            sb.append(toJson(f.getSimilarHistoricalDefects().get(i)));
            if (i < f.getSimilarHistoricalDefects().size() - 1) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }

    public static String toJson(HistoricalDefect d) {
        if (d == null) return "null";
        return String.format("{\"defectId\":\"%s\",\"repository\":\"%s\",\"title\":\"%s\",\"description\":\"%s\",\"component\":\"%s\",\"rootCause\":\"%s\",\"resolvedFix\":\"%s\",\"similarityScore\":%s}",
                escapeJson(d.getDefectId()), escapeJson(d.getRepository()), escapeJson(d.getTitle()), escapeJson(d.getDescription()),
                escapeJson(d.getComponent()), escapeJson(d.getRootCause()), escapeJson(d.getResolvedFix()), d.getSimilarityScore());
    }

    public static String toJson(ValidationReport vr) {
        if (vr == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("{\"totalTestCases\":%d,\"passedCount\":%d,\"failedCount\":%d,\"severityAccuracy\":%s,\"priorityAccuracy\":%s,\"componentAccuracy\":%s,\"overallTriageAccuracy\":%s,\"exceptionAccuracy\":%s,\"failurePointAccuracy\":%s,\"codePathAccuracy\":%s,\"overallLogAccuracy\":%s,\"overallSystemAccuracy\":%s,",
                vr.getTotalTestCases(), vr.getPassedCount(), vr.getFailedCount(), vr.getSeverityAccuracy(), vr.getPriorityAccuracy(),
                vr.getComponentAccuracy(), vr.getOverallTriageAccuracy(), vr.getExceptionAccuracy(), vr.getFailurePointAccuracy(),
                vr.getCodePathAccuracy(), vr.getOverallLogAccuracy(), vr.getOverallSystemAccuracy()));
        sb.append("\"details\":[");
        for (int i = 0; i < vr.getDetails().size(); i++) {
            ValidationReport.TestEvaluationDetail d = vr.getDetails().get(i);
            sb.append(String.format("{\"testId\":\"%s\",\"testName\":\"%s\",\"category\":\"%s\",\"severityMatch\":%b,\"priorityMatch\":%b,\"componentMatch\":%b,\"exceptionMatch\":%b,\"failurePointMatch\":%b,\"actualSeverity\":\"%s\",\"actualPriority\":\"%s\",\"actualComponent\":\"%s\",\"actualException\":\"%s\",\"actualFailurePoint\":\"%s\",\"fullyPassed\":%b}",
                    escapeJson(d.testId), escapeJson(d.testName), escapeJson(d.category), d.severityMatch, d.priorityMatch, d.componentMatch, d.exceptionMatch, d.failurePointMatch,
                    escapeJson(d.actualSeverity), escapeJson(d.actualPriority), escapeJson(d.actualComponent), escapeJson(d.actualException), escapeJson(d.actualFailurePoint), d.fullyPassed));
            if (i < vr.getDetails().size() - 1) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }

    public static String extractJsonField(String json, String field) {
        if (json == null) return null;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"");
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) return unescapeJson(m.group(1));
        return null;
    }

    public static String unescapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
    }
}
