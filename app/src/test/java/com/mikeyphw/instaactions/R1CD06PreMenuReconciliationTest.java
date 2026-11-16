package com.mikeyphw.instaactions;
import org.junit.Test;
import static org.junit.Assert.*;
public class R1CD06PreMenuReconciliationTest {
    @Test public void noActiveStageDoesNotPretendToHaveEvidence(){
        R1CD06PreMenuReconciliation.clear();
        assertEquals(R1CD06PreMenuReconciliation.Verdict.NOT_QUALIFIED,
            R1CD06PreMenuReconciliation.compare(null,new Object(),Object.class,
                R1BNavigationPolicy.Screen.UNKNOWN,
                R1CD06MenuOwnership.Verdict.CONTEXT_DIRECT_MATCH,
                R1CD05FabSession.Step.IDLE));
    }
    @Test public void missingSnapshotIsExplicitNotPagerAbsent(){
        R1CD06PreMenuReconciliation.clear();
        assertEquals(R1CD06PreMenuReconciliation.Verdict.SNAPSHOT_MISSING,
            R1CD06PreMenuReconciliation.compare(null,new Object(),Object.class,
                R1BNavigationPolicy.Screen.REELS,
                R1CD06MenuOwnership.Verdict.CONTEXT_DIRECT_MATCH,
                R1CD05FabSession.Step.A1));
    }
    @Test public void stageCountersCannotQualifyMediaIdentity(){
        R1CD05FabSession s=new R1CD05FabSession();
        s.start(true,100);
        s.observe(R1BNavigationPolicy.Screen.REELS,R1CD05ReferenceContinuity.Result.BASELINE,
            true,R1CD06MenuOwnership.Verdict.CONTEXT_DIRECT_MATCH,
            R1CD06VisiblePageBinding.Verdict.NO_VERTICAL_PAGER,
            R1CD06PreMenuReconciliation.Verdict.NO_DIRECT_MEDIA,110);
        assertEquals("NO_DIRECT_MEDIA",s.currentPreMenu());
        assertEquals("MENU_HIDES_PAGER_PREMENU_PAGE_HAS_NO_DIRECT_MEDIA",s.diagnosticSummary());
        assertTrue(s.report().contains("A1_PREMENU_NO_DIRECT_MEDIA=1"));
        assertTrue(s.report().contains("MEDIA_IDENTITY=NOT_VERIFIED"));
    }
    @Test public void mediaCandidateStillCannotQualifyBinding(){
        R1CD05FabSession s=new R1CD05FabSession();
        s.start(true,100);
        s.notePreflight(new R1CD06ViewPreflight.Snapshot(
          R1CD06ViewPreflight.Status.VIEWER_STRUCTURE_FOUND,72,61,2,1,1,1,1,1,4),105);
        s.observe(R1BNavigationPolicy.Screen.REELS,R1CD05ReferenceContinuity.Result.BASELINE,
            true,R1CD06MenuOwnership.Verdict.CONTEXT_DIRECT_MATCH,
            R1CD06VisiblePageBinding.Verdict.NO_VERTICAL_PAGER,
            R1CD06PreMenuReconciliation.Verdict.PREMENU_MEDIA_MATCH_CANDIDATE,110);
        assertEquals("MENU_HIDES_PAGER_PREMENU_PAGE_MEDIA_CANDIDATE_ONLY",s.diagnosticSummary());
        assertTrue(s.report().contains("D06_ACTIVE_VIEW_BINDING=NOT_QUALIFIED"));
        assertTrue(s.report().contains("VISIBLE_MEDIA_IDENTITY=NOT_VERIFIED"));
    }
}
