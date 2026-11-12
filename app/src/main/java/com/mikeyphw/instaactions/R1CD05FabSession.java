package com.mikeyphw.instaactions;

/**
 * D05 FAB-only, content-free capture window. No Instagram objects, strings,
 * accounts, media IDs, URLs, or views are accepted or retained here.
 * Windows are user-advanced; evidence counts NEVER establish visible-media identity.
 */
final class R1CD05FabSession {
    enum Step { IDLE, A1, A2, B1, B2, DONE, CANCELLED }
    static final long MAX_WINDOW_MS = 60_000L;
    static final int MAX_STAGE_EVENTS = 16;
    private static final String[] METRICS = {"MEDIA_TYPED_REELS", "UNKNOWN_SCREEN", "SKIPPED_UNSTABLE",
            "SKIPPED_WRONG_TYPE", "BASELINE", "BASELINE_COLLECTED", "SAME_REFERENCE", "DIFFERENT_REFERENCE"};
    private final int[][] counts = new int[4][METRICS.length];
    // D06 ownership verdicts are separate from D05 reference comparisons.
    private final int[][] ownerCounts =
            new int[4][R1CD06MenuOwnership.Verdict.values().length];
    private final int[][] visibleCounts =
            new int[4][R1CD06VisiblePageBinding.Verdict.values().length];
    // Structural-only FAB preflight, independent of menu callbacks and Media.
    private final int[][] preflightCounts = new int[4][R1CD06ViewPreflight.Status.values().length];
    private final int[][] preflightMax = new int[4][9];
    private final int[] preflightSamples = new int[4];
    private final boolean[] windowsValid = new boolean[4];
    private final int[] stageEvents = new int[4];
    private final long[] durationsMs = new long[4];
    private Step step = Step.IDLE;
    private long windowStart;
    private int totalEvents;
    private String reason = "not_started";

    synchronized Step step() { return step; }
    synchronized boolean isActiveWindow() { return index(step) >= 0; }
    synchronized int currentCount() {
        int i = index(step);
        return i < 0 ? 0 : counts[i][0];
    }
    synchronized int total() { return totalEvents; }

    synchronized boolean start(boolean hookInstalled, long nowMs) {
        if (!hookInstalled || step == Step.A1 || step == Step.A2 || step == Step.B1 || step == Step.B2)
            return false;
        for (int[] row : counts) java.util.Arrays.fill(row, 0);
        for (int[] row : ownerCounts) java.util.Arrays.fill(row, 0);
        for (int[] row : visibleCounts) java.util.Arrays.fill(row, 0);
        for (int[] row : preflightCounts) java.util.Arrays.fill(row, 0);
        for (int[] row : preflightMax) java.util.Arrays.fill(row, 0);
        java.util.Arrays.fill(preflightSamples, 0);
        java.util.Arrays.fill(windowsValid, false);
        java.util.Arrays.fill(stageEvents, 0);
        java.util.Arrays.fill(durationsMs, 0);
        totalEvents = 0;
        step = Step.A1;
        windowStart = nowMs;
        reason = "capturing";
        return true;
    }

    synchronized Step advance(long nowMs) {
        int i = index(step);
        if (i < 0) return step;
        long elapsed = nowMs >= windowStart ? nowMs - windowStart : MAX_WINDOW_MS + 1;
        durationsMs[i] = Math.min(MAX_WINDOW_MS + 1, elapsed);
        windowsValid[i] = elapsed <= MAX_WINDOW_MS && counts[i][0] > 0;
        step = i == 3 ? Step.DONE : Step.values()[i + 2];
        if (step == Step.DONE) reason = validAll() ? "REFERENCE_COUNTS_ONLY" : "INCONCLUSIVE_MISSING_OR_EXPIRED_WINDOW";
        else windowStart = nowMs;
        return step;
    }

    synchronized void observe(R1BNavigationPolicy.Screen screen,
            R1CD05ReferenceContinuity.Result result, boolean typed,
            R1CD06MenuOwnership.Verdict ownership,
            R1CD06VisiblePageBinding.Verdict visible, long nowMs) {
        int i = index(step);
        if (i < 0 || nowMs < windowStart || nowMs - windowStart > MAX_WINDOW_MS
                || totalEvents >= 64 || stageEvents[i] >= MAX_STAGE_EVENTS) return;
        totalEvents++;
        stageEvents[i]++;
        if (ownership != null) ownerCounts[i][ownership.ordinal()]++;
        if (visible != null) visibleCounts[i][visible.ordinal()]++;
        if (screen == R1BNavigationPolicy.Screen.REELS && typed) counts[i][0]++;
        if (screen == R1BNavigationPolicy.Screen.UNKNOWN) counts[i][1]++;
        switch (result) {
            case SKIPPED_UNSTABLE: counts[i][2]++; break;
            case SKIPPED_WRONG_TYPE: counts[i][3]++; break;
            case BASELINE: counts[i][4]++; break;
            case BASELINE_COLLECTED: counts[i][5]++; break;
            case SAME_REFERENCE: counts[i][6]++; break;
            case DIFFERENT_REFERENCE: counts[i][7]++; break;
            default: break;
        }
    }

    synchronized boolean needsPreflight() {
        int i=index(step);
        return i>=0 && preflightSamples[i] < R1CD06ViewPreflight.MAX_SAMPLES_PER_WINDOW;
    }

    synchronized void notePreflight(R1CD06ViewPreflight.Snapshot sample, long nowMs) {
        int i=index(step);
        if (i<0 || sample==null || nowMs<windowStart ||
                nowMs-windowStart>MAX_WINDOW_MS ||
                preflightSamples[i]>=R1CD06ViewPreflight.MAX_SAMPLES_PER_WINDOW) return;
        preflightSamples[i]++;
        preflightCounts[i][sample.status.ordinal()]++;
        int[] values={sample.visited,sample.shown,sample.pagers,sample.vertical,
                sample.centered,sample.eligible,sample.recyclers,sample.centeredPages,sample.durationMs};
        for(int k=0;k<values.length;k++)preflightMax[i][k]=Math.max(preflightMax[i][k],values[k]);
    }

    synchronized String currentPreflight() {
        int i=index(step);
        if(i<0)return "No live window";
        if(preflightSamples[i]==0)return "WAITING";
        for(R1CD06ViewPreflight.Status st:R1CD06ViewPreflight.Status.values())
            if(preflightCounts[i][st.ordinal()]>0 &&
                    st!=R1CD06ViewPreflight.Status.VIEWER_STRUCTURE_FOUND)
                return st.name();
        return "VIEWER_STRUCTURE_FOUND";
    }

    synchronized void cancel(boolean activityLeft) {
        if (index(step) >= 0) {
            step = Step.CANCELLED;
            reason = activityLeft ? "CANCELLED_ACTIVITY_LEFT" : "CANCELLED_BY_USER";
        }
    }

    private static int index(Step step) {
        switch(step) { case A1: return 0; case A2: return 1; case B1: return 2; case B2: return 3; default:return -1; }
    }
    private boolean validAll() { for (boolean b: windowsValid) if (!b) return false; return true; }

    synchronized String currentOwnership() {
        int i = index(step);
        if (i < 0) return "No live window";
        if (stageEvents[i] == 0) return "WAITING";
        R1CD06MenuOwnership.Verdict[] all = R1CD06MenuOwnership.Verdict.values();
        // Failures take precedence over a previous matching callback.
        // Never show a positive UI status for a mixed/ambiguous stage.
        for (R1CD06MenuOwnership.Verdict verdict : all) {
            if (verdict == R1CD06MenuOwnership.Verdict.OWNER_DIRECT_MATCH
                    || verdict == R1CD06MenuOwnership.Verdict.CONTEXT_DIRECT_MATCH
                    || verdict == R1CD06MenuOwnership.Verdict.OWNER_AND_CONTEXT_MATCH) continue;
            if (ownerCounts[i][verdict.ordinal()] > 0) return verdict.name();
        }
        for (R1CD06MenuOwnership.Verdict verdict : all) {
            if (ownerCounts[i][verdict.ordinal()] > 0) return verdict.name();
        }
        return "UNVERIFIED";
    }

    synchronized String currentVisiblePage() {
        int i = index(step);
        if (i < 0) return "No live window";
        if (stageEvents[i] == 0) return "WAITING";
        for (R1CD06VisiblePageBinding.Verdict verdict : R1CD06VisiblePageBinding.Verdict.values()) {
            if (verdict != R1CD06VisiblePageBinding.Verdict.VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE
                    && visibleCounts[i][verdict.ordinal()] > 0) return verdict.name();
        }
        return visibleCounts[i][R1CD06VisiblePageBinding.Verdict.VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE.ordinal()] > 0
                ? "VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE" : "UNVERIFIED";
    }

    /** Conservative, actionable diagnosis. This is a selection of the NEXT
     * investigation target, not a media-identity assertion or runtime pass.
     */
    synchronized String diagnosticSummary() {
        int samples=0, found=0, noPager=0, offMain=0, noDirect=0,
                mismatch=0, timeLimited=0, callbackCount=0;
        for (int i=0;i<4;i++) {
            samples+=preflightSamples[i];
            found+=preflightCounts[i][R1CD06ViewPreflight.Status.VIEWER_STRUCTURE_FOUND.ordinal()];
            noPager+=preflightCounts[i][R1CD06ViewPreflight.Status.NO_VIEWPAGER2.ordinal()]
                    +preflightCounts[i][R1CD06ViewPreflight.Status.NO_VERTICAL_PAGER.ordinal()];
            offMain+=visibleCounts[i][R1CD06VisiblePageBinding.Verdict.OFF_MAIN_THREAD.ordinal()];
            noDirect+=visibleCounts[i][R1CD06VisiblePageBinding.Verdict.NO_DIRECT_MEDIA.ordinal()];
            mismatch+=visibleCounts[i][R1CD06VisiblePageBinding.Verdict.MEDIA_MISMATCH.ordinal()]
                    +visibleCounts[i][R1CD06VisiblePageBinding.Verdict.MEDIA_AMBIGUOUS.ordinal()];
            timeLimited+=visibleCounts[i][R1CD06VisiblePageBinding.Verdict.DECOR_TIME_LIMIT.ordinal()]
                    +visibleCounts[i][R1CD06VisiblePageBinding.Verdict.PAGE_TIME_LIMIT.ordinal()];
            callbackCount+=stageEvents[i];
        }
        if(samples==0)return "NO_FAB_PREFLIGHT_SAMPLES";
        if(callbackCount==0)return "NO_MENU_CALLBACKS_CAPTURED";
        if(offMain>0 && found>0)return "PAGER_PRESENT_BUT_CALLBACK_OFF_MAIN";
        if(offMain>0)return "CALLBACK_OFF_MAIN_CHECK_PAGER_PREFLIGHT";
        if(mismatch>0)return "PAGE_MEDIA_MISMATCH_OR_AMBIGUITY";
        if(noDirect>0 && found>0)return "PAGER_FOUND_BUT_PAGE_HAS_NO_DIRECT_MEDIA";
        if(timeLimited>0)return "SYNCHRONOUS_VIEW_SCAN_TIME_LIMIT";
        if(found==0 && noPager>0)return "EXPECTED_VIEWPAGER2_NOT_FOUND_IN_SAMPLES";
        if(found>0)return "VIEWER_STRUCTURE_PRESENT_CHECK_PER_STAGE_CALLBACK_VERDICTS";
        return "INSUFFICIENT_OR_MIXED_VIEWER_STRUCTURE";
    }

    private boolean visibleCandidateOnEveryWindow() {
        for (int i=0; i<4; i++) {
            int matches = visibleCounts[i][R1CD06VisiblePageBinding.Verdict.VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE.ordinal()];
            // A single positive cannot mask failures in the same window.
            if (!windowsValid[i] || matches < 1 || matches != stageEvents[i]) return false;
        }
        return true;
    }

    private boolean ownedOnEveryWindow() {
        for (int i=0; i < 4; i++) {
            int accepted = ownerCounts[i][R1CD06MenuOwnership.Verdict.OWNER_DIRECT_MATCH.ordinal()]
                    + ownerCounts[i][R1CD06MenuOwnership.Verdict.CONTEXT_DIRECT_MATCH.ordinal()]
                    + ownerCounts[i][R1CD06MenuOwnership.Verdict.OWNER_AND_CONTEXT_MATCH.ordinal()];
            // One accepted callback cannot mask a failed/ambiguous callback in
            // the same window. All events must be positively resolved.
            if (!windowsValid[i] || accepted < 1 || accepted != stageEvents[i]) return false;
        }
        return true;
    }

    synchronized String prompt() {
        switch(step) {
            case IDLE: return "Start a read-only D05 capture. InstaEclipse stays enabled.";
            case A1: return "Reel A: open its three-dot menu once, then tap Next.";
            case A2: return "Same Reel A: reopen its three-dot menu, then tap Next.";
            case B1: return "Swipe to a DIFFERENT Reel B; open its menu, then Next.";
            case B2: return "Same Reel B: reopen its menu, then Finish.";
            case DONE: return "Capture finished. Copy or share the content-free report.";
            case CANCELLED: return "Capture cancelled on activity exit. Start again.";
            default: return "Unavailable";
        }
    }

    synchronized String report() {
        StringBuilder out = new StringBuilder(650);
        out.append("INA_D06_CONSOLIDATED_FINDINGS_V1\n")
           .append("EVIDENCE_ORDER=UPSTREAM_SOURCE_THEN_EXACT_IG455_DEX_THEN_DEVICE\n")
           .append("EXACT_IG455_HOOK_GATE=").append(R1CD04PassiveProbe.isInstalled()).append('\n')
           .append("UPSTREAM_MENU_STATUS=").append(R1CD06UpstreamConformance.summary()).append('\n')
           .append("CURRENT_CAPTURE_STAGE=").append(step).append('\n')
           .append("CURRENT_D05_CONTINUITY=").append(step == Step.DONE && validAll()
                    ? "FOUR_WINDOWS_REFERENCE_COUNTS_ONLY" : "NOT_QUALIFIED_IN_CURRENT_CAPTURE").append('\n')
           .append("CURRENT_D06_MENU_OWNER=").append(step == Step.DONE && ownedOnEveryWindow()
                    ? "FOUR_WINDOWS_DIRECT_OWNER_MATCH_ONLY" : "NOT_QUALIFIED_IN_CURRENT_CAPTURE").append('\n')
           .append("CURRENT_VISIBLE_PAGE=").append(step == Step.DONE && visibleCandidateOnEveryWindow()
                    ? "FOUR_CANDIDATE_WINDOWS_UNVERIFIED" : "NOT_QUALIFIED_IN_CURRENT_CAPTURE").append('\n')
           .append("NEXT_REQUIRED_CONFIRMATION=").append(R1CD06UpstreamConformance.nextTarget()).append('\n')
           .append("INSTA_ECLIPSE=user_kept_enabled_not_programmatically_verified\n")
           .append("MEDIA_IDENTITY=NOT_VERIFIED\nACTIONS=OFF\n")
           .append("FULL_RAW_COUNTERS_FOLLOW=true\n")
           .append("INA_D05_FAB_REPORT_V1\n")
           .append("RESULT=").append(reason).append('\n')
           .append("STEP=").append(step).append('\n')
           .append("HOOK=exact_IG455_gate_required_at_start\n")
           .append("INSTA_ECLIPSE=user_kept_enabled_not_programmatically_verified\n")
           .append("MEDIA_IDENTITY=NOT_VERIFIED\nACTIONS=OFF\n")
           .append("D06_SELECTION=UPSTREAM_DERIVED_DIRECT_OWNER_ONLY\n")
           .append("D06_VISIBLE_BINDING=NOT_VERIFIED\n")
           .append("NOTE=windows_are_user_labeled_not_visible_media_identity\n");
        String[] names = {"A1", "A2", "B1", "B2"};
        for (int i=0;i<4;i++) {
            out.append("WINDOW_").append(names[i]).append("_VALID=").append(windowsValid[i]).append('\n');
            out.append("WINDOW_").append(names[i]).append("_DURATION_MS=").append(durationsMs[i]).append('\n');
            for (int j=0;j<METRICS.length;j++) {
                out.append(names[i]).append('_').append(METRICS[j]).append('=')
                   .append(counts[i][j]).append('\n');
            }
        }
        out.append("INA_D06_MENU_OWNER_REPORT_V1\n")
           .append("D06_OWNER_RESULT=")
           .append(step == Step.DONE && ownedOnEveryWindow()
                   ? "FOUR_WINDOWS_DIRECT_OWNER_MATCH_ONLY" : "UNVERIFIED_OR_INCONCLUSIVE")
           .append('\n');
        R1CD06MenuOwnership.Verdict[] verdicts = R1CD06MenuOwnership.Verdict.values();
        for (int i = 0; i < 4; i++) {
            for (R1CD06MenuOwnership.Verdict verdict : verdicts) {
                out.append(names[i]).append("_D06_").append(verdict.name())
                   .append('=').append(ownerCounts[i][verdict.ordinal()]).append('\n');
            }
        }
        out.append("INA_D06_VISIBLE_PAGE_REPORT_V1\n")
           .append("D06_VISIBLE_PAGE_RESULT=")
           .append(step == Step.DONE && ownedOnEveryWindow() && visibleCandidateOnEveryWindow()
                   ? "FOUR_WINDOWS_PAGE_MATCH_CANDIDATES_ONLY" : "UNVERIFIED_OR_INCONCLUSIVE")
           .append('\n')
           .append("D06_ACTIVE_VIEW_BINDING=NOT_QUALIFIED\n");
        R1CD06VisiblePageBinding.Verdict[] visibleVerdicts = R1CD06VisiblePageBinding.Verdict.values();
        for (int i=0;i<4;i++) {
            for (R1CD06VisiblePageBinding.Verdict verdict : visibleVerdicts) {
                out.append(names[i]).append("_VP_").append(verdict.name())
                   .append('=').append(visibleCounts[i][verdict.ordinal()]).append('\n');
            }
        }
        out.append("INA_D06_DEEP_DIAGNOSIS_V1\n")
           .append("NEXT_DIAGNOSTIC_TARGET=").append(diagnosticSummary()).append('\n')
           .append("DIAGNOSIS_IS_PROVISIONAL=true\n")
           .append("INA_D06_STRUCTURAL_PREFLIGHT_REPORT_V1\n")
           .append("D06_PREFLIGHT_SCOPE=FAB_MAIN_THREAD_NO_MEDIA_OR_IDENTITY\n")
           .append("D06_PREFLIGHT_MAX_SAMPLES_PER_STAGE=")
           .append(R1CD06ViewPreflight.MAX_SAMPLES_PER_WINDOW).append('\n');
        String[] preflightMetrics={"VISITED","SHOWN","PAGERS","VERTICAL",
                "CENTERED","ELIGIBLE","RECYCLERS","CENTERED_PAGES","DURATION_MS"};
        for(int i=0;i<4;i++) {
            out.append(names[i]).append("_PF_SAMPLES=").append(preflightSamples[i]).append('\n');
            for(R1CD06ViewPreflight.Status st:R1CD06ViewPreflight.Status.values())
                out.append(names[i]).append("_PF_").append(st.name()).append('=')
                   .append(preflightCounts[i][st.ordinal()]).append('\n');
            for(int k=0;k<preflightMetrics.length;k++)
                out.append(names[i]).append("_PF_MAX_").append(preflightMetrics[k]).append('=')
                   .append(preflightMax[i][k]).append('\n');
        }
        out.append(R1CD06UpstreamConformance.report());
        return out.toString();
    }
}
