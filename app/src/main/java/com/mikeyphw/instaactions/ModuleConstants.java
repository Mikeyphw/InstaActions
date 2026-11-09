package com.mikeyphw.instaactions;

/** Immutable R0 contract. No Instagram account data or hooks. */
public final class ModuleConstants {
    public static final String TARGET_PACKAGE = "com.instagram.android";
    public static final String ENTRY_LOG_TAG = "INA_R0";
    public static final boolean ACTION_HOOKS_ENABLED = false;

    private ModuleConstants() {
        throw new AssertionError("No instances");
    }

    public static boolean isMainInstagramProcess(String processName) {
        return TARGET_PACKAGE.equals(processName);
    }
}
