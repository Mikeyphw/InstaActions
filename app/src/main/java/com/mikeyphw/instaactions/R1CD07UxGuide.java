package com.mikeyphw.instaactions;

/**
 * Pure UX02 instructions, not an Instagram account/tab identity verifier.
 * This class has NO hooks and cannot enable or change the D07 recovery gates.
 */
final class R1CD07UxGuide {
    private R1CD07UxGuide() { }

    static int step(boolean recoveryOn, boolean hooksReady, boolean dataReady,
            boolean reelsNavigation, boolean recordingA, boolean freshA,
            boolean itmRecoveryOffAcknowledged) {
        if (recoveryOn) return 5;
        if (!hooksReady || !dataReady || !reelsNavigation) return 1;
        if (!recordingA) return 2;
        if (!freshA) return 3;
        return 4;
    }

    static String next(boolean recoveryOn, boolean hooksReady, boolean dataReady,
            boolean reelsNavigation, boolean recordingA, boolean freshA,
            boolean itmRecoveryOffAcknowledged) {
        int step=step(recoveryOn,hooksReady,dataReady,reelsNavigation,
                recordingA,freshA,itmRecoveryOffAcknowledged);
        if (recoveryOn) return "STEP 5 OF 5 — Recovery ON. Close IA, tap Instagram's original Repost yourself, then return to IA and press Recovery OFF before changing account/tab. Copy the report.";
        if (!hooksReady) return "STEP 1 OF 5 — Open exact Instagram 455, check Vector scope and InstaEclipse, restart Instagram, then reopen IA. Native hooks must be ready.";
        if (!dataReady) return "STEP 1 OF 5 — Native data hook not ready. Do not press Start A or Enable. Verify the exact Instagram build and restart.";
        if (!reelsNavigation) return "STEP 1 OF 5 — Open the dedicated Reels tab from the bottom bar (NOT Feed/profile/post Reels), then reopen IA.";
        if (!recordingA) return "STEP 2 OF 5 — Press Start A ONCE on the affected account. Do NOT confirm ITM OFF yet; starting A clears previous confirmations.";
        if (!freshA) return "STEP 3 OF 5 — Keep A recording. Close IA, swipe through several Reels within the dedicated Reels tab, reopen IA and check again. Do not press Stop A/B.";
        if (!itmRecoveryOffAcknowledged) return "STEP 4 OF 5 — A evidence ready. Confirm InstaTaskManager Repost recovery is OFF (4A), THEN tap Enable (4B). This confirmation is manual, not verified by IA.";
        return "STEP 4 OF 5 — A evidence ready and ITM OFF confirmed. Tap Enable (4B) NOW, then follow Step 5. No automatic Repost happens.";
    }

    static String checklist(boolean hooksReady, boolean dataReady, boolean reelsNavigation,
            boolean recordingA, boolean freshA, boolean itmRecoveryOffAcknowledged) {
        return "READINESS (technical hints, not verified account/tab identity)\n"
            + "Native hooks: " + (hooksReady ? "READY" : "WAIT")
            + " · Data hook: " + (dataReady ? "READY" : "WAIT")
            + "\nReels viewer hint: " + (reelsNavigation ? "PRESENT" : "NOT DETECTED")
            + " · A recording: " + (recordingA ? "YES" : "NO")
            + "\nFresh A eligibility: " + (freshA ? "READY" : "WAIT")
            + " · ITM OFF acknowledged: " + (itmRecoveryOffAcknowledged ? "YES (manual)" : "NO");
    }

    static String shortState(boolean enabled, boolean recordingA, boolean recordingB) {
        if (enabled) return "RECOVERY ON · LIMITED SESSION";
        if (recordingA) return "OBSERVING A · RECOVERY OFF";
        if (recordingB) return "OBSERVING B · RECOVERY OFF";
        return "RECOVERY OFF · SAFE DEFAULT";
    }
}
