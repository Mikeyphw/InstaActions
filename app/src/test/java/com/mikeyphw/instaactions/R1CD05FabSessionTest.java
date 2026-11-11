package com.mikeyphw.instaactions;

import org.junit.Test;
import static org.junit.Assert.*;

/** D05 UI capture counters and lifecycle are independent of Instagram objects. */
public class R1CD05FabSessionTest {
    private static final R1BNavigationPolicy.Screen REELS = R1BNavigationPolicy.Screen.REELS;
    private static final R1CD05ReferenceContinuity.Result SAME =
            R1CD05ReferenceContinuity.Result.SAME_REFERENCE;
    private static final R1CD05ReferenceContinuity.Result DIFFERENT =
            R1CD05ReferenceContinuity.Result.DIFFERENT_REFERENCE;

    @Test public void exactHookGatePreventsStart() {
        R1CD05FabSession s = new R1CD05FabSession();
        assertFalse(s.start(false, 100));
        assertEquals(R1CD05FabSession.Step.IDLE, s.step());
        assertTrue(s.start(true, 100));
        assertFalse(s.start(true, 101));
    }

    @Test public void fourUserWindowsRemainReferenceOnly() {
        R1CD05FabSession s = new R1CD05FabSession();
        assertTrue(s.start(true, 1000));
        s.observe(REELS, R1CD05ReferenceContinuity.Result.BASELINE, true, 1002);
        assertEquals(R1CD05FabSession.Step.A2, s.advance(1050));
        s.observe(REELS, SAME, true, 1060);
        assertEquals(R1CD05FabSession.Step.B1, s.advance(1100));
        s.observe(REELS, DIFFERENT, true, 1110);
        assertEquals(R1CD05FabSession.Step.B2, s.advance(1200));
        s.observe(REELS, SAME, true, 1210);
        assertEquals(R1CD05FabSession.Step.DONE, s.advance(1300));
        String report = s.report();
        assertTrue(report.contains("RESULT=REFERENCE_COUNTS_ONLY"));
        assertTrue(report.contains("A1_BASELINE=1"));
        assertTrue(report.contains("A2_SAME_REFERENCE=1"));
        assertTrue(report.contains("B1_DIFFERENT_REFERENCE=1"));
        assertTrue(report.contains("B2_SAME_REFERENCE=1"));
        assertTrue(report.contains("MEDIA_IDENTITY=NOT_VERIFIED"));
        assertFalse(report.contains("CAPTION="));
    }

    @Test public void expiredWindowDoesNotQualifyOrAcceptLateCallbacks() {
        R1CD05FabSession s = new R1CD05FabSession();
        s.start(true, 500);
        s.observe(REELS, SAME, true, 500 + R1CD05FabSession.MAX_WINDOW_MS + 1);
        assertEquals(0, s.total());
        s.advance(500 + R1CD05FabSession.MAX_WINDOW_MS + 1);
        s.observe(REELS, SAME, true, 60500 + 2);
        s.advance(61000);
        s.advance(61500);
        s.advance(62000);
        assertTrue(s.report().contains("INCONCLUSIVE_MISSING_OR_EXPIRED_WINDOW"));
    }

    @Test public void unknownScreenNotCountedAsStableReels() {
        R1CD05FabSession s = new R1CD05FabSession();
        s.start(true, 200);
        s.observe(R1BNavigationPolicy.Screen.UNKNOWN,
                R1CD05ReferenceContinuity.Result.SKIPPED_UNSTABLE, true, 210);
        assertEquals(0, s.currentCount());
        assertTrue(s.report().contains("A1_UNKNOWN_SCREEN=1"));
        s.advance(220);
        assertTrue(s.report().contains("WINDOW_A1_VALID=false"));
    }

    @Test public void activityExitFailsClosedAndNewStartClearsCounts() {
        R1CD05FabSession s = new R1CD05FabSession();
        s.start(true, 100);
        s.observe(REELS, SAME, true, 101);
        s.cancel(true);
        s.observe(REELS, SAME, true, 102);
        assertEquals(1, s.total());
        assertEquals(R1CD05FabSession.Step.CANCELLED, s.step());
        assertTrue(s.start(true, 200));
        assertEquals(0, s.total());
        assertTrue(s.report().contains("A1_SAME_REFERENCE=0"));
    }

    @Test public void nonReelTypedCallbackDoesNotQualify() {
        R1CD05FabSession s = new R1CD05FabSession();
        s.start(true, 100);
        s.observe(R1BNavigationPolicy.Screen.STORY, SAME, true, 110);
        s.observe(REELS, R1CD05ReferenceContinuity.Result.SKIPPED_WRONG_TYPE, false, 115);
        s.advance(120);
        assertTrue(s.report().contains("WINDOW_A1_VALID=false"));
    }
}
