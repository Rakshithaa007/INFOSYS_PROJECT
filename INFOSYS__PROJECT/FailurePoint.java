package com.bugdiagnosis.model;

public class FailurePoint {
    private String fileName;
    private String className;
    private String methodName;
    private int lineNumber;
    private boolean userCode;
    private String codeSnippet;

    public FailurePoint() {
        this.lineNumber = -1;
    }

    public FailurePoint(String fileName, String className, String methodName, int lineNumber, boolean userCode) {
        this.fileName = fileName;
        this.className = className;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
        this.userCode = userCode;
    }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }

    public int getLineNumber() { return lineNumber; }
    public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }

    public boolean isUserCode() { return userCode; }
    public void setUserCode(boolean userCode) { this.userCode = userCode; }

    public String getCodeSnippet() { return codeSnippet; }
    public void setCodeSnippet(String codeSnippet) { this.codeSnippet = codeSnippet; }

    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        if (className != null && !className.isEmpty()) {
            sb.append(className);
        }
        if (methodName != null && !methodName.isEmpty()) {
            if (sb.length() > 0) sb.append(".");
            sb.append(methodName);
        }
        if (fileName != null && !fileName.isEmpty()) {
            sb.append(" (").append(fileName);
            if (lineNumber > 0) {
                sb.append(":").append(lineNumber);
            }
            sb.append(")");
        } else if (lineNumber > 0) {
            sb.append(" [line ").append(lineNumber).append("]");
        }
        return sb.length() > 0 ? sb.toString() : "Unknown Failure Point";
    }

    @Override
    public String toString() {
        return toDisplayString();
    }
}