package com.bugdiagnosis.model;

public class StackFrame {
    private int frameIndex;
    private String rawLine;
    private String className;
    private String methodName;
    private String fileName;
    private int lineNumber;
    private boolean nativeMethod;
    private boolean userCode;

    public StackFrame() {
        this.lineNumber = -1;
    }

    public StackFrame(int frameIndex, String rawLine, String className, String methodName, String fileName, int lineNumber, boolean userCode) {
        this.frameIndex = frameIndex;
        this.rawLine = rawLine;
        this.className = className;
        this.methodName = methodName;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.userCode = userCode;
    }

    public int getFrameIndex() { return frameIndex; }
    public void setFrameIndex(int frameIndex) { this.frameIndex = frameIndex; }

    public String getRawLine() { return rawLine; }
    public void setRawLine(String rawLine) { this.rawLine = rawLine; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public int getLineNumber() { return lineNumber; }
    public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }

    public boolean isNativeMethod() { return nativeMethod; }
    public void setNativeMethod(boolean nativeMethod) { this.nativeMethod = nativeMethod; }

    public boolean isUserCode() { return userCode; }
    public void setUserCode(boolean userCode) { this.userCode = userCode; }

    public String toCallPathEntry() {
        StringBuilder sb = new StringBuilder();
        if (className != null && !className.isEmpty()) {
            sb.append(className).append(".");
        }
        sb.append(methodName != null ? methodName : "<unknown>");
        if (fileName != null && !fileName.isEmpty()) {
            sb.append(" (").append(fileName);
            if (lineNumber > 0) sb.append(":").append(lineNumber);
            sb.append(")");
        }
        return sb.toString();
    }
}