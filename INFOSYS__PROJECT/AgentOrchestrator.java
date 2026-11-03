package com.bugdiagnosis.agent;

import com.bugdiagnosis.knowledgebase.HistoricalDefectKnowledgeBase;
import com.bugdiagnosis.model.BugReport;
import com.bugdiagnosis.model.FixRecommendation;
import com.bugdiagnosis.model.LogAnalysisResult;
import com.bugdiagnosis.model.StructuredBugContext;
import com.bugdiagnosis.model.TriageResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AgentOrchestrator {

    private final TriageAgent triageAgent;
    private final LogAnalysisAgent logAnalysisAgent;
    private final FixRecommendationAgent fixRecommendationAgent;
    private final HistoricalDefectKnowledgeBase knowledgeBase;
    private final ExecutorService executorService;

    public AgentOrchestrator(HistoricalDefectKnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
        this.triageAgent = new TriageAgent();
        this.logAnalysisAgent = new LogAnalysisAgent();
        this.fixRecommendationAgent = new FixRecommendationAgent(knowledgeBase);
        this.executorService = Executors.newFixedThreadPool(4);
    }

    public StructuredBugContext orchestrate(BugReport report) {
        long startTime = System.currentTimeMillis();
        StructuredBugContext context = new StructuredBugContext(report);

        context.addAuditLog("Submission ingested: ID=" + report.getId() + ", Title=\"" + report.getTitle() + "\"");

        try {
            // Milestone 2.3: Parallel agent execution
            CompletableFuture<TriageResult> triageFuture = CompletableFuture.supplyAsync(() -> {
                context.addAuditLog("Triage Agent triggered on thread " + Thread.currentThread().getName());
                return triageAgent.triage(report);
            }, executorService);

            CompletableFuture<LogAnalysisResult> logFuture = CompletableFuture.supplyAsync(() -> {
                context.addAuditLog("Log Analysis Agent triggered on thread " + Thread.currentThread().getName());
                return logAnalysisAgent.analyze(report);
            }, executorService);

            CompletableFuture.allOf(triageFuture, logFuture).get(5000, TimeUnit.MILLISECONDS);

            TriageResult triageResult = triageFuture.get();
            LogAnalysisResult logResult = logFuture.get();

            // Fuse into structured bug context
            context.setTriageResult(triageResult);
            context.setLogAnalysisResult(logResult);
            context.addAuditLog("Triage and Log Analysis outputs fused into structured context.");

            // Downstream: Fix Recommendation Agent
            FixRecommendation fixRecommendation = fixRecommendationAgent.generateRecommendation(context);
            context.setFixRecommendation(fixRecommendation);
            context.addAuditLog("Fix Recommendation synthesized successfully.");

            context.setSuccessful(true);

        } catch (Exception e) {
            context.setSuccessful(false);
            context.addWarning("Orchestration encountered exception: " + e.getMessage());
            fallbackRecovery(report, context);
        } finally {
            context.setTotalProcessingTimeMs(System.currentTimeMillis() - startTime);
            context.addAuditLog("Orchestration completed in " + context.getTotalProcessingTimeMs() + " ms");
        }

        return context;
    }

    private void fallbackRecovery(BugReport report, StructuredBugContext context) {
        if (context.getTriageResult() == null) context.setTriageResult(triageAgent.triage(report));
        if (context.getLogAnalysisResult() == null) context.setLogAnalysisResult(logAnalysisAgent.analyze(report));
        if (context.getFixRecommendation() == null) context.setFixRecommendation(fixRecommendationAgent.generateRecommendation(context));
    }

    public HistoricalDefectKnowledgeBase getKnowledgeBase() { return knowledgeBase; }
    public void shutdown() { executorService.shutdown(); }
}
