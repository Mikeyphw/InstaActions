package com.mikeyphw.instaactions;

import java.util.Locale;
import java.util.Set;

/**
 * Pure, fail-closed UI surface inference. Source is resource entry names of visible
 * Instagram-owned views only; no text, URLs, captions, media IDs or account data.
 * A candidate is NEVER permission to run Instagram actions.
 */
public final class R1BContextPolicy {
    public enum Surface { REELS, STORY, POST, UNKNOWN }
    public enum Confidence { ANCHORED, CANDIDATE, NONE }
    public static final int MAX_VIEWS = 384;
    public static final int MAX_DEPTH = 18;
    public static final long MIN_SAMPLE_INTERVAL_MS = 800;

    public static final class Decision {
        public final Surface surface;
        public final Confidence confidence;
        public final int reelsScore;
        public final int storyScore;
        public final int postScore;
        public final boolean truncated;
        Decision(Surface surface, Confidence confidence, int reels, int story,
                int post, boolean truncated) {
            this.surface = surface;
            this.confidence = confidence;
            this.reelsScore = reels;
            this.storyScore = story;
            this.postScore = post;
            this.truncated = truncated;
        }
        boolean sameState(Decision other) {
            return other != null && surface == other.surface
                    && confidence == other.confidence && truncated == other.truncated;
        }
    }

    private R1BContextPolicy() { throw new AssertionError("No instances"); }

    public static Decision classify(Set<String> resourceNames, boolean truncated) {
        if (resourceNames == null || resourceNames.isEmpty() || truncated) {
            return new Decision(Surface.UNKNOWN, Confidence.NONE, 0, 0, 0, truncated);
        }
        int reels = 0, story = 0, post = 0;
        boolean reelAnchor = false, storyAnchor = false, postAnchor = false;
        for (String raw : resourceNames) {
            if (raw == null) continue;
            String id = raw.toLowerCase(Locale.ROOT);
            // Navigation and collapsed story tray are not an active viewer.
            if (id.contains("tab") || id.contains("tray") || id.contains("nav_")
                    || id.contains("thumbnail") || id.contains("badge")
                    || id.contains("shortcut")) continue;
            if (containsAny(id, "clips_viewer", "clips_player", "reels_viewer",
                    "reels_player", "reel_video_player")) {
                reels += 3; reelAnchor = true;
            } else if (containsAny(id, "clips_video", "reels_video", "clips_pager")) {
                reels++;
            }
            // Instagram calls its Stories viewer 'ReelViewer' internally. Do not
            // mistake reel_viewer* for the separate clips/Reels surface.
            if (containsAny(id, "story_viewer", "stories_viewer", "reel_viewer",
                    "story_pager")) {
                story += 3; storyAnchor = true;
            } else if (containsAny(id, "story_video", "story_progress", "stories_player")) {
                story++;
            }
            if (containsAny(id, "post_container", "feed_post", "feed_media",
                    "feed_item", "post_media")) {
                post += 3; postAnchor = true;
            } else if (containsAny(id, "post_actions", "feed_video", "feed_scroll")) {
                post++;
            }
        }
        int max = Math.max(reels, Math.max(story, post));
        if (max < 2) return new Decision(Surface.UNKNOWN, Confidence.NONE,
                reels, story, post, false);
        int winningCount = (reels == max ? 1 : 0) + (story == max ? 1 : 0)
                + (post == max ? 1 : 0);
        if (winningCount != 1) return new Decision(Surface.UNKNOWN, Confidence.NONE,
                reels, story, post, false);
        Surface candidate = reels == max ? Surface.REELS
                : story == max ? Surface.STORY : Surface.POST;
        int runnerUp = candidate == Surface.REELS ? Math.max(story, post)
                : candidate == Surface.STORY ? Math.max(reels, post) : Math.max(reels, story);
        if (max - runnerUp < 2) return new Decision(Surface.UNKNOWN, Confidence.NONE,
                reels, story, post, false);
        boolean anchored = candidate == Surface.REELS ? reelAnchor
                : candidate == Surface.STORY ? storyAnchor : postAnchor;
        Confidence confidence = anchored && max >= 3 ? Confidence.ANCHORED
                : Confidence.CANDIDATE;
        return new Decision(candidate, confidence, reels, story, post, false);
    }

    private static boolean containsAny(String target, String... needles) {
        for (String needle : needles) if (target.contains(needle)) return true;
        return false;
    }
}
