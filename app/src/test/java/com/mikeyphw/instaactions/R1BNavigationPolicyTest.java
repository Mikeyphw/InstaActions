package com.mikeyphw.instaactions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public class R1BNavigationPolicyTest {
    private static final boolean MAIN = true;
    private static Set<String> ids(String... names) {
        return new HashSet<>(Arrays.asList(names));
    }
    private static R1BContextPolicy.Decision media(String... names) {
        return R1BContextPolicy.classify(ids(names), false);
    }
    private static R1BNavigationPolicy.Screen nav(boolean selected,
            boolean otherSelected, boolean bar, R1BContextPolicy.Decision m) {
        return R1BNavigationPolicy.infer(MAIN, true, selected,
                otherSelected, bar, false, m);
    }

    @Test public void knownMainActivityNamesOnly() {
        assertTrue(R1BNavigationPolicy.isMainActivity(
                "com.instagram.mainactivity.InstagramMainActivity"));
        assertTrue(R1BNavigationPolicy.isMainActivity(
                "com.instagram.android.activity.MainTabActivity"));
        assertFalse(R1BNavigationPolicy.isMainActivity("android.app.Activity"));
    }
    @Test public void selectedHomeTabPlusBarIndicatesNavigationNotMedia() {
        assertEquals(R1BNavigationPolicy.Screen.HOME_FEED,
                nav(true, false, true, media("feed_tab", "main_feed_action_bar")));
        assertEquals(R1BContextPolicy.Surface.UNKNOWN,
                media("feed_tab", "main_feed_action_bar").surface);
    }
    @Test public void homeTabMerelyVisibleCannotEstablishHome() {
        assertEquals(R1BNavigationPolicy.Screen.UNKNOWN,
                nav(false, false, true, media("feed_tab", "main_feed_action_bar")));
        assertEquals(R1BNavigationPolicy.Screen.UNKNOWN,
                nav(true, false, false, media("feed_tab")));
    }
    @Test public void otherTabSelectionAndMainActivityGuardFailClosed() {
        assertEquals(R1BNavigationPolicy.Screen.UNKNOWN,
                nav(true, true, true, media("feed_tab", "main_feed_action_bar")));
        assertEquals(R1BNavigationPolicy.Screen.UNKNOWN,
                R1BNavigationPolicy.infer(false, true, true, false, true,
                        false, media("feed_tab", "main_feed_action_bar")));
    }
    @Test public void viewerAnchorsOverrideSelectedHomeTab() {
        assertEquals(R1BNavigationPolicy.Screen.REELS,
                nav(true, false, true, media("clips_viewer_root")));
        assertEquals(R1BNavigationPolicy.Screen.STORY,
                nav(true, false, true, media("story_viewer_root")));
    }
    @Test public void viewerCollisionsAndCandidateHintsAreUnknown() {
        assertEquals(R1BNavigationPolicy.Screen.UNKNOWN,
                nav(true, false, true, media("clips_viewer_root", "story_viewer_root")));
        assertEquals(R1BNavigationPolicy.Screen.UNKNOWN,
                nav(true, false, true, media("clips_video_surface", "main_feed_action_bar")));
    }
    @Test public void truncationVetoesAllNavigation() {
        assertEquals(R1BNavigationPolicy.Screen.UNKNOWN,
                R1BNavigationPolicy.infer(true, true, true, false, true,
                        true, media("feed_tab", "main_feed_action_bar")));
        assertEquals(R1BNavigationPolicy.Screen.UNKNOWN,
                R1BNavigationPolicy.infer(true, true, true, false, true,
                        false, R1BContextPolicy.classify(ids("clips_viewer_root"), true)));
    }
    @Test public void genericFeedContainerIsNotPostButCanBeHomeScreen() {
        R1BContextPolicy.Decision model = media("feed_media_container");
        assertEquals(R1BContextPolicy.Surface.UNKNOWN, model.surface);
        assertEquals(R1BNavigationPolicy.Screen.HOME_FEED,
                nav(true, false, true, model));
    }
    @Test public void feedRowProofRemainsSeparateFromHomeScreen() {
        R1BContextPolicy.Decision model = media("row_feed_button_like");
        assertEquals(R1BContextPolicy.Surface.POST, model.surface);
        assertEquals(R1BNavigationPolicy.Screen.HOME_FEED,
                nav(true, false, true, model));
    }
    @Test public void missingMediaEvidenceIsUnknown() {
        assertEquals(R1BNavigationPolicy.Screen.UNKNOWN,
                R1BNavigationPolicy.infer(true, true, true, false, true, false, null));
    }
}
