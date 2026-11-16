package com.mikeyphw.instaactions;

import java.util.ArrayDeque;
import java.util.Arrays;

/**
 * D13: automatic, content-free navigation discovery. The existing R1B stable
 * screen is the only viewer anchor. Bottom-tab state is an additional UI hint,
 * not proof of which Media or account is active. No IDs/strings from Views,
 * Activity, user records, URLs, network, or objects are retained.
 */
final class R1CD13AutoSurface {
    enum Surface {
        HOME_FEED, DEDICATED_REELS_TAB_HINT, FEED_OPENED_REEL_CANDIDATE,
        REELS_ORIGIN_UNKNOWN, STORY, UNKNOWN
    }
    private static final int MAX_EVENTS=96;
    private static final int MAX_COUNT=100_000;
    private static final int MAX_INVALID=100_000;
    private static final int[][] A_B_COUNTS=new int[2][Surface.values().length];
    private static final int[] COUNTS=new int[Surface.values().length];
    private static final String[] REASONS={"TRUNCATED_VIEW_SCAN", "CONFLICTING_TABS",
        "REELS_TABS_HIDDEN", "NONMAIN_REELS", "FEED_TAB_NOT_SELECTED",
        "UNANCHORED_OR_UNKNOWN", "OTHER_UNRESOLVED"};
    private static final int[] REASON_COUNTS=new int[REASONS.length];
    private static final ArrayDeque<String> EVENTS=new ArrayDeque<>();
    private static Surface current=Surface.UNKNOWN, pending=Surface.UNKNOWN;
    private static int pendingRepeats, count, transitions, dropped, invalidated;
    private static int ambiguousOrigin, candidateOrigin, explicitTabOrigin, skipped;
    private static int lastCohort=-1;
    private static boolean sawHome;
    private static long ordinal;
    private static String status="NO_SUPPORTED_ACTIVITY_SAMPLES";
    private R1CD13AutoSurface() { }

    /** Source-backed R1B anchored viewer + exact Instagram navigation-tab hints.
     * A feed-opened Reel cannot be VERIFIED from this evidence; it is a candidate.
     */
    static Surface classify(String stableScreen, boolean mainActivity,
            boolean feedTabSelected, boolean clipsTabSelected, boolean truncated) {
        if(truncated || feedTabSelected && clipsTabSelected) return Surface.UNKNOWN;
        if("STORY".equals(stableScreen))return Surface.STORY;
        if("HOME_FEED".equals(stableScreen))
            return mainActivity && feedTabSelected && !clipsTabSelected
                ? Surface.HOME_FEED : Surface.UNKNOWN;
        if("REELS".equals(stableScreen)) {
            if(!mainActivity)return Surface.REELS_ORIGIN_UNKNOWN;
            if(clipsTabSelected && !feedTabSelected)
                return Surface.DEDICATED_REELS_TAB_HINT;
            if(feedTabSelected && !clipsTabSelected)
                return Surface.FEED_OPENED_REEL_CANDIDATE;
            return Surface.REELS_ORIGIN_UNKNOWN;
        }
        return Surface.UNKNOWN;
    }
    private static void event(String type, String detail) {
        if(EVENTS.size()==MAX_EVENTS){EVENTS.removeFirst();dropped++;}
        EVENTS.addLast("#"+(++ordinal)+" "+type+" "+detail);
    }
    static synchronized void observe(String stableScreen, boolean mainActivity,
            boolean feedTabSelected, boolean clipsTabSelected, boolean truncated) {
        if(count<MAX_COUNT)count++;
        status="R1B_STABLE_SCREEN_AND_VISIBLE_NAV_TAB_HINTS";
        Surface found=classify(stableScreen,mainActivity,feedTabSelected,clipsTabSelected,truncated);
        if(found==Surface.UNKNOWN || found==Surface.REELS_ORIGIN_UNKNOWN){
            int why=truncated?0:feedTabSelected && clipsTabSelected?1:
                "REELS".equals(stableScreen) && !mainActivity?3:
                "REELS".equals(stableScreen) && !feedTabSelected && !clipsTabSelected?2:
                "HOME_FEED".equals(stableScreen) && !feedTabSelected?4:
                "UNKNOWN".equals(stableScreen)?5:6;
            if(REASON_COUNTS[why]<MAX_COUNT)REASON_COUNTS[why]++;
        }
        if(truncated){
            if(skipped<MAX_INVALID)skipped++;
            notAvailable("VIEW_SCAN_LIMIT");return;
        }
        if(found!=pending){pending=found;pendingRepeats=1;return;}
        if(pendingRepeats<3)pendingRepeats++;
        if(pendingRepeats<2)return; // avoid reporting one-frame transient UI
        int cohort=R1CD10ComparisonFlow.cohort();
        if(found!=current){
            Surface previous=current;
            current=found;
            if(transitions<MAX_COUNT)transitions++;
            if(COUNTS[found.ordinal()]<MAX_COUNT)COUNTS[found.ordinal()]++;
            if(found==Surface.HOME_FEED)sawHome=true;
            String provenance=(found==Surface.REELS_ORIGIN_UNKNOWN && previous==Surface.HOME_FEED)
                ?"from_HOME_FEED_ROUTE_ONLY" :
                found==Surface.FEED_OPENED_REEL_CANDIDATE?"feed_tab_selected_CANDIDATE" :
                found==Surface.DEDICATED_REELS_TAB_HINT?"clips_tab_selected_HINT" :
                "stable_screen_only";
            event("SURFACE_CHANGED",previous.name()+"->"+found.name()+" evidence="+provenance);
            if(found==Surface.REELS_ORIGIN_UNKNOWN && ambiguousOrigin<MAX_COUNT)ambiguousOrigin++;
            if(found==Surface.FEED_OPENED_REEL_CANDIDATE && candidateOrigin<MAX_COUNT)candidateOrigin++;
            if(found==Surface.DEDICATED_REELS_TAB_HINT && explicitTabOrigin<MAX_COUNT)explicitTabOrigin++;
        }
        // A/B is a separate user-driven session. No autonomous switch of cohort.
        // Only count one feature observation when the active cohort starts, or
        // when a genuinely new stable screen transition is observed.
        if(R1CD09DiagnosticGate.active(R1CD09DiagnosticGate.Mode.AUTO) && cohort>=0) {
            if(cohort!=lastCohort || (current==found && lastObservedCohortSurface!=found)){
                lastCohort=cohort;lastObservedCohortSurface=found;
                if(A_B_COUNTS[cohort][found.ordinal()]<MAX_COUNT)
                    A_B_COUNTS[cohort][found.ordinal()]++;
                if(found!=Surface.UNKNOWN){
                    R1CD10ComparisonFlow.sample();
                    R1CD10ComparisonFlow.recordFeature(0,true);
                }
            }
        } else {lastCohort=-1;lastObservedCohortSurface=Surface.UNKNOWN;}
    }
    private static Surface lastObservedCohortSurface=Surface.UNKNOWN;
    static synchronized void notAvailable(String reason) {
        if("NO_SUPPORTED_APK".equals(reason) || "OBSERVER_INACTIVE".equals(reason)
            || "ACTIVITY_PAUSED".equals(reason) || "NO_ACTIVE_ACTIVITY".equals(reason)
            || "VIEW_SCAN_LIMIT".equals(reason)){
            status=reason;
            pending=Surface.UNKNOWN;pendingRepeats=0;
            lastObservedCohortSurface=Surface.UNKNOWN;
            if(current!=Surface.UNKNOWN){
                event("SURFACE_LOST","previous="+current.name()+" reason="+reason);
                current=Surface.UNKNOWN;
            }
        }
    }
    static synchronized void invalidatedByNewIntent() {
        if(invalidated<MAX_COUNT)invalidated++;
        pending=Surface.UNKNOWN;pendingRepeats=0;
        current=Surface.UNKNOWN;
        lastObservedCohortSurface=Surface.UNKNOWN;
        event("NEW_INTENT", "SURFACE_OR_ACCOUNT_NOT_VERIFIED");
    }
    static synchronized String brief() {
        return "D13_AUTO_SURFACE="+current+" hint_only=true "+
            "account_identity=NOT_VERIFIED latest_status="+status;
    }
    static synchronized String report() {
        StringBuilder b=new StringBuilder("D13 AUTOMATIC SURFACE/ACCOUNT-SWITCH DISCOVERY (PASSIVE READ-ONLY)\n");
        b.append("SAMPLER_STATUS=").append(status).append(" polls=").append(count)
            .append(" stable_transitions=").append(transitions)
            .append(" skipped_budget=").append(skipped)
            .append(" new_intent_invalidations=").append(invalidated).append('\n');
        b.append("CURRENT_SURFACE=").append(current.name())
            .append(" SURFACE_EVIDENCE_ONLY ACCOUNT_IDENTITY=NOT_VERIFIED")
            .append(" MEDIA_IDENTITY=NOT_VERIFIED ACTIONS=OFF\n");
        for(Surface s:Surface.values())b.append(" ").append(s.name())
            .append(" transition_entries=").append(COUNTS[s.ordinal()])
            .append(" manual_A=").append(A_B_COUNTS[0][s.ordinal()])
            .append(" manual_B=").append(A_B_COUNTS[1][s.ordinal()]).append('\n');
        b.append("UNRESOLVED_EVIDENCE_REASONS (sample counts, not unique UI owners)\n");
        for(int i=0;i<REASONS.length;i++)
            b.append(' ').append(REASONS[i]).append('=').append(REASON_COUNTS[i]).append('\n');
        b.append("ORIGIN_HINTS explicit_clips_tab=").append(explicitTabOrigin)
            .append(" feed_tab_candidate=").append(candidateOrigin)
            .append(" unresolved_reels=").append(ambiguousOrigin)
            .append(" recent_home_seen=").append(sawHome).append('\n');
        b.append("D13_EVENT_ORDER limit=").append(MAX_EVENTS)
            .append(" dropped_oldest=").append(dropped).append('\n');
        for(String e:EVENTS)b.append(e).append('\n');
        b.append("Reels opened from Feed remains CANDIDATE unless an exact origin owner is verified. ")
            .append("Stories use R1B anchored screen. Session-reference changes are recorded in D09/D12, ")
            .append("never equal verified account changes. No account auto-rearm, media IDs, captions, ")
            .append("View strings, thumbnails or network retained.\n");
        return b.toString();
    }
    static synchronized void resetForTest(){
        Arrays.fill(COUNTS,0);Arrays.fill(REASON_COUNTS,0);
        for(int[] a:A_B_COUNTS)Arrays.fill(a,0);
        EVENTS.clear();current=pending=lastObservedCohortSurface=Surface.UNKNOWN;
        lastCohort=-1;pendingRepeats=count=transitions=dropped=invalidated=0;
        ambiguousOrigin=candidateOrigin=explicitTabOrigin=skipped=0;sawHome=false;ordinal=0;
        status="NO_SUPPORTED_ACTIVITY_SAMPLES";
    }
}
