package com.bugdiagnosis.model;

public enum Priority {
    P1_CRITICAL("P1 - Blocker", 1, "Immediate resolution required; completely halts deployment or production operations."),
    P2_HIGH("P2 - Urgent", 2, "High urgency; critical impact on business operations or significant user cohort."),
    P3_MEDIUM("P3 - Normal", 3, "Standard defect to be addressed in the regular release cycle; workarounds available."),
    P4_LOW("P4 - Low", 4, "Low priority enhancement or cosmetic bug; schedule when higher priority tasks are complete.");

    private final String displayName;
    private final int level;
    private final String description;

    Priority(String displayName, int level, String description) {
        this.displayName = displayName;
        this.level = level;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    public static Priority fromString(String text) {
        if (text == null) return P3_MEDIUM;
        String clean = text.trim().toUpperCase();
        for (Priority p : values()) {
            if (p.name().equals(clean) || p.displayName.toUpperCase().equals(clean)) {
                return p;
            }
        }
        if (clean.contains("P1") || clean.contains("BLOCK") || clean.contains("IMMEDIATE")) return P1_CRITICAL;
        if (clean.contains("P2") || clean.contains("URGENT") || clean.contains("HIGH")) return P2_HIGH;
        if (clean.contains("P3") || clean.contains("NORMAL") || clean.contains("MED")) return P3_MEDIUM;
        if (clean.contains("P4") || clean.contains("LOW") || clean.contains("MINOR")) return P4_LOW;
        return P3_MEDIUM;
    }
}