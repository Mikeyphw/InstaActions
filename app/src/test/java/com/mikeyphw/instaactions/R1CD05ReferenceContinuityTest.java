package com.mikeyphw.instaactions;

import org.junit.Test;
import static org.junit.Assert.*;

public class R1CD05ReferenceContinuityTest {
    @Test public void repeatedObjectThenChangedObject() {
        R1CD05ReferenceContinuity c = new R1CD05ReferenceContinuity();
        Object first = new Object(), second = new Object();
        assertEquals(R1CD05ReferenceContinuity.Result.BASELINE,
                c.accept(first, R1BNavigationPolicy.Screen.REELS, true));
        assertEquals(R1CD05ReferenceContinuity.Result.SAME_REFERENCE,
                c.accept(first, R1BNavigationPolicy.Screen.REELS, true));
        assertEquals(R1CD05ReferenceContinuity.Result.DIFFERENT_REFERENCE,
                c.accept(second, R1BNavigationPolicy.Screen.REELS, true));
        assertEquals(R1CD05ReferenceContinuity.Result.SAME_REFERENCE,
                c.accept(second, R1BNavigationPolicy.Screen.REELS, true));
    }

    @Test public void unknownAndStoryInvalidateImmediately() {
        R1CD05ReferenceContinuity c = new R1CD05ReferenceContinuity();
        Object m = new Object();
        c.accept(m, R1BNavigationPolicy.Screen.REELS, true);
        assertEquals(R1CD05ReferenceContinuity.Result.SKIPPED_UNSTABLE,
                c.accept(m, R1BNavigationPolicy.Screen.UNKNOWN, true));
        assertEquals(R1CD05ReferenceContinuity.Result.BASELINE,
                c.accept(m, R1BNavigationPolicy.Screen.REELS, true));
        assertEquals(R1CD05ReferenceContinuity.Result.SKIPPED_UNSTABLE,
                c.accept(m, R1BNavigationPolicy.Screen.STORY, true));
        assertEquals(R1CD05ReferenceContinuity.Result.BASELINE,
                c.accept(m, R1BNavigationPolicy.Screen.REELS, true));
    }

    @Test public void wrongTypeAndExplicitPauseInvalidate() {
        R1CD05ReferenceContinuity c = new R1CD05ReferenceContinuity();
        Object m = new Object();
        c.accept(m, R1BNavigationPolicy.Screen.REELS, true);
        assertEquals(R1CD05ReferenceContinuity.Result.SKIPPED_WRONG_TYPE,
                c.accept(new Object(), R1BNavigationPolicy.Screen.REELS, false));
        assertEquals(R1CD05ReferenceContinuity.Result.BASELINE,
                c.accept(m, R1BNavigationPolicy.Screen.REELS, true));
        c.invalidate();
        assertEquals(R1CD05ReferenceContinuity.Result.BASELINE,
                c.accept(m, R1BNavigationPolicy.Screen.REELS, true));
    }
}
