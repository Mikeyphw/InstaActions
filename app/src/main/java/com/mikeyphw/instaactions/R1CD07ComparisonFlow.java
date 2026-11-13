package com.mikeyphw.instaactions;

/** UI-only read-only A/B capture instructions; does not authenticate account identity. */
final class R1CD07ComparisonFlow {
    private R1CD07ComparisonFlow() { }
    static boolean canStopA(boolean recording, int cohort, boolean recoveryOn) {
        return recording && cohort == 0 && !recoveryOn;
    }
    static boolean canStartB(boolean recording, int cohort, int aEvents,
            boolean recoveryOn, boolean hooksReady) {
        return !recording && cohort == -1 && aEvents > 0 && !recoveryOn && hooksReady;
    }
    static boolean canStopB(boolean recording, int cohort, boolean recoveryOn) {
        return recording && cohort == 1 && !recoveryOn;
    }
    static String progress(boolean recording, int cohort, int aEvents,
            int bEvents, boolean recoveryOn) {
        String counts = "A: " + aEvents + " recorded events · B: " + bEvents + " recorded events. ";
        if (recoveryOn) return counts + "Recovery ON — press Recovery OFF before an account switch.";
        if (recording && cohort == 0) return counts + "A RECORDING — browse, then press Stop A.";
        if (recording && cohort == 1) return counts + "B RECORDING — browse, then press Stop B.";
        if (aEvents == 0) return counts + "Start A above, browse Reels, then Stop A.";
        if (bEvents == 0) return counts + "A saved. Switch accounts manually, then Start B.";
        return counts + "Both cohorts saved in memory. Copy combined A/B report now.";
    }
}
