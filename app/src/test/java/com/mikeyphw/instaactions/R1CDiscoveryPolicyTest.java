package com.mikeyphw.instaactions;

import org.junit.Test;
import static org.junit.Assert.*;

public class R1CDiscoveryPolicyTest {
    @Test public void exactVersionGateAndNoSilentExpansion() {
        assertTrue(R1CDiscoveryPolicy.mayInventory("455.0.0.8.72"));
        assertFalse(R1CDiscoveryPolicy.mayInventory("455.0.0.8.73"));
        assertFalse(R1CDiscoveryPolicy.mayInventory("453.0.0.0.56"));
        assertFalse(R1CDiscoveryPolicy.mayInventory(null));
    }
    @Test public void pureCandidateStatusNeverPromotes() {
        assertEquals(R1CDiscoveryPolicy.Outcome.MISSING, R1CDiscoveryPolicy.classify(0));
        assertEquals(R1CDiscoveryPolicy.Outcome.UNIQUE_CANDIDATE, R1CDiscoveryPolicy.classify(1));
        assertEquals(R1CDiscoveryPolicy.Outcome.AMBIGUOUS, R1CDiscoveryPolicy.classify(2));
        assertEquals(R1CDiscoveryPolicy.Outcome.FAILED, R1CDiscoveryPolicy.classify(-1));
        assertFalse(R1CDiscoveryPolicy.MEDIA_IDENTITY_VERIFIED);
        assertFalse(R1CDiscoveryPolicy.BINDING_HOOKS_ENABLED);
        assertFalse(R1CDiscoveryPolicy.ACTION_HOOKS_ENABLED);
        assertFalse(R1CDiscoveryPolicy.mayBindMediaInstance(true,
                R1CDiscoveryPolicy.Outcome.UNIQUE_CANDIDATE, true));
    }
    @Test public void finiteProbeBudget() {
        assertEquals(3, R1CDiscoveryPolicy.MAX_MARKER_QUERIES);
        assertEquals(5, R1CDiscoveryPolicy.MAX_CLASS_PROBES);
    }
}
