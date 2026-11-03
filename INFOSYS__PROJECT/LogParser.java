package com.bugdiagnosis.ingestion;

import com.bugdiagnosis.model.FailurePoint;
import com.bugdiagnosis.model.LogAnalysisResult;
import com.bugdiagnosis.model.StackFrame;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogParser {

    // Java Patterns
    private static final Pattern JAVA_EXCEPTION_HEADER = Pattern.compile(
            "(?:Exception in thread \"[^\"]+\"\\s+)?([a-zA-Z0-9_$.]+(?:Exception|Error|Throwable|Fault))(?::\\s*(.*))?",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern JAVA_CAUSED_BY = Pattern.compile(
            "Caused by:\\s+([a-zA-Z0-9_$.]+(?:Exception|Error|Throwable|Fault))(?::\\s*(.*))?",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern JAVA_STACK_LINE = Pattern.compile(
            "^\\s*at\\s+([a-zA-Z0-9_$.]+)\\.([a-zA-Z0-9_$<>]+)\\((?:([a-zA-Z0-9_$.-]+\\.java):(\\d+)|(Native Method)|(Unknown Source))\\)",
            Pattern.CASE_INSENSITIVE
    );

    // Python Patterns
    private static final Pattern PYTHON_TRACEBACK_START = Pattern.compile("^\\s*Traceback \\(most recent call last\\):", Pattern.CASE_INSENSITIVE);
    private static final Pattern PYTHON_STACK_LINE = Pattern.compile(
            "^\\s*File\\s+[\"']([^\"']+)[\"'],\\s+line\\s+(\\d+)(?:,\\s+in\\s+([a-zA-Z0-9_<>]+))?",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern PYTHON_EXCEPTION_LINE = Pattern.compile(
            "^\\s*([a-zA-Z0-9_]+(?:Error|Exception|Interrupt|Exit)):\\s*(.*)$"
    );

    // JavaScript / Node Patterns
    private static final Pattern JS_EXCEPTION_HEADER = Pattern.compile(
            "^\\s*([a-zA-Z0-9_]+(?:Error|Exception)):\\s*(.*)$"
    );
    private static final Pattern JS_STACK_LINE = Pattern.compile(
            "^\\s*at\\s+(?:([a-zA-Z0-9_$.<>]+)\\s+)?\\(?([^():]+):(\\d+)(?::(\\d+))?\\)?",
            Pattern.CASE_INSENSITIVE
    );

    // Go Panic Patterns
    private static final Pattern GO_PANIC = Pattern.compile("^\\s*panic:\\s*(.*)$");
    private static final Pattern GO_STACK_FUNC = Pattern.compile("^\\s*([a-zA-Z0-9_\\/.]+\\.[a-zA-Z0-9_]+)\\(.*\\)");
    private static final Pattern GO_STACK_FILE = Pattern.compile("^\\s*([a-zA-Z0-9_\\/.\\-]+):(\\d+)(?:\\s+\\+0x[0-9a-fA-F]+)?");

    // General Log Line (Log4j / Syslog / Generic)
    private static final Pattern LOG_LEVEL_PATTERN = Pattern.compile(
            "\\[?(FATAL|CRITICAL|SEVERE|ERROR|WARN|WARNING|INFO)\\]?\\s*[:-]?\\s*(.*)",
            Pattern.CASE_INSENSITIVE
    );

    public LogAnalysisResult parse(String rawLog) {
        LogAnalysisResult result = new LogAnalysisResult();

        if (rawLog == null || rawLog.trim().isEmpty()) {
            result.setHasLogs(false);
            result.setDetectedLogFormat("NONE");
            result.setConfidenceScore(0.0);
            result.setParsingNotes("No stack trace or log content provided in submission.");
            result.setExceptionType("NoExceptionProvided");
            result.setErrorMessage("No log stream attached to defect report.");
            return result;
        }

        result.setHasLogs(true);
        String[] lines = rawLog.split("\\r?\\n");
        String format = detectFormat(rawLog, lines);
        result.setDetectedLogFormat(format);

        switch (format) {
            case "JAVA":
                parseJavaTrace(lines, result);
                break;
            case "PYTHON":
                parsePythonTrace(lines, result);
                break;
            case "JAVASCRIPT":
                parseJavaScriptTrace(lines, result);
                break;
            case "GO":
                parseGoTrace(lines, result);
                break;
            default:
                parseGenericOrUnstructured(lines, result);
                break;
        }

        calculateConfidence(result);
        return result;
    }

    private String detectFormat(String rawLog, String[] lines) {
        if (PYTHON_TRACEBACK_START.matcher(rawLog).find() || rawLog.contains("Traceback (most recent call last):")) {
            return "PYTHON";
        }
        for (String line : lines) {
            if (line.trim().startsWith("at ") && line.contains(".java:")) return "JAVA";
            if (line.contains("Exception in thread") || line.contains("Caused by:")) return "JAVA";
            if (line.trim().startsWith("panic:") || line.trim().startsWith("goroutine ")) return "GO";
        }
        if (rawLog.contains("TypeError:") || rawLog.contains("ReferenceError:") || rawLog.contains("SyntaxError:")) {
            return "JAVASCRIPT";
        }
        for (String line : lines) {
            if (JAVA_EXCEPTION_HEADER.matcher(line.trim()).find()) return "JAVA";
            if (JS_STACK_LINE.matcher(line.trim()).find()) return "JAVASCRIPT";
        }
        return "GENERIC_OR_UNSTRUCTURED";
    }

    private void parseJavaTrace(String[] lines, LogAnalysisResult result) {
        String primaryException = null;
        String primaryMessage = null;
        String causedByException = null;
        String causedByMessage = null;
        List<StackFrame> frames = new ArrayList<>();
        int frameIndex = 0;

        for (String line : lines) {
            String trimmed = line.trim();

            Matcher exMatcher = JAVA_EXCEPTION_HEADER.matcher(trimmed);
            if (exMatcher.find() && primaryException == null) {
                primaryException = cleanExceptionName(exMatcher.group(1));
                primaryMessage = exMatcher.group(2) != null ? exMatcher.group(2).trim() : "";
            }

            Matcher cbMatcher = JAVA_CAUSED_BY.matcher(trimmed);
            if (cbMatcher.find()) {
                causedByException = cleanExceptionName(cbMatcher.group(1));
                causedByMessage = cbMatcher.group(2) != null ? cbMatcher.group(2).trim() : "";
            }

            Matcher frameMatcher = JAVA_STACK_LINE.matcher(line);
            if (frameMatcher.find()) {
                String className = frameMatcher.group(1);
                String methodName = frameMatcher.group(2);
                String fileName = frameMatcher.group(3);
                String lineStr = frameMatcher.group(4);
                boolean isNative = frameMatcher.group(5) != null;
                int lineNum = (lineStr != null) ? Integer.parseInt(lineStr) : -1;

                boolean isUserCode = isLikelyUserCode(className);
                StackFrame frame = new StackFrame(frameIndex++, line.trim(), className, methodName, fileName, lineNum, isUserCode);
                frame.setNativeMethod(isNative);
                frames.add(frame);
            }
        }

        if (causedByException != null) {
            result.setExceptionType(causedByException);
            result.setErrorMessage(causedByMessage != null && !causedByMessage.isEmpty() ? causedByMessage : (primaryMessage != null ? primaryMessage : ""));
            result.setParsingNotes("Extracted root cause from 'Caused by' chain.");
        } else if (primaryException != null) {
            result.setExceptionType(primaryException);
            result.setErrorMessage(primaryMessage != null ? primaryMessage : "");
            result.setParsingNotes("Extracted from primary Java exception header.");
        } else {
            result.setExceptionType("UnknownException");
            result.setErrorMessage(lines.length > 0 ? lines[0].trim() : "");
        }

        result.setProbableAffectedCodePath(frames);
        result.setTotalFramesParsed(frames.size());
        resolveFailurePoint(frames, result);
    }

    private void parsePythonTrace(String[] lines, LogAnalysisResult result) {
        List<StackFrame> frames = new ArrayList<>();
        int frameIndex = 0;
        String exceptionType = null;
        String errorMessage = null;

        for (String line : lines) {
            Matcher frameMatcher = PYTHON_STACK_LINE.matcher(line);
            if (frameMatcher.find()) {
                String file = frameMatcher.group(1);
                int lineNum = Integer.parseInt(frameMatcher.group(2));
                String method = frameMatcher.group(3);
                String shortFile = extractSimpleFileName(file);
                boolean isUser = !file.contains("site-packages") && !file.contains("lib/python");

                StackFrame frame = new StackFrame(frameIndex++, line.trim(), extractClassNameFromPythonPath(file), method, shortFile, lineNum, isUser);
                frames.add(frame);
            }

            Matcher exMatcher = PYTHON_EXCEPTION_LINE.matcher(line.trim());
            if (exMatcher.find()) {
                exceptionType = exMatcher.group(1);
                errorMessage = exMatcher.group(2).trim();
            }
        }

        result.setExceptionType(exceptionType != null ? exceptionType : "PythonException");
        result.setErrorMessage(errorMessage != null ? errorMessage : "");
        result.setProbableAffectedCodePath(frames);
        result.setTotalFramesParsed(frames.size());
        result.setParsingNotes("Parsed standard Python traceback format.");

        if (!frames.isEmpty()) {
            StackFrame bottom = frames.get(frames.size() - 1);
            FailurePoint fp = new FailurePoint(bottom.getFileName(), bottom.getClassName(), bottom.getMethodName(), bottom.getLineNumber(), bottom.isUserCode());
            result.setFailurePoint(fp);
        }
    }

    private void parseJavaScriptTrace(String[] lines, LogAnalysisResult result) {
        List<StackFrame> frames = new ArrayList<>();
        int frameIndex = 0;
        String exceptionType = null;
        String errorMessage = null;

        for (String line : lines) {
            String trimmed = line.trim();
            Matcher exMatcher = JS_EXCEPTION_HEADER.matcher(trimmed);
            if (exMatcher.find() && exceptionType == null) {
                exceptionType = exMatcher.group(1);
                errorMessage = exMatcher.group(2).trim();
                continue;
            }

            Matcher frameMatcher = JS_STACK_LINE.matcher(trimmed);
            if (frameMatcher.find()) {
                String method = frameMatcher.group(1);
                String file = frameMatcher.group(2);
                int lineNum = frameMatcher.group(3) != null ? Integer.parseInt(frameMatcher.group(3)) : -1;
                String shortFile = extractSimpleFileName(file);
                boolean isUser = file != null && !file.contains("node_modules");

                StackFrame frame = new StackFrame(frameIndex++, trimmed, "module", method != null ? method : "anonymous", shortFile, lineNum, isUser);
                frames.add(frame);
            }
        }

        result.setExceptionType(exceptionType != null ? exceptionType : "JavaScriptError");
        result.setErrorMessage(errorMessage != null ? errorMessage : "");
        result.setProbableAffectedCodePath(frames);
        result.setTotalFramesParsed(frames.size());
        result.setParsingNotes("Parsed JavaScript V8/Node.js stack trace.");

        if (!frames.isEmpty()) {
            StackFrame top = frames.get(0);
            FailurePoint fp = new FailurePoint(top.getFileName(), top.getClassName(), top.getMethodName(), top.getLineNumber(), top.isUserCode());
            result.setFailurePoint(fp);
        }
    }

    private void parseGoTrace(String[] lines, LogAnalysisResult result) {
        List<StackFrame> frames = new ArrayList<>();
        String panicMsg = "";

        for (int i = 0; i < lines.length; i++) {
            Matcher pMatcher = GO_PANIC.matcher(lines[i].trim());
            if (pMatcher.find()) panicMsg = pMatcher.group(1);

            Matcher fnMatcher = GO_STACK_FUNC.matcher(lines[i].trim());
            if (fnMatcher.find() && i + 1 < lines.length) {
                Matcher fileMatcher = GO_STACK_FILE.matcher(lines[i + 1].trim());
                if (fileMatcher.find()) {
                    String func = fnMatcher.group(1);
                    String file = fileMatcher.group(1);
                    int line = Integer.parseInt(fileMatcher.group(2));
                    StackFrame frame = new StackFrame(frames.size(), lines[i].trim(), "main", func, extractSimpleFileName(file), line, true);
                    frames.add(frame);
                }
            }
        }

        result.setExceptionType("GoPanic");
        result.setErrorMessage(panicMsg);
        result.setProbableAffectedCodePath(frames);
        result.setTotalFramesParsed(frames.size());
        result.setParsingNotes("Parsed Golang panic dump.");
        if (!frames.isEmpty()) {
            StackFrame top = frames.get(0);
            result.setFailurePoint(new FailurePoint(top.getFileName(), top.getClassName(), top.getMethodName(), top.getLineNumber(), true));
        }
    }

    private void parseGenericOrUnstructured(String[] lines, LogAnalysisResult result) {
        String detectedEx = "RuntimeError";
        String extractedMsg = "";

        for (String line : lines) {
            Matcher lm = LOG_LEVEL_PATTERN.matcher(line);
            if (lm.find()) {
                String level = lm.group(1).toUpperCase();
                String msg = lm.group(2);
                if (level.contains("ERROR") || level.contains("FATAL") || level.contains("CRIT")) {
                    if (extractedMsg.isEmpty()) extractedMsg = msg;
                }
            }
            if (line.toLowerCase().contains("nullpointer")) detectedEx = "NullPointerException";
            else if (line.toLowerCase().contains("outofmemory")) detectedEx = "OutOfMemoryError";
            else if (line.toLowerCase().contains("timeout")) detectedEx = "TimeoutException";
            else if (line.toLowerCase().contains("sql")) detectedEx = "SQLException";
            else if (line.toLowerCase().contains("segmentation")) detectedEx = "SegmentationFault";
        }

        if (extractedMsg.isEmpty() && lines.length > 0) extractedMsg = lines[0].trim();

        result.setExceptionType(detectedEx);
        result.setErrorMessage(extractedMsg);
        result.setParsingNotes("Extracted via pattern analysis from unstructured/generic log stream.");
        result.setTotalFramesParsed(0);

        Pattern genericFilePattern = Pattern.compile("([a-zA-Z0-9_.-]+\\.(?:java|py|js|ts|cpp|c|go)):(\\d+)", Pattern.CASE_INSENSITIVE);
        for (String line : lines) {
            Matcher m = genericFilePattern.matcher(line);
            if (m.find()) {
                String file = m.group(1);
                int lineNum = Integer.parseInt(m.group(2));
                FailurePoint fp = new FailurePoint(file, file, "unknown", lineNum, true);
                result.setFailurePoint(fp);
                break;
            }
        }
    }

    private void resolveFailurePoint(List<StackFrame> frames, LogAnalysisResult result) {
        if (frames.isEmpty()) {
            result.setFailurePoint(new FailurePoint("Unknown", "Unknown", "Unknown", -1, false));
            return;
        }

        StackFrame target = null;
        for (StackFrame frame : frames) {
            if (frame.isUserCode() && frame.getLineNumber() > 0) {
                target = frame;
                break;
            }
        }
        if (target == null) target = frames.get(0);

        FailurePoint fp = new FailurePoint(
                target.getFileName(),
                target.getClassName(),
                target.getMethodName(),
                target.getLineNumber(),
                target.isUserCode()
        );
        result.setFailurePoint(fp);
    }

    private void calculateConfidence(LogAnalysisResult result) {
        double score = 0.50;
        if (result.getExceptionType() != null && !result.getExceptionType().equals("UnknownException") && !result.getExceptionType().equals("NoExceptionProvided")) {
            score += 0.20;
        }
        if (result.getErrorMessage() != null && !result.getErrorMessage().isEmpty()) score += 0.10;
        if (result.getFailurePoint() != null && result.getFailurePoint().getLineNumber() > 0) score += 0.15;
        if (result.getTotalFramesParsed() > 0) score += 0.05;
        if (!result.isHasLogs()) score = 0.0;

        result.setConfidenceScore(Math.min(1.0, Math.max(0.0, Math.round(score * 100.0) / 100.0)));
    }

    private boolean isLikelyUserCode(String className) {
        if (className == null) return false;
        return !className.startsWith("java.") && !className.startsWith("javax.") && !className.startsWith("jdk.") &&
               !className.startsWith("org.springframework.") && !className.startsWith("org.apache.catalina.");
    }

    private String cleanExceptionName(String ex) {
        if (ex == null) return "Exception";
        int lastDot = ex.lastIndexOf('.');
        return lastDot >= 0 ? ex.substring(lastDot + 1) : ex;
    }

    private String extractSimpleFileName(String path) {
        if (path == null) return "";
        int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }

    private String extractClassNameFromPythonPath(String path) {
        String simple = extractSimpleFileName(path);
        if (simple.endsWith(".py")) return simple.substring(0, simple.length() - 3);
        return simple;
    }
}