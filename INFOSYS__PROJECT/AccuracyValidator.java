package com.bugdiagnosis.validation;

import com.bugdiagnosis.agent.AgentOrchestrator;
import com.bugdiagnosis.model.*;

import java.util.ArrayList;
import java.util.List;

public class AccuracyValidator {

    private final AgentOrchestrator orchestrator;

    public AccuracyValidator(AgentOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    public List<TestCase> getBenchmarkSuite() {
        List<TestCase> suite = new ArrayList<>();

        suite.add(new TestCase(
                "TC-01", "Tomcat NIO Acceptor Thread Deadlock", "WITH_STACK_TRACE",
                "Tomcat NioEndpoint acceptor deadlocks under high load with poller thread waiting on lock.",
                "java.lang.IllegalStateException: Poller event queue saturated\n\tat org.apache.tomcat.util.net.NioEndpoint$Poller.run(NioEndpoint.java:782)\n\tat org.apache.tomcat.util.net.NioEndpoint$Acceptor.run(NioEndpoint.java:450)",
                Severity.CRITICAL, Priority.P1_CRITICAL, "Networking / NIO Connector", "IllegalStateException", "NioEndpoint.java", 450
        ));

        suite.add(new TestCase(
                "TC-02", "HDFS NameNode Heap Space OOM", "WITH_STACK_TRACE",
                "NameNode throws OutOfMemoryError heap space during simultaneous cluster block reports.",
                "java.lang.OutOfMemoryError: Java heap space\n\tat java.base/java.util.Arrays.copyOf(Arrays.java:3745)\n\tat org.apache.hadoop.hdfs.server.blockmanagement.BlockManager.processReport(BlockManager.java:1890)",
                Severity.CRITICAL, Priority.P1_CRITICAL, "Distributed Storage / HDFS", "OutOfMemoryError", "BlockManager.java", 1890
        ));

        suite.add(new TestCase(
                "TC-03", "Complete Production Service Outage on Database Failure", "DESCRIPTION_ONLY",
                "Severe database connection pool exhaustion has caused complete outage for all user transactions. System crash across all API nodes.",
                "",
                Severity.CRITICAL, Priority.P1_CRITICAL, "Database & Persistence Layer", "NoExceptionProvided", "Unknown", -1
        ));

        suite.add(new TestCase(
                "TC-04", "Typo in Profile Settings Page Header", "DESCRIPTION_ONLY",
                "The header on the user profile settings page has a minor typo: 'Prefrences' instead of 'Preferences'. Purely cosmetic UI flaw.",
                "",
                Severity.LOW, Priority.P4_LOW, "User Interface & Presentation", "NoExceptionProvided", "Unknown", -1
        ));

        suite.add(new TestCase(
                "TC-05", "Python ZeroDivisionError in Analytics", "WITH_STACK_TRACE",
                "Batch analytics job fails abruptly when computing ratio for zero-volume products.",
                "Traceback (most recent call last):\n  File \"app/analytics/metrics_calculator.py\", line 84, in calculate_conversion_rate\n    ratio = total_conversions / total_impressions\nZeroDivisionError: division by zero",
                Severity.HIGH, Priority.P2_HIGH, "Core Application Logic", "ZeroDivisionError", "metrics_calculator.py", 84
        ));

        suite.add(new TestCase(
                "TC-06", "Node.js Express TypeError Undefined", "WITH_STACK_TRACE",
                "Express API router crashes when client passes null auth authorization headers.",
                "TypeError: Cannot read properties of undefined (reading 'split')\n    at verifyToken (/app/src/auth/jwtMiddleware.js:42:25)",
                Severity.HIGH, Priority.P2_HIGH, "Authentication & Security", "TypeError", "jwtMiddleware.js", 42
        ));

        return suite;
    }

    public ValidationReport runValidation() {
        ValidationReport report = new ValidationReport();
        List<TestCase> suite = getBenchmarkSuite();
        report.setTotalTestCases(suite.size());

        int severityMatches = 0;
        int priorityMatches = 0;
        int componentMatches = 0;
        int exceptionMatches = 0;
        int failurePointMatches = 0;
        int codePathMatches = 0;
        int fullyPassedCount = 0;

        for (TestCase tc : suite) {
            BugReport bug = new BugReport(tc.getName(), tc.getDescription(), tc.getRawLog());
            StructuredBugContext context = orchestrator.orchestrate(bug);

            TriageResult triage = context.getTriageResult();
            LogAnalysisResult log = context.getLogAnalysisResult();

            ValidationReport.TestEvaluationDetail detail = new ValidationReport.TestEvaluationDetail();
            detail.testId = tc.getId();
            detail.testName = tc.getName();
            detail.category = tc.getInputCategory();

            detail.actualSeverity = triage.getSeverity().name();
            detail.expectedSeverity = tc.getExpectedSeverity().name();
            detail.severityMatch = triage.getSeverity() == tc.getExpectedSeverity();
            if (detail.severityMatch) severityMatches++;

            detail.actualPriority = triage.getPriority().name();
            detail.expectedPriority = tc.getExpectedPriority().name();
            detail.priorityMatch = triage.getPriority() == tc.getExpectedPriority();
            if (detail.priorityMatch) priorityMatches++;

            detail.actualComponent = triage.getAffectedComponent();
            detail.expectedComponent = tc.getExpectedComponent();
            detail.componentMatch = compareComponents(triage.getAffectedComponent(), tc.getExpectedComponent());
            if (detail.componentMatch) componentMatches++;

            detail.actualException = log.getExceptionType();
            detail.expectedException = tc.getExpectedExceptionType();
            detail.exceptionMatch = compareExceptions(log.getExceptionType(), tc.getExpectedExceptionType());
            if (detail.exceptionMatch) exceptionMatches++;

            detail.actualFailurePoint = log.getFailurePoint().toDisplayString();
            detail.expectedFailurePoint = tc.getExpectedFailureFile() + (tc.getExpectedFailureLine() > 0 ? ":" + tc.getExpectedFailureLine() : "");
            detail.failurePointMatch = compareFailurePoint(log.getFailurePoint(), tc.getExpectedFailureFile(), tc.getExpectedFailureLine());
            if (detail.failurePointMatch) failurePointMatches++;

            boolean hasExpectedTrace = !tc.getInputCategory().equals("DESCRIPTION_ONLY");
            boolean codePathExtracted = (!hasExpectedTrace && log.getTotalFramesParsed() == 0) || (hasExpectedTrace && log.getTotalFramesParsed() > 0);
            if (codePathExtracted) codePathMatches++;

            detail.fullyPassed = detail.severityMatch && detail.priorityMatch && detail.componentMatch && detail.exceptionMatch && detail.failurePointMatch;
            if (detail.fullyPassed) fullyPassedCount++;

            report.getDetails().add(detail);
        }

        int total = suite.size();
        report.setPassedCount(fullyPassedCount);
        report.setFailedCount(total - fullyPassedCount);

        report.setSeverityAccuracy(roundToPercent((double) severityMatches / total));
        report.setPriorityAccuracy(roundToPercent((double) priorityMatches / total));
        report.setComponentAccuracy(roundToPercent((double) componentMatches / total));
        report.setOverallTriageAccuracy(roundToPercent((double) (severityMatches + priorityMatches + componentMatches) / (total * 3)));

        report.setExceptionAccuracy(roundToPercent((double) exceptionMatches / total));
        report.setFailurePointAccuracy(roundToPercent((double) failurePointMatches / total));
        report.setCodePathAccuracy(roundToPercent((double) codePathMatches / total));
        report.setOverallLogAccuracy(roundToPercent((double) (exceptionMatches + failurePointMatches + codePathMatches) / (total * 3)));

        report.setOverallSystemAccuracy(roundToPercent((report.getOverallTriageAccuracy() + report.getOverallLogAccuracy()) / 2.0));

        return report;
    }

    private boolean compareComponents(String actual, String expected) {
        if (actual == null || expected == null) return false;
        return actual.equalsIgnoreCase(expected) || actual.toLowerCase().contains(expected.toLowerCase()) || expected.toLowerCase().contains(actual.toLowerCase());
    }

    private boolean compareExceptions(String actual, String expected) {
        if (actual == null || expected == null) return false;
        return actual.equalsIgnoreCase(expected) || actual.toLowerCase().contains(expected.toLowerCase()) || expected.toLowerCase().contains(actual.toLowerCase());
    }

    private boolean compareFailurePoint(FailurePoint actual, String expectedFile, int expectedLine) {
        if (expectedFile.equals("Unknown")) return true;
        if (actual.getFileName() == null) return false;
        boolean fileMatch = actual.getFileName().equalsIgnoreCase(expectedFile) || actual.getFileName().endsWith(expectedFile);
        if (!fileMatch) return false;
        return expectedLine <= 0 || actual.getLineNumber() == expectedLine || Math.abs(actual.getLineNumber() - expectedLine) <= 2;
    }

    private double roundToPercent(double val) { return Math.round(val * 1000.0) / 10.0; }
}
