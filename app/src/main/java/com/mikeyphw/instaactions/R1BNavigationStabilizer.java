package com.mikeyphw.instaactions;

/**
 * Lifecycle-local confirmation of read-only navigation observations.
 * Transitions instantly invalidate the previous state; never carry a stale
 * Home/Reels/Story identity across an UNKNOWN or changing UI.
 */
final class R1BNavigationStabilizer {
    static final long MIN_STABLE_MS = 1000L;
    private R1BNavigationPolicy.Screen pending = R1BNavigationPolicy.Screen.UNKNOWN;
    private R1BNavigationPolicy.Screen confirmed = R1BNavigationPolicy.Screen.UNKNOWN;
    private long sinceMs;
    private int matches;

    R1BNavigationPolicy.Screen accept(R1BNavigationPolicy.Screen candidate, long uptimeMs) {
        if (candidate == null || candidate == R1BNavigationPolicy.Screen.UNKNOWN) {
            reset();
            return confirmed;
        }
        if (candidate != pending || uptimeMs < sinceMs) {
            pending = candidate;
            sinceMs = uptimeMs;
            matches = 1;
            confirmed = R1BNavigationPolicy.Screen.UNKNOWN;
            return confirmed;
        }
        if (matches < Integer.MAX_VALUE) matches++;
        if (matches >= 2 && uptimeMs - sinceMs >= MIN_STABLE_MS) {
            confirmed = candidate;
        }
        return confirmed;
    }

    void reset() {
        pending = R1BNavigationPolicy.Screen.UNKNOWN;
        confirmed = R1BNavigationPolicy.Screen.UNKNOWN;
        matches = 0;
        sinceMs = 0L;
    }
}
