package com.mikeyphw.instaactions;

import android.content.Context;

/** Persisted in the standalone module application's private storage; OFF by default. */
final class R1CD20FabOverrideStore {
    static final String PREFS = "r1c_d20_fab_version_override";
    static final String KEY = "ignore_unsupported_instagram_version_for_fab";
    static boolean enabled(Context c) {
        return c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getBoolean(KEY,false);
    }
    static boolean setEnabled(Context c,boolean value) {
        // Synchronous commit: the value must be visible before switching back to Instagram.
        return c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit()
                .putBoolean(KEY,value).commit();
    }
    private R1CD20FabOverrideStore() {}
}
