package com.mikeyphw.instaactions;

import org.junit.Test;
import static org.junit.Assert.*;

public class ModuleConstantsTest {
    @Test public void onlyMainInstagramProcessIsTargeted() {
        assertTrue(ModuleConstants.isMainInstagramProcess("com.instagram.android"));
        assertFalse(ModuleConstants.isMainInstagramProcess("com.instagram.android:direct"));
        assertFalse(ModuleConstants.isMainInstagramProcess("ps.reso.instaeclipse"));
        assertFalse(ModuleConstants.isMainInstagramProcess(null));
    }
    @Test public void actionHooksRemainOffAtR1() {
        assertFalse(ModuleConstants.ACTION_HOOKS_ENABLED);
    }
}
