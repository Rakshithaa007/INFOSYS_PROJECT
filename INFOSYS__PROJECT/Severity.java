package com.bugdiagnosis.model;

public enum Severity {
    CRITICAL("Critical", 4, "System crash, data corruption, severe security vulnerability, or complete service outage."),
    HIGH("High", 3, "Major feature broken, memory leak, significant performance degradation, or blocking primary user flow."),
    MEDIUM("Medium", 2, "Secondary feature malfunctioning, non-fatal exceptions, cosmetic defect in key workflow, or workaround available."),
    LOW("Low", 1, "Minor cosmetic flaw, typo in documentation/UI, minor log noise, or negligible functional impact.");

    private final String displayName;
    private final int weight;
    private final String description;

    Severity(String displayName, int weight, String description) {
        this.displayName = displayName;
        this.weight = weight;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getWeight() {
        return weight;
    }

    public String getDescription() {
        return description;
    }

    public static Severity fromString(String text) {
        if (text == null) return MEDIUM;
        String clean = text.trim().toUpperCase();
        for (Severity s : values()) {
            if (s.name().equals(clean) || s.displayName.toUpperCase().equals(clean)) {
                return s;
            }
        }
        if (clean.contains("CRIT") || clean.contains("BLOCKER") || clean.contains("FATAL")) return CRITICAL;
        if (clean.contains("HIGH") || clean.contains("MAJOR")) return HIGH;
        if (clean.contains("MED") || clean.contains("NORMAL")) return MEDIUM;
        if (clean.contains("LOW") || clean.contains("MINOR") || clean.contains("TRIVIAL")) return LOW;
        return MEDIUM;
    }
}