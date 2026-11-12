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

    @Test public void oneOwnerMatchCannotMaskBudgetRefusalInSameWindow() {
        R1CD05FabSession s = new R1CD05FabSession();
        s.start(true,100);
        s.observe(REELS, R1CD05ReferenceContinuity.Result.BASELINE,true,
                R1CD06MenuOwnership.Verdict.OWNER_DIRECT_MATCH, R1CD06VisiblePageBinding.Verdict.VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE,101);
        s.observe(REELS, SAME,true,
                R1CD06MenuOwnership.Verdict.OWNER_DECLARED_FIELD_LIMIT, R1CD06VisiblePageBinding.Verdict.MENU_UNQUALIFIED,102);
        assertEquals("OWNER_DECLARED_FIELD_LIMIT",s.currentOwnership());
        s.advance(103);
        for (int i=0;i<3;i++) {
            s.observe(REELS,SAME,true,R1CD06MenuOwnership.Verdict.OWNER_DIRECT_MATCH,
                    R1CD06VisiblePageBinding.Verdict.VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE,104+i*2);
            s.advance(105+i*2);
        }
        assertEquals(R1CD05FabSession.Step.DONE,s.step());
        assertTrue(s.report().contains("D06_OWNER_RESULT=UNVERIFIED_OR_INCONCLUSIVE"));
        assertTrue(s.report().contains("A1_D06_OWNER_DECLARED_FIELD_LIMIT=1"));
    }

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
        s.observe(REELS, R1CD05ReferenceContinuity.Result.BASELINE, true, R1CD06MenuOwnership.Verdict.OWNER_DIRECT_MATCH, R1CD06VisiblePageBinding.Verdict.VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE, 1002);
        assertEquals(R1CD05FabSession.Step.A2, s.advance(1050));
        s.observe(REELS, SAME, true, R1CD06MenuOwnership.Verdict.OWNER_DIRECT_MATCH, R1CD06VisiblePageBinding.Verdict.VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE, 1060);
        assertEquals(R1CD05FabSession.Step.B1, s.advance(1100));
        s.observe(REELS, DIFFERENT, true, R1CD06MenuOwnership.Verdict.OWNER_DIRECT_MATCH, R1CD06VisiblePageBinding.Verdict.VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE, 1110);
        assertEquals(R1CD05FabSession.Step.B2, s.advance(1200));
        s.observe(REELS, SAME, true, R1CD06MenuOwnership.Verdict.OWNER_DIRECT_MATCH, R1CD06VisiblePageBinding.Verdict.VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE, 1210);
        assertEquals(R1CD05FabSession.Step.DONE, s.advance(1300));
        String report = s.report();
        assertTrue(report.contains("RESULT=REFERENCE_COUNTS_ONLY"));
        assertTrue(report.contains("A1_BASELINE=1"));
        assertTrue(report.contains("A2_SAME_REFERENCE=1"));
        assertTrue(report.contains("B1_DIFFERENT_REFERENCE=1"));
        assertTrue(report.contains("B2_SAME_REFERENCE=1"));
        assertTrue(report.contains("MEDIA_IDENTITY=NOT_VERIFIED"));
        assertTrue(report.contains("D06_VISIBLE_BINDING=NOT_VERIFIED"));
        assertTrue(report.contains("A1_D06_OWNER_DIRECT_MATCH=1"));
        assertTrue(report.contains("D06_OWNER_RESULT=FOUR_WINDOWS_DIRECT_OWNER_MATCH_ONLY"));
        assertFalse(report.contains("CAPTION="));
    }

    @Test public void expiredWindowDoesNotQualifyOrAcceptLateCallbacks() {
        R1CD05FabSession s = new R1CD05FabSession();
        s.start(true, 500);
        s.observe(REELS, SAME, true, R1CD06MenuOwnership.Verdict.OWNER_DIRECT_MATCH, R1CD06VisiblePageBinding.Verdict.VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE, 500 + R1CD05FabSession.MAX_WINDOW_MS + 1);
        assertEquals(0, s.total());
        s.advance(500 + R1CD05FabSession.MAX_WINDOW_MS + 1);
        s.observe(REELS, SAME, true, R1CD06MenuOwnership.Verdict.OWNER_DIRECT_MATCH, R1CD06VisiblePageBinding.Verdict.VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE, 60500 + 2);
        s.advance(61000);
        s.advance(61500);
        s.advance(62000);
        assertTrue(s.report().contains("INCONCLUSIVE_MISSING_OR_EXPIRED_WINDOW"));
    }

    @Test public void unknownScreenNotCountedAsStableReels() {
        R1CD05FabSession s = new R1CD05FabSession();
        s.start(true, 200);
        s.observe(R1BNavigationPolicy.Screen.UNKNOWN,
                R1CD05ReferenceContinuity.Result.SKIPPED_UNSTABLE, true, R1CD06MenuOwnership.Verdict.OWNER_DIRECT_MATCH, R1CD06VisiblePageBinding.Verdict.VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE, 210);
        assertEquals(0, s.currentCount());
        assertTrue(s.report().contains("A1_UNKNOWN_SCREEN=1"));
        s.advance(220);
        assertTrue(s.report().contains("WINDOW_A1_VALID=false"));
    }

    @Test public void activityExitFailsClosedAndNewStartClearsCounts() {
        R1CD05FabSession s = new R1CD05FabSession();
        s.start(true, 100);
        s.observe(REELS, SAME, true, R1CD06MenuOwnership.Verdict.OWNER_DIRECT_MATCH, R1CD06VisiblePageBinding.Verdict.VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE, 101);
        s.cancel(true);
        s.observe(REELS, SAME, true, R1CD06MenuOwnership.Verdict.OWNER_DIRECT_MATCH, R1CD06VisiblePageBinding.Verdict.VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE, 102);
        assertEquals(1, s.total());
        assertEquals(R1CD05FabSession.Step.CANCELLED, s.step());
        assertTrue(s.start(true, 200));
        assertEquals(0, s.total());
        assertTrue(s.report().contains("A1_SAME_REFERENCE=0"));
    }

    @Test public void nonReelTypedCallbackDoesNotQualify() {
        R1CD05FabSession s = new R1CD05FabSession();
        s.start(true, 100);
        s.observe(R1BNavigationPolicy.Screen.STORY, SAME, true, R1CD06MenuOwnership.Verdict.OWNER_DIRECT_MATCH, R1CD06VisiblePageBinding.Verdict.VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE, 110);
        s.observe(REELS, R1CD05ReferenceContinuity.Result.SKIPPED_WRONG_TYPE, false, R1CD06MenuOwnership.Verdict.WRONG_MEDIA_TYPE, R1CD06VisiblePageBinding.Verdict.MENU_UNQUALIFIED, 115);
        s.advance(120);
        assertTrue(s.report().contains("WINDOW_A1_VALID=false"));
    }

    @Test public void visiblePageMixedMatchAndFailureIsInconclusive() {
        R1CD05FabSession s = new R1CD05FabSession();
        s.start(true,100);
        s.observe(REELS, R1CD05ReferenceContinuity.Result.BASELINE, true,
                R1CD06MenuOwnership.Verdict.CONTEXT_DIRECT_MATCH,
                R1CD06VisiblePageBinding.Verdict.VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE,101);
        s.observe(REELS,SAME,true,R1CD06MenuOwnership.Verdict.CONTEXT_DIRECT_MATCH,
                R1CD06VisiblePageBinding.Verdict.AMBIGUOUS_CURRENT_PAGE,102);
        assertEquals("AMBIGUOUS_CURRENT_PAGE",s.currentVisiblePage());
        s.advance(103);
        for(int i=0;i<3;i++){
            s.observe(REELS,SAME,true,R1CD06MenuOwnership.Verdict.CONTEXT_DIRECT_MATCH,
                    R1CD06VisiblePageBinding.Verdict.VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE,104+i*2);
            s.advance(105+i*2);
        }
        assertEquals(R1CD05FabSession.Step.DONE,s.step());
        assertTrue(s.report().contains("D06_VISIBLE_PAGE_RESULT=UNVERIFIED_OR_INCONCLUSIVE"));
        assertTrue(s.report().contains("A1_VP_AMBIGUOUS_CURRENT_PAGE=1"));
        assertTrue(s.report().contains("D06_ACTIVE_VIEW_BINDING=NOT_QUALIFIED"));
    }

    @Test public void visiblePageFourCandidateWindowsRemainUnqualified() {
        R1CD05FabSession s = new R1CD05FabSession();
        s.start(true,100);
        for(int i=0;i<4;i++){
            s.observe(REELS,R1CD05ReferenceContinuity.Result.BASELINE,true,
                    R1CD06MenuOwnership.Verdict.CONTEXT_DIRECT_MATCH,
                    R1CD06VisiblePageBinding.Verdict.VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE,101+i*2);
            s.advance(102+i*2);
        }
        assertTrue(s.report().contains("D06_VISIBLE_PAGE_RESULT=FOUR_WINDOWS_PAGE_MATCH_CANDIDATES_ONLY"));
        assertTrue(s.report().contains("MEDIA_IDENTITY=NOT_VERIFIED"));
        assertTrue(s.report().contains("D06_ACTIVE_VIEW_BINDING=NOT_QUALIFIED"));
    }
    @Test public void structuralPreflightNeverQualifiesMediaIdentity() {
        R1CD05FabSession s=new R1CD05FabSession();
        s.start(true,100);
        R1CD06ViewPreflight.Snapshot sample=new R1CD06ViewPreflight.Snapshot(
                R1CD06ViewPreflight.Status.NO_VIEWPAGER2,42,20,0,0,0,0,0,0,7);
        s.notePreflight(sample,150);
        assertEquals("NO_VIEWPAGER2",s.currentPreflight());
        assertTrue(s.report().contains("A1_PF_NO_VIEWPAGER2=1"));
        assertTrue(s.report().contains("A1_PF_MAX_VISITED=42"));
        assertTrue(s.report().contains("NEXT_DIAGNOSTIC_TARGET=NO_MENU_CALLBACKS_CAPTURED"));
        assertTrue(s.report().contains("MEDIA_IDENTITY=NOT_VERIFIED"));
    }
    @Test public void repeatedStructuralScansAreCappedAndReset() {
        R1CD05FabSession s=new R1CD05FabSession();
        s.start(true,100);
        R1CD06ViewPreflight.Snapshot sample=new R1CD06ViewPreflight.Snapshot(
                R1CD06ViewPreflight.Status.VIEWER_STRUCTURE_FOUND,10,7,1,1,1,1,1,1,2);
        for(int i=0;i<40;i++)s.notePreflight(sample,120+i);
        assertTrue(s.report().contains("A1_PF_SAMPLES=12"));
        assertFalse(s.needsPreflight());
        s.cancel(false);
        s.start(true,400);
        assertTrue(s.needsPreflight());
        assertTrue(s.report().contains("A1_PF_SAMPLES=0"));
    }
    @Test public void preflightDistinguishesOffMainCallbackFromAbsentPager() {
        R1CD05FabSession s=new R1CD05FabSession();
        s.start(true,100);
        s.notePreflight(new R1CD06ViewPreflight.Snapshot(
                R1CD06ViewPreflight.Status.VIEWER_STRUCTURE_FOUND,50,45,1,1,1,1,1,1,4),150);
        s.observe(R1BNavigationPolicy.Screen.REELS,
                R1CD05ReferenceContinuity.Result.BASELINE,true,
                R1CD06MenuOwnership.Verdict.CONTEXT_DIRECT_MATCH,
                R1CD06VisiblePageBinding.Verdict.OFF_MAIN_THREAD,160);
        assertEquals("PAGER_PRESENT_BUT_CALLBACK_OFF_MAIN",s.diagnosticSummary());
    }
    @Test public void preflightDistinguishesMissingDirectPageModel() {
        R1CD05FabSession s=new R1CD05FabSession();
        s.start(true,100);
        s.notePreflight(new R1CD06ViewPreflight.Snapshot(
                R1CD06ViewPreflight.Status.VIEWER_STRUCTURE_FOUND,70,60,1,1,1,1,1,1,5),150);
        s.observe(R1BNavigationPolicy.Screen.REELS,
                R1CD05ReferenceContinuity.Result.BASELINE,true,
                R1CD06MenuOwnership.Verdict.CONTEXT_DIRECT_MATCH,
                R1CD06VisiblePageBinding.Verdict.NO_DIRECT_MEDIA,170);
        assertEquals("PAGER_FOUND_BUT_PAGE_HAS_NO_DIRECT_MEDIA",s.diagnosticSummary());
    }
}
