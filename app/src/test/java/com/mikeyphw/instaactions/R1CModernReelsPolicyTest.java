package com.mikeyphw.instaactions;
import org.junit.Test;
import static org.junit.Assert.*;
public final class R1CModernReelsPolicyTest {
    @Test public void exactApkShapeOnly() {
        assertTrue(R1CModernReelsPolicy.exact(
                "LX/07EF;->A05(LX/03jS;Lcom/instagram/feed/media/Media;)Ljava/util/ArrayList;"));
        assertFalse(R1CModernReelsPolicy.exact(null));
        assertFalse(R1CModernReelsPolicy.exact(
                "LX/07EF;->A05(Lcom/instagram/feed/media/Media;)Ljava/util/ArrayList;"));
    }
    @Test public void incompleteAndAmbiguousFailClosed() {
        assertEquals("IG455_EXACT_DESCRIPTOR_CANDIDATE",
                R1CModernReelsPolicy.conclusion(3, 1, false));
        assertEquals("NO_EXACT_DESCRIPTOR_MATCH",
                R1CModernReelsPolicy.conclusion(32, 0, false));
        assertEquals("AMBIGUOUS_EXACT_DESCRIPTOR",
                R1CModernReelsPolicy.conclusion(3, 2, false));
        assertEquals("INCOMPLETE",
                R1CModernReelsPolicy.conclusion(65, 1, true));
        assertEquals("INCOMPLETE",
                R1CModernReelsPolicy.conclusion(1, 1, true));
    }
}
