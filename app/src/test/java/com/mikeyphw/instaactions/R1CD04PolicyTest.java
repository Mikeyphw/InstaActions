package com.mikeyphw.instaactions;

import org.junit.Test;
import static org.junit.Assert.*;

public final class R1CD04PolicyTest {
    @Test public void exactApkAndVersionRequired() {
        assertTrue(R1CD04Policy.validBuild("455.0.0.8.72", R1CD04Policy.TARGET_APK_SHA256));
        assertFalse(R1CD04Policy.validBuild("453.0.0.0.56", R1CD04Policy.TARGET_APK_SHA256));
        assertFalse(R1CD04Policy.validBuild("455.0.0.8.72", "bad"));
        assertFalse(R1CD04Policy.validBuild(null, null));
    }
    @Test public void boundsAndPermissionsStayClosed() {
        assertTrue(R1CD04Policy.MAX_RECORDED_EVENTS <= 64);
        assertTrue(R1CD04Policy.MAX_DECLARED_METHODS <= 256);
        assertFalse(R1CD04Policy.MEDIA_IDENTITY_VERIFIED);
        assertFalse(R1CD04Policy.ACTIONS_ENABLED);
    }
}
