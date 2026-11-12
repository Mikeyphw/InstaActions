package com.mikeyphw.instaactions;

/**
 * Pure presentation guidance for D07. This does not grant eligibility or arm
 * anything: R1CD07Recovery and the exact IG455 hooks remain authoritative.
 */
final class R1CD07UxGuide {
    private R1CD07UxGuide() { }

    static String next(boolean recoveryOn, boolean hooksReady, boolean dataReady,
            boolean reelsNavigation, boolean recordingA, boolean freshA,
            boolean itmRecoveryOffAcknowledged) {
        if (recoveryOn) return "Recovery is ON. Close this panel to try the original Repost button, then return and press Recovery OFF before leaving this account or tab.";
        if (!hooksReady) return "Open Instagram 455, wait for the exact native hooks, and reopen IA. If verification fails, do not arm recovery.";
        if (!dataReady) return "Native data hook not ready. Keep recovery OFF; the exact Instagram build must be verified first.";
        if (!reelsNavigation) return "Go to the dedicated Reels tab (bottom navigation), not a Reel inside Feed, a post or a profile. Then reopen IA.";
        if (!recordingA) return "Tap Start A, close IA, and browse Reels on your affected account. Account labels are manual, not verified identities.";
        if (!freshA) return "Keep A recording and browse several Reels until fresh native eligibility results are observed. Do not press Stop A/B yet.";
        if (!itmRecoveryOffAcknowledged) return "Turn OFF InstaTaskManager's Repost recovery, then tap its confirmation here. This is only your confirmation; apps cannot verify one another.";
        return "Evidence is ready for a manual attempt. Tap Enable native Repost, close IA and test the original Repost control. No automatic repost is performed.";
    }

    static String shortState(boolean enabled, boolean recordingA, boolean recordingB) {
        if (enabled) return "RECOVERY ON · LIMITED SESSION";
        if (recordingA) return "OBSERVING A · RECOVERY OFF";
        if (recordingB) return "OBSERVING B · RECOVERY OFF";
        return "RECOVERY OFF · SAFE DEFAULT";
    }
}
