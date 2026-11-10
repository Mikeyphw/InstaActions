package com.mikeyphw.instaactions;

/**
 * Diagnostic-only per-version scope. A read-only marker query MUST NOT confer
 * permission to bind a live Media instance or mutate Instagram state.
 */
public final class R1CDiscoveryPolicy {
    public enum Outcome { MISSING, UNIQUE_CANDIDATE, AMBIGUOUS, FAILED }
    public static final boolean MEDIA_IDENTITY_VERIFIED = false;
    public static final boolean BINDING_HOOKS_ENABLED = false;
    public static final boolean ACTION_HOOKS_ENABLED = false;
    public static final int MAX_MARKER_QUERIES = 3;
    public static final int MAX_CLASS_PROBES = 5;
    public static final String TARGET_VERSION = "455.0.0.8.72";

    private R1CDiscoveryPolicy() { throw new AssertionError("No instances"); }

    public static boolean mayInventory(String version) {
        return TARGET_VERSION.equals(version);
    }

    public static Outcome classify(int matches) {
        if (matches < 0) return Outcome.FAILED;
        if (matches == 0) return Outcome.MISSING;
        if (matches == 1) return Outcome.UNIQUE_CANDIDATE;
        return Outcome.AMBIGUOUS;
    }

    /** Read-only discovery may *never* be promoted to a verified live-media identity. */
    public static boolean mayBindMediaInstance(boolean screenStable, Outcome marker,
            boolean hasMediaClass) {
        return false;
    }
}
