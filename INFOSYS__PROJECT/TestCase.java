package com.bugdiagnosis.model;

public class TestCase {
    private String id;
    private String name;
    private String description;
    private String rawLog;
    private String inputCategory;
    private Severity expectedSeverity;
    private Priority expectedPriority;
    private String expectedComponent;
    private String expectedExceptionType;
    private String expectedFailureFile;
    private int expectedFailureLine;

    public TestCase() {}

    public TestCase(String id, String name, String inputCategory, String description, String rawLog,
                    Severity expectedSeverity, Priority expectedPriority, String expectedComponent,
                    String expectedExceptionType, String expectedFailureFile, int expectedFailureLine) {
        this.id = id;
        this.name = name;
        this.inputCategory = inputCategory;
        this.description = description;
        this.rawLog = rawLog;
        this.expectedSeverity = expectedSeverity;
        this.expectedPriority = expectedPriority;
        this.expectedComponent = expectedComponent;
        this.expectedExceptionType = expectedExceptionType;
        this.expectedFailureFile = expectedFailureFile;
        this.expectedFailureLine = expectedFailureLine;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getRawLog() { return rawLog; }
    public String getInputCategory() { return inputCategory; }
    public Severity getExpectedSeverity() { return expectedSeverity; }
    public Priority getExpectedPriority() { return expectedPriority; }
    public String getExpectedComponent() { return expectedComponent; }
    public String getExpectedExceptionType() { return expectedExceptionType; }
    public String getExpectedFailureFile() { return expectedFailureFile; }
    public int getExpectedFailureLine() { return expectedFailureLine; }
}