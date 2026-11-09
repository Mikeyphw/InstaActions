package com.mikeyphw.instaactions;

/** Pure, deterministic R1 capability policy. No Instagram implementation classes are linked. */
public final class R1ProbePolicy {
    public enum Kind { METHOD, CLASS }
    public enum Result { MISSING, UNIQUE, AMBIGUOUS, FAILED }
    public static final class Marker {
        public final String id;
        public final Kind kind;
        public final String needle;
        Marker(String id, Kind kind, String needle) {
            this.id = id;
            this.kind = kind;
            this.needle = needle;
        }
    }
    /** Publicly inspectable *discovery hints*, not verified Instagram API signatures. */
    public static final Marker[] MARKERS = {
        new Marker("reel_options", Kind.METHOD, "ClipsOrganicMediaItemViewMoreOptionsController"),
        new Marker("story_item_bound", Kind.METHOD, "ReelViewerFragment.onCurrentActiveItemBound"),
        new Marker("story_menu", Kind.METHOD, "[INTERNAL] Pause Playback"),
        new Marker("feed_video_player", Kind.CLASS, "IgFeedVideoPlayer: Persistent Visible Cover Image"),
        new Marker("playback_started", Kind.METHOD, "Playback started ")
    };
    public static final int MAX_LOGGED_DESCRIPTORS = 1;
    public static final boolean MUTATING_ACTIONS_ENABLED = false;
    private R1ProbePolicy() { throw new AssertionError("No instances"); }

    public static boolean supportedVersion(String version) {
        return "451.0.0.20.80".equals(version) || "453.0.0.0.56".equals(version);
    }
    public static Result classify(int matches) {
        if (matches < 0) return Result.FAILED;
        if (matches == 0) return Result.MISSING;
        if (matches == 1) return Result.UNIQUE;
        return Result.AMBIGUOUS;
    }
    public static String cacheKey(String version, long apkSize, long apkModified) {
        if (!supportedVersion(version) || apkSize <= 0 || apkModified <= 0) return null;
        return version + ":" + apkSize + ":" + apkModified;
    }
}
