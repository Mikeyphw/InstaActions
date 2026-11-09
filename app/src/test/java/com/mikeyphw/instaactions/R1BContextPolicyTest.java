package com.mikeyphw.instaactions;

import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public class R1BContextPolicyTest {
    private static Set<String> ids(String... values) {
        return new HashSet<>(Arrays.asList(values));
    }
    @Test public void reelsNeedsVisibleViewerEvidence() {
        R1BContextPolicy.Decision found = R1BContextPolicy.classify(
                ids("clips_viewer_layout", "clips_video_surface"), false);
        assertEquals(R1BContextPolicy.Surface.REELS, found.surface);
        assertEquals(R1BContextPolicy.Confidence.ANCHORED, found.confidence);
        assertEquals(R1BContextPolicy.Surface.UNKNOWN,
                R1BContextPolicy.classify(ids("reels_tab_button"), false).surface);
    }
    @Test public void reelViewerNamingMapsToStoriesNotClips() {
        R1BContextPolicy.Decision found = R1BContextPolicy.classify(
                ids("reel_viewer_root", "story_progress_view"), false);
        assertEquals(R1BContextPolicy.Surface.STORY, found.surface);
        assertEquals(R1BContextPolicy.Confidence.ANCHORED, found.confidence);
    }
    @Test public void feedPostAndAmbiguityAreConservative() {
        // A generic Feed/Litho container is not a specific media row.
        assertEquals(R1BContextPolicy.Surface.UNKNOWN,
                R1BContextPolicy.classify(ids("feed_media_container"), false).surface);
        assertEquals(R1BContextPolicy.Surface.POST,
                R1BContextPolicy.classify(ids("row_feed_button_like"), false).surface);
        assertEquals(R1BContextPolicy.Surface.UNKNOWN,
                R1BContextPolicy.classify(
                        ids("clips_viewer_layout", "story_viewer_root"), false).surface);
    }
    @Test public void noEvidenceOrIncompleteScanIsUnknown() {
        assertEquals(R1BContextPolicy.Surface.UNKNOWN,
                R1BContextPolicy.classify(Collections.emptySet(), false).surface);
        assertEquals(R1BContextPolicy.Surface.UNKNOWN,
                R1BContextPolicy.classify(ids("clips_viewer_layout"), true).surface);
        assertEquals(R1BContextPolicy.Surface.UNKNOWN,
                R1BContextPolicy.classify(ids("story_tray_icon", "story_tab"), false).surface);
    }
    @Test public void noStateChangeForEqualState() {
        R1BContextPolicy.Decision first = R1BContextPolicy.classify(
                ids("row_feed_button_like"), false);
        R1BContextPolicy.Decision same = R1BContextPolicy.classify(
                ids("row_feed_button_like", "post_actions"), false);
        assertTrue(first.sameState(same));
        assertFalse(first.sameState(R1BContextPolicy.classify(
                ids("clips_viewer_layout"), false)));
    }
}
