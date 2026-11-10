package com.mikeyphw.instaactions;

/**
 * APK-455-only, read-only descriptor evidence for the modern Reels list builder.
 * A matching declaration is NOT a bound, on-screen Media instance.
 * No Instagram-internal method is loaded, called or hooked by this policy.
 */
final class R1CModernReelsPolicy {
    static final String OWNER = "X.07EF";
    static final String EXACT_DESCRIPTOR =
            "LX/07EF;->A05(LX/03jS;Lcom/instagram/feed/media/Media;)Ljava/util/ArrayList;";
    static final int MAX_MATCHES = 64;

    private R1CModernReelsPolicy() { throw new AssertionError("No instances"); }

    static boolean exact(String descriptor) {
        return EXACT_DESCRIPTOR.equals(descriptor);
    }

    static String conclusion(int raw, int exact, boolean truncated) {
        if (truncated || raw > MAX_MATCHES || raw < 0 || exact < 0 || exact > raw)
            return "INCOMPLETE";
        if (exact == 1) return "IG455_EXACT_DESCRIPTOR_CANDIDATE";
        if (exact > 1) return "AMBIGUOUS_EXACT_DESCRIPTOR";
        return "NO_EXACT_DESCRIPTOR_MATCH";
    }
}
