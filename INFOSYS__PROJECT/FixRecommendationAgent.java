package com.bugdiagnosis.agent;

import com.bugdiagnosis.knowledgebase.HistoricalDefectKnowledgeBase;
import com.bugdiagnosis.knowledgebase.VectorStore;
import com.bugdiagnosis.model.*;

import java.util.ArrayList;
import java.util.List;

public class FixRecommendationAgent {

    private final HistoricalDefectKnowledgeBase knowledgeBase;

    public FixRecommendationAgent(HistoricalDefectKnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    public FixRecommendation generateRecommendation(StructuredBugContext context) {
        FixRecommendation rec = new FixRecommendation();
        BugReport report = context.getBugReport();
        LogAnalysisResult logResult = context.getLogAnalysisResult();

        // 1. RAG Retrieval from Historical Defect Knowledge Base
        List<VectorStore.SearchResult> ragMatches = knowledgeBase.searchSimilar(report, logResult, 3);
        List<HistoricalDefect> similarDefects = new ArrayList<>();
        for (VectorStore.SearchResult sr : ragMatches) {
            similarDefects.add(sr.getDefect());
        }
        rec.setSimilarHistoricalDefects(similarDefects);

        // 2. Synthesize Root Cause Analysis
        String ex = (logResult != null && logResult.getExceptionType() != null) ? logResult.getExceptionType() : "Runtime Fault";
        String failureTarget = (logResult != null && logResult.getFailurePoint() != null) ? logResult.getFailurePoint().toDisplayString() : "Core Logic Flow";
        rec.setTargetFile((logResult != null && logResult.getFailurePoint() != null && logResult.getFailurePoint().getFileName() != null)
                ? logResult.getFailurePoint().getFileName() : "ApplicationCore.java");

        StringBuilder rootCause = new StringBuilder();
        rootCause.append(String.format("Root cause identified as unhandled %s at failure point [%s]. ", ex, failureTarget));

        if (!similarDefects.isEmpty()) {
            HistoricalDefect topMatch = similarDefects.get(0);
            rootCause.append(String.format("Pattern exhibits high semantic similarity (%.1f%%) to resolved %s defect %s: \"%s\".",
                    topMatch.getSimilarityScore() * 100.0, topMatch.getRepository(), topMatch.getDefectId(), topMatch.getRootCause()));
        } else {
            rootCause.append("Defect pattern corresponds to missing precondition validation or resource state race condition.");
        }
        rec.setRootCauseAnalysis(rootCause.toString());

        rec.setSummaryRecommendation(String.format("Remediate %s by implementing defensive guard assertions and synchronizing resource access in %s.",
                ex, rec.getTargetFile()));

        // 3. Actionable Steps
        List<String> steps = new ArrayList<>();
        steps.add(String.format("1. Inspect method '%s' in '%s' at line %s.",
                (logResult != null && logResult.getFailurePoint() != null ? logResult.getFailurePoint().getMethodName() : "caller"),
                rec.getTargetFile(),
                (logResult != null && logResult.getFailurePoint() != null && logResult.getFailurePoint().getLineNumber() > 0 ? logResult.getFailurePoint().getLineNumber() : "indicated")));
        steps.add("2. Add explicit non-null / bounds validation guards before dereferencing.");
        steps.add("3. Isolate shared state mutations within atomic or synchronized blocks.");
        steps.add("4. Implement a graceful fallback / default recovery path instead of propagating raw exceptions.");
        steps.add("5. Add automated regression unit test verifying boundary inputs and null parameter safety.");
        rec.setFixSteps(steps);

        rec.setCodePatchDiff(generateUnifiedPatchDiff(rec.getTargetFile(), logResult, similarDefects));

        // 4. Prevention Checklist
        List<String> checklist = new ArrayList<>();
        checklist.add("Enable static analysis rule in CI/CD (SpotBugs / SonarQube) to flag unvalidated dereferences.");
        checklist.add("Add automated contract annotations (@NonNull, Optional<T>, or input schema validation).");
        checklist.add("Verify thread-safety guarantees under concurrent load tests before release.");
        rec.setPreventionChecklist(checklist);

        double conf = 0.70;
        if (!similarDefects.isEmpty() && similarDefects.get(0).getSimilarityScore() > 0.40) conf += 0.15;
        if (logResult != null && logResult.getFailurePoint() != null && logResult.getFailurePoint().getLineNumber() > 0) conf += 0.10;
        rec.setConfidenceScore(Math.min(0.96, Math.round(conf * 100.0) / 100.0));

        return rec;
    }

    private String generateUnifiedPatchDiff(String fileName, LogAnalysisResult logResult, List<HistoricalDefect> similarDefects) {
        if (!similarDefects.isEmpty() && similarDefects.get(0).getPatchDiff() != null && !similarDefects.get(0).getPatchDiff().isEmpty()) {
            HistoricalDefect top = similarDefects.get(0);
            return String.format("# Patch adapted from verified resolution of %s (%s)\n%s",
                    top.getDefectId(), top.getRepository(), top.getPatchDiff());
        }

        int line = (logResult != null && logResult.getFailurePoint() != null && logResult.getFailurePoint().getLineNumber() > 0)
                ? logResult.getFailurePoint().getLineNumber() : 42;

        return String.format(
                "--- a/src/%s\n+++ b/src/%s\n@@ -%d,4 +%d,8 @@\n-   targetResource.processRequest(context);\n+   if (targetResource == null || !targetResource.isInitialized()) {\n+       log.warn(\"Target resource uninitialized, applying fallback handler\");\n+       return fallbackResponse(context);\n+   }\n+   targetResource.processRequest(context);\n",
                fileName, fileName, line, line
        );
    }
}
