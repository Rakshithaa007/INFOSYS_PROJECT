package com.bugdiagnosis;

import com.bugdiagnosis.agent.AgentOrchestrator;
import com.bugdiagnosis.knowledgebase.HistoricalDefectKnowledgeBase;
import com.bugdiagnosis.server.AppHttpServer;
import com.bugdiagnosis.validation.AccuracyValidator;
import com.bugdiagnosis.validation.ValidationReport;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        System.out.println("Starting Intelligent Bug Diagnosis Platform (Java 21 LTS)...");

        HistoricalDefectKnowledgeBase knowledgeBase = new HistoricalDefectKnowledgeBase();
        AgentOrchestrator orchestrator = new AgentOrchestrator(knowledgeBase);

        boolean runBenchmark = false;
        int port = 8080;

        for (String arg : args) {
            if ("--benchmark".equalsIgnoreCase(arg)) runBenchmark = true;
        }

        if (runBenchmark) {
            AccuracyValidator validator = new AccuracyValidator(orchestrator);
            ValidationReport report = validator.runValidation();

            System.out.println("==========================================================");
            System.out.println("       ACCURACY VALIDATION & BENCHMARK REPORT (M2.4)");
            System.out.println("==========================================================");
            System.out.println("Total Test Cases:          " + report.getTotalTestCases());
            System.out.println("Passed Test Cases:         " + report.getPassedCount());
            System.out.println("Failed Test Cases:         " + report.getFailedCount());
            System.out.println("Overall Triage Accuracy:   " + report.getOverallTriageAccuracy() + "%");
            System.out.println("Overall Log Analysis Acc:  " + report.getOverallLogAccuracy() + "%");
            System.out.println("OVERALL SYSTEM ACCURACY:   " + report.getOverallSystemAccuracy() + "%");
            System.out.println("==========================================================");
            return;
        }

        String currentDir = System.getProperty("user.dir");
        String webDir = currentDir + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "web";
        if (!new File(webDir).exists()) webDir = "src/main/resources/web";

        try {
            AppHttpServer server = new AppHttpServer(port, orchestrator, webDir);
            server.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down platform...");
                server.stop();
                orchestrator.shutdown();
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}