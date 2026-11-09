package com.mikeyphw.instaactions;

import org.junit.Test;
import static org.junit.Assert.*;

public class R1ProbePolicyTest {
    @Test public void versionGateFailsClosed() {
        assertTrue(R1ProbePolicy.supportedVersion("451.0.0.20.80"));
        assertTrue(R1ProbePolicy.supportedVersion("453.0.0.0.56"));
        assertFalse(R1ProbePolicy.supportedVersion(null));
        assertFalse(R1ProbePolicy.supportedVersion("454.0.0.0.1"));
        assertFalse(R1ProbePolicy.supportedVersion("534.0.0.8.108"));
    }
    @Test public void classifyRequiresUniqueMatch() {
        assertEquals(R1ProbePolicy.Result.FAILED, R1ProbePolicy.classify(-1));
        assertEquals(R1ProbePolicy.Result.MISSING, R1ProbePolicy.classify(0));
        assertEquals(R1ProbePolicy.Result.UNIQUE, R1ProbePolicy.classify(1));
        assertEquals(R1ProbePolicy.Result.AMBIGUOUS, R1ProbePolicy.classify(2));
    }
    @Test public void cacheIdentityIncludesApkSizeAndTimestamp() {
        String a = R1ProbePolicy.cacheKey("453.0.0.0.56", 100, 101);
        String b = R1ProbePolicy.cacheKey("453.0.0.0.56", 100, 102);
        assertNotEquals(a, b);
        assertNull(R1ProbePolicy.cacheKey("454.0.0.0.1", 100, 101));
        assertNull(R1ProbePolicy.cacheKey("451.0.0.20.80", 0, 101));
    }
    @Test public void probeInventoryIsReadOnlyAndBounded() {
        assertFalse(R1ProbePolicy.MUTATING_ACTIONS_ENABLED);
        assertEquals(5, R1ProbePolicy.MARKERS.length);
        for (R1ProbePolicy.Marker marker : R1ProbePolicy.MARKERS) {
            assertNotNull(marker.id);
            assertFalse(marker.needle.isEmpty());
        }
    }
}
