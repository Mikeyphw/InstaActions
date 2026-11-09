package com.mikeyphw.instaactions;

import org.junit.Test;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

/** Source-backed view resource anchors; actual Instagram runtime is a separate gate. */
public class R1BHF04FeedAnchorsTest {
    private static Set<String> ids(String... names) {
        return new HashSet<>(Arrays.asList(names));
    }

    @Test public void classicFeedLikeButtonQualifiesPost() {
        R1BContextPolicy.Decision result = R1BContextPolicy.classify(
                ids("row_feed_button_like"), false);
        assertEquals(R1BContextPolicy.Surface.POST, result.surface);
        assertEquals(R1BContextPolicy.Confidence.ANCHORED, result.confidence);
    }

    @Test public void supportingFeedResourcesRaiseScoreOnlyWithLikeButton() {
        R1BContextPolicy.Decision result = R1BContextPolicy.classify(
                ids("row_feed_button_like", "row_feed_button_save", "media_group"), false);
        assertEquals(R1BContextPolicy.Surface.POST, result.surface);
        assertEquals(7, result.postScore);
        assertEquals(R1BContextPolicy.Surface.UNKNOWN, R1BContextPolicy.classify(
                ids("row_feed_button_save", "media_group", "video_container"), false).surface);
    }

    @Test public void reelAndStoryViewerEvidenceOutranksBackgroundFeedRow() {
        assertEquals(R1BContextPolicy.Surface.REELS, R1BContextPolicy.classify(
                ids("row_feed_button_like", "clips_viewer_layout"), false).surface);
        assertEquals(R1BContextPolicy.Surface.STORY, R1BContextPolicy.classify(
                ids("row_feed_button_like", "story_viewer_root"), false).surface);
    }

    @Test public void incompleteOrNavigationOnlyEvidenceFailsClosed() {
        assertEquals(R1BContextPolicy.Surface.UNKNOWN, R1BContextPolicy.classify(
                ids("row_feed_button_like"), true).surface);
        assertEquals(R1BContextPolicy.Surface.UNKNOWN, R1BContextPolicy.classify(
                ids("feed_tab", "reels_tray_container"), false).surface);
    }
}
