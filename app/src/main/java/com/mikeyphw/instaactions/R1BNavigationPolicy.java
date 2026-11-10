package com.mikeyphw.instaactions;

/**
 * Read-only foreground navigation evidence, independent of media identity.
 * A visible Feed tab is NOT sufficient: it must be explicitly selected, the
 * main Feed bar must be visible, the host Activity must be Instagram Main, and
 * no Reels/Stories evidence can be competing. These are still preview hints.
 */
public final class R1BNavigationPolicy {
    public enum Screen { HOME_FEED, REELS, STORY, UNKNOWN }

    private R1BNavigationPolicy() { throw new AssertionError("No instances"); }

    public static boolean isMainActivity(String className) {
        return "com.instagram.mainactivity.InstagramMainActivity".equals(className)
                || "com.instagram.android.activity.MainTabActivity".equals(className);
    }

    public static Screen infer(boolean mainActivity, boolean feedTabVisible,
            boolean feedTabSelected, boolean otherTabSelected,
            boolean feedBarVisible, boolean truncated,
            R1BContextPolicy.Decision media) {
        if (truncated || media == null || media.truncated) return Screen.UNKNOWN;
        // Full-screen viewer anchors outweigh a Home tab retained in the
        // background. Unanchored or contradictory viewer hints fail closed.
        if (media.surface == R1BContextPolicy.Surface.REELS
                && media.confidence == R1BContextPolicy.Confidence.ANCHORED) {
            return Screen.REELS;
        }
        if (media.surface == R1BContextPolicy.Surface.STORY
                && media.confidence == R1BContextPolicy.Confidence.ANCHORED) {
            return Screen.STORY;
        }
        if (media.reelsScore != 0 || media.storyScore != 0) return Screen.UNKNOWN;
        if (mainActivity && feedTabVisible && feedTabSelected && !otherTabSelected
                && feedBarVisible) {
            return Screen.HOME_FEED;
        }
        return Screen.UNKNOWN;
    }
}
