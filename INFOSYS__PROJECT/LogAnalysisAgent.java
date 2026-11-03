package com.bugdiagnosis.agent;

import com.bugdiagnosis.ingestion.LogParser;
import com.bugdiagnosis.model.BugReport;
import com.bugdiagnosis.model.LogAnalysisResult;

public class LogAnalysisAgent {

    private final LogParser logParser;

    public LogAnalysisAgent() {
        this.logParser = new LogParser();
    }

    public LogAnalysisResult analyze(BugReport report) {
        String logContent = report.getRawLog();
        if ((logContent == null || logContent.trim().isEmpty()) && report.getStackTrace() != null) {
            logContent = report.getStackTrace();
        }

        LogAnalysisResult result = logParser.parse(logContent);

        if (!result.isHasLogs() && report.getDescription() != null) {
            LogAnalysisResult descCheck = logParser.parse(report.getDescription());
            if (descCheck.isHasLogs()) {
                descCheck.setParsingNotes("Extracted log information from bug description field.");
                return descCheck;
            }
        }

        return result;
    }

    public LogAnalysisResult analyzeLogString(String rawLog) {
        return logParser.parse(rawLog);
    }
}
