package com.mikeyphw.instaactions;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class R0SmokeTest {
    @Test public void applicationIsCorrectAndHasNoHooks() {
        assertEquals("com.mikeyphw.instaactions",
            InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName());
        assertFalse(ModuleConstants.ACTION_HOOKS_ENABLED);
    }
}
