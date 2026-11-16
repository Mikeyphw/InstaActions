package com.mikeyphw.instaactions;
import org.junit.Test;
import static org.junit.Assert.*;
/** SF01 pinned upstream signal classification: no Android/Instagram runtime necessary. */
public class R1CD06UpstreamConformanceTest {
  @Test public void exactMenuFingerprintRequiresOneDescriptorMatch(){
    assertEquals(R1CD06UpstreamConformance.Menu.MATCH_EXACT_D04,
        R1CD06UpstreamConformance.classifyMenu(1,true));
    assertEquals(R1CD06UpstreamConformance.Menu.DIFFERENT_METHOD,
        R1CD06UpstreamConformance.classifyMenu(1,false));
  }
  @Test public void missingOrAmbiguousMenuIsNotSuccess(){
    assertEquals(R1CD06UpstreamConformance.Menu.NO_MATCH,
        R1CD06UpstreamConformance.classifyMenu(0,true));
    assertEquals(R1CD06UpstreamConformance.Menu.AMBIGUOUS,
        R1CD06UpstreamConformance.classifyMenu(2,true));
    assertEquals(R1CD06UpstreamConformance.Menu.AMBIGUOUS,
        R1CD06UpstreamConformance.classifyMenu(33,true));
  }
  @Test public void pagerMustBeUniqueAndHaveExactlyOneTypedField(){
    assertEquals(R1CD06UpstreamConformance.Pager.UNIQUE_WITH_VIEWPAGER2_FIELD,
        R1CD06UpstreamConformance.classifyPager(1,1));
    assertEquals(R1CD06UpstreamConformance.Pager.UNIQUE_NO_VIEWPAGER2_FIELD,
        R1CD06UpstreamConformance.classifyPager(1,0));
    assertEquals(R1CD06UpstreamConformance.Pager.AMBIGUOUS_VIEWPAGER2_FIELDS,
        R1CD06UpstreamConformance.classifyPager(1,2));
    assertEquals(R1CD06UpstreamConformance.Pager.AMBIGUOUS,
        R1CD06UpstreamConformance.classifyPager(2,1));
    assertEquals(R1CD06UpstreamConformance.Pager.NO_MATCH,
        R1CD06UpstreamConformance.classifyPager(0,0));
    assertEquals(R1CD06UpstreamConformance.Pager.REFLECTION_FAILED,
        R1CD06UpstreamConformance.classifyPager(1,-1));
  }
  @Test public void nextTargetStartsWithExactBuildGate(){
    assertEquals("VERIFY_IG455_EXACT_GATE",R1CD06UpstreamConformance.nextTarget());
  }
  @Test public void reportNeverClaimsIdentityOrHookEnablement(){
    String report=R1CD06UpstreamConformance.report();
    assertTrue(report.contains("INA_D06_INSTAECLIPSE_SOURCE_CONFORMANCE_V1"));
    assertTrue(report.contains("VISIBLE_REEL_IDENTITY=NOT_VERIFIED"));
    assertTrue(report.contains("ACTIONS=OFF"));
    assertFalse(report.contains("MEDIA_ID="));
  }
}
