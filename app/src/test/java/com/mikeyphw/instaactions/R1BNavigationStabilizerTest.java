package com.mikeyphw.instaactions;

import org.junit.Test;
import static org.junit.Assert.*;

public class R1BNavigationStabilizerTest {
    private static final R1BNavigationPolicy.Screen HOME = R1BNavigationPolicy.Screen.HOME_FEED;
    private static final R1BNavigationPolicy.Screen REELS = R1BNavigationPolicy.Screen.REELS;
    private static final R1BNavigationPolicy.Screen STORY = R1BNavigationPolicy.Screen.STORY;
    private static final R1BNavigationPolicy.Screen UNKNOWN = R1BNavigationPolicy.Screen.UNKNOWN;

    @Test public void oneSampleNeverQualifiesHome() {
        R1BNavigationStabilizer s = new R1BNavigationStabilizer();
        assertEquals(UNKNOWN, s.accept(HOME, 100));
        assertEquals(UNKNOWN, s.accept(HOME, 700));
        assertEquals(HOME, s.accept(HOME, 1300));
    }
    @Test public void viewerAndHomeTransitionsDropPreviousImmediately() {
        R1BNavigationStabilizer s = new R1BNavigationStabilizer();
        assertEquals(UNKNOWN, s.accept(HOME, 0));
        assertEquals(HOME, s.accept(HOME, 1600));
        assertEquals(UNKNOWN, s.accept(REELS, 1700));
        assertEquals(REELS, s.accept(REELS, 3400));
        assertEquals(UNKNOWN, s.accept(STORY, 3500));
        assertEquals(STORY, s.accept(STORY, 5200));
        assertEquals(UNKNOWN, s.accept(HOME, 6000));
        assertEquals(HOME, s.accept(HOME, 7600));
    }
    @Test public void unknownResetsNoStaleIdentity() {
        R1BNavigationStabilizer s = new R1BNavigationStabilizer();
        s.accept(HOME, 0);
        s.accept(HOME, 1200);
        assertEquals(UNKNOWN, s.accept(UNKNOWN, 1400));
        assertEquals(UNKNOWN, s.accept(HOME, 1500));
        assertEquals(HOME, s.accept(HOME, 3000));
    }
    @Test public void cleanupAndClockRollbackFailClosed() {
        R1BNavigationStabilizer s = new R1BNavigationStabilizer();
        s.accept(REELS, 100);
        s.accept(REELS, 1800);
        s.reset();
        assertEquals(UNKNOWN, s.accept(REELS, 1900));
        assertEquals(UNKNOWN, s.accept(REELS, 1800));
    }
}
