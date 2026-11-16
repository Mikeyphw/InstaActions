package com.mikeyphw.instaactions;

import java.lang.ref.WeakReference;

/**
 * D05: diagnostic-only continuity of Media object *references* between
 * consecutive Reels menu callbacks. Does not inspect, hash, or log values,
 * identityHashCode, toString, fields, Media IDs, URLs or accounts.
 * A reference comparison is NOT proof of which Reel is visible.
 */
final class R1CD05ReferenceContinuity {
    enum Result {
        SKIPPED_UNSTABLE, SKIPPED_WRONG_TYPE,
        BASELINE, BASELINE_COLLECTED, SAME_REFERENCE, DIFFERENT_REFERENCE
    }

    private WeakReference<Object> previous = new WeakReference<>(null);
    private boolean baselineRecorded;

    synchronized Result accept(Object candidate, R1BNavigationPolicy.Screen screen,
            boolean mediaTyped) {
        if (screen != R1BNavigationPolicy.Screen.REELS) {
            invalidate();
            return Result.SKIPPED_UNSTABLE;
        }
        if (!mediaTyped || candidate == null) {
            invalidate();
            return Result.SKIPPED_WRONG_TYPE;
        }
        Object prior = previous.get();
        if (!baselineRecorded || prior == null) {
            Result out = baselineRecorded ? Result.BASELINE_COLLECTED : Result.BASELINE;
            previous = new WeakReference<>(candidate);
            baselineRecorded = true;
            return out;
        }
        Result out = prior == candidate ? Result.SAME_REFERENCE : Result.DIFFERENT_REFERENCE;
        previous = new WeakReference<>(candidate);
        return out;
    }

    synchronized void invalidate() {
        previous.clear();
        previous = new WeakReference<>(null);
        baselineRecorded = false;
    }
}
