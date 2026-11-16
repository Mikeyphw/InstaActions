package com.mikeyphw.instaactions;

/** Pure UI-only policy. This class never qualifies Instagram-native action hooks. */
final class R1CD20FabGatePolicy {
    static boolean canShowFab(boolean exactVersion, boolean userRequestedOverride) {
        return exactVersion || userRequestedOverride;
    }
    static boolean canOfferNativeRecovery(boolean exactVersion) {
        // The existing exact D04 SHA and D07 evidence gates also remain mandatory.
        return exactVersion;
    }
    private R1CD20FabGatePolicy() {}
}
