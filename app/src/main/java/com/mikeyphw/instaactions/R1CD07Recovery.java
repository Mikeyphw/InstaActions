package com.mikeyphw.instaactions;

/**
 * HF13: opt-in, in-memory, short-lease native Repost UI recovery.
 * A/B are manual labels, NOT authenticated account identities. In particular,
 * this is NOT a persistent account-bound enable switch. User must re-arm only
 * while viewing Reels from the Reels TAB and stop before switching accounts.
 * No Repost actions, request handlers, network data, keys or social records
 * are created, read, or persisted. Only the HF12 exact D3r false result is
 * eligible for local UI-state recovery. Native null/true are never changed.
 */
final class R1CD07Recovery {
    static final int LIMIT = 64;
    static final int SESSION_LIMIT = 4096; // bounded same-foreground diagnostic mode
    static final long LEASE_NS = 90_000_000_000L; // explicit re-arm for longer sessions
    interface Clock { long now(); }
    private static Clock clock = System::nanoTime;
    private static boolean enabled;
    private static boolean sessionMode;
    private static long expires, epoch=1, leaseEpoch;
    private static int altered, attempts;
    private static String lastReason="Default OFF; Reels tab only (manual confirmation)";

    private R1CD07Recovery() { }
    static synchronized long epoch() {return epoch;}
    static synchronized boolean isEnabled() {
        if (enabled && ((!sessionMode && clock.now() - expires >= 0) ||
                altered >= (sessionMode ? SESSION_LIMIT : LIMIT)))
            off("Lease expired or call limit reached");
        return enabled;
    }
    static synchronized void off(String why) {
        enabled=false; sessionMode=false; expires=0; leaseEpoch=0;
        epoch++; // invalidates already-running frames across disable/account change
        lastReason=why;
    }
    static synchronized void onCaptureTransition() {off("Capture started/stopped or account label changed");}
    static synchronized void onActivityExit() {off("Instagram Activity left foreground");}
    static synchronized void onHookFailure() {off("Exact native data hook unavailable");}

    static synchronized String arm(int cohort, boolean verifiedApk, boolean dataHookReady) {
        return armMode(cohort, verifiedApk, dataHookReady, false);
    }
    static synchronized String armSession(int cohort, boolean verifiedApk, boolean dataHookReady) {
        return armMode(cohort, verifiedApk, dataHookReady, true);
    }
    private static synchronized String armMode(int cohort, boolean verifiedApk,
            boolean dataHookReady, boolean keepDuringForeground) {
        off("Re-arm requested");
        if (!verifiedApk) return refusal("Exact Instagram APK/hooks not verified");
        if (!dataHookReady) return refusal("Native D3r hook not confirmed active");
        if (cohort != 0) return refusal("Start A first; only affected manual cohort supported");
        if (!R1CD07ExactGateTrial.isDeoptimized()) return refusal("Exact selector-4 deoptimization unavailable");
        if (!R1CD07ExactGateTrial.eligibleForHF13Recovery())
            return refusal("Fresh Reels-tab A native-data evidence missing/contradicted");
        // Mutual exclusion; HF12's original bounded experiment remains for CI regression.
        R1CD07ExactGateTrial.disarm();
        altered=0;attempts=0;
        leaseEpoch=epoch;
        sessionMode=keepDuringForeground;
        expires=keepDuringForeground ? 0 : clock.now()+LEASE_NS;
        enabled=true;
        lastReason="Manual A, Reels TAB only; no verified account identity binding";
        return "ENABLED HF13: native Repost UI only; manual A / Reels tab, " +
                (sessionMode ? "foreground-session (no 90s timer), 4096-call safety cap" :
                    "90 s or 64 changed calls") +
                "; OFF before account switch or leaving Instagram. No automatic repost.";
    }
    private static String refusal(String why) {lastReason=why;return "REFUSED HF13: "+why+"; unchanged";}

    /** Caller must have proven the exact D3r int key, selector-4 stack and data. */
    static synchronized Object maybeOverride(long entryEpoch, int cohort, int dataPresence,
                                             boolean nested, Object original) {
        if (!isEnabled()) return original;
        // Long foreground sessions fail closed if the *native* exact data
        // predicate diverges; could mean changed content, account or rollout.
        if(sessionMode && !Boolean.FALSE.equals(original)) {
            off("Native D3r original result changed; account/context change SUSPECTED");
            return original;
        }
        if (entryEpoch!=leaseEpoch || nested || cohort!=0 ||
            dataPresence!=R1CD07BranchInputs.PRESENT || !Boolean.FALSE.equals(original))
            return original;
        attempts++;
        if (!R1CD07ExactGateTrial.isDeoptimized() ||
            !R1CD07ExactGateTrial.eligibleForHF13Recovery()) {
            off("Native eligibility lost; original results restored");
            return original;
        }
        altered++;
        // Final permitted result still changed; next call returns native value.
        if (altered >= (sessionMode ? SESSION_LIMIT : LIMIT)) {
            enabled=false;leaseEpoch=0;expires=0;epoch++;
            lastReason="changed-call safety guard reached; re-arm manually on Reels tab";
            sessionMode=false;
        }
        return Boolean.TRUE;
    }
    static synchronized boolean isSessionMode(){return isEnabled() && sessionMode;}
    static synchronized String status() {
        boolean on=isEnabled();
        long left=on && !sessionMode?Math.max(0,(expires-clock.now())/1_000_000_000L):0;
        return (on?(sessionMode?"SESSION ON (foreground only; no 90s timer)":
                "ENABLED (manual A · Reels-tab claim; "+left+"s left)"):"OFF")+
            " · altered="+altered+"/"+(sessionMode?SESSION_LIMIT:LIMIT)+" · attempts="+attempts+
            " · " + lastReason;
    }
    // Package-only synthetic testing, never used in production paths.
    static synchronized void setClockForTest(Clock value) {off("Test reset");clock=value;}
    static synchronized void restoreClockForTest() {off("Test reset");clock=System::nanoTime;}
}
