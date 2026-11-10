package com.mikeyphw.instaactions;

/** Fail-closed, deterministic D04 acceptance gate; no action capability. */
public final class R1CD04Policy {
    static final String TARGET_VERSION = "455.0.0.8.72";
    static final String TARGET_APK_SHA256 =
            "fb6ca8c21d909d95892e67339746b8fe43f409a9dea11f87d9ab0cee8e97c5b6";
    static final long TARGET_APK_BYTES = 238346353L;
    static final long MAX_HASH_BYTES = 320L * 1024L * 1024L;
    static final int MAX_DECLARED_METHODS = 256;
    static final int MAX_RECORDED_EVENTS = 64;
    public static final boolean MEDIA_IDENTITY_VERIFIED = false;
    public static final boolean ACTIONS_ENABLED = false;
    private R1CD04Policy() { throw new AssertionError("No instances"); }

    static boolean validBuild(String version, String digest) {
        return TARGET_VERSION.equals(version) && TARGET_APK_SHA256.equals(digest);
    }
}
