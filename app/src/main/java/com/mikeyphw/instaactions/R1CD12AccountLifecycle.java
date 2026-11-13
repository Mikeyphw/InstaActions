package com.mikeyphw.instaactions;

import java.util.ArrayDeque;

/** D12: account-SWITCH CORRELATION, never identity. Framework lifecycle evidence
 * is independent of the Repost UserSession predicate. No Intent contents, user
 * IDs, activities, account objects, usernames or timestamps are retained.
 */
final class R1CD12AccountLifecycle {
    enum Event { ACTIVITY_RESUMED, ACTIVITY_PAUSED, ACTIVITY_DESTROYED,
        NEW_INTENT_DELIVERED, SCREEN_HINT_CHANGED,
        MANUAL_SWITCH_MARKER, USERSESSION_REFERENCE_CHANGED }
    private static final int LIMIT = 64;
    private static final long MAX_COUNT = 100000;
    private static final long[] counts = new long[Event.values().length];
    private static final long[][] phaseCounts = new long[2][Event.values().length];
    private static final ArrayDeque<String> timeline = new ArrayDeque<>();
    private static long ordinal, dropped, capped, markerOrdinal;
    private static int newIntentsDisarmed;
    private static String lastHint = "UNOBSERVED";
    private static boolean markerSeen;
    private static String intentHookStatus="NOT_INSTALLED";
    private static String lifecycleStatus="NOT_REGISTERED";
    private R1CD12AccountLifecycle() { }

    static synchronized void setIntentHookStatus(String status) {
        if ("READY_2_AND_3_ARGUMENT_OVERLOADS".equals(status) ||
            "READY_2_ARGUMENT_ONLY".equals(status) ||
            "READY_3_ARGUMENT_ONLY".equals(status) ||
            "UNAVAILABLE_FAIL_CLOSED".equals(status)) intentHookStatus=status;
    }
    static synchronized void setLifecycleStatus(boolean installed) {
        lifecycleStatus=installed?"REGISTERED_ANDROID_APPLICATION_CALLBACKS":"UNAVAILABLE";
    }
    private static void record(Event type) {
        int idx=type.ordinal();
        if(counts[idx]<MAX_COUNT)counts[idx]++;else capped++;
        int phase=markerSeen?1:0;
        if(phaseCounts[phase][idx]<MAX_COUNT)phaseCounts[phase][idx]++;else capped++;
        String value="#"+(++ordinal)+" "+type.name()+" phase="+
            (markerSeen?"AFTER_MANUAL_MARKER":"BEFORE_MANUAL_MARKER");
        if(timeline.size()>=LIMIT){timeline.removeFirst();dropped++;}
        timeline.addLast(value);
    }
    static synchronized void markManualSwitch() {
        markerSeen=true;
        markerOrdinal=ordinal+1;
        record(Event.MANUAL_SWITCH_MARKER);
    }
    static synchronized void lifecycleResumed() {record(Event.ACTIVITY_RESUMED);}
    static synchronized void lifecyclePaused() {record(Event.ACTIVITY_PAUSED);}
    static synchronized void lifecycleDestroyed() {record(Event.ACTIVITY_DESTROYED);}
    static synchronized void newIntentDelivered() {
        record(Event.NEW_INTENT_DELIVERED);
        // A new Intent changes navigation context. It is NOT account identity.
        // Fail closed without reading any Intent extras/action/data.
        if(R1CD07Recovery.isEnabled()){
            R1CD07Recovery.off("New Intent delivered: account/context NOT VERIFIED");
            if(newIntentsDisarmed<100000)newIntentsDisarmed++;
        }
    }
    static synchronized void observedSessionReferenceChange() {
        record(Event.USERSESSION_REFERENCE_CHANGED);
        // The original D09 sentinel, not this ledger, enforces recovery OFF.
    }
    static synchronized void screenHint(String value) {
        String bucket="REELS".equals(value)?"REELS":
            "HOME_FEED".equals(value)?"HOME_FEED":"OTHER_OR_UNKNOWN";
        if(!bucket.equals(lastHint)){
            lastHint=bucket;
            record(Event.SCREEN_HINT_CHANGED);
        }
    }
    static synchronized String report() {
        StringBuilder b=new StringBuilder("D12 INDEPENDENT ACCOUNT-SWITCH / LIFECYCLE SIGNALS (PASSIVE)\n");
        b.append("ANDROID_LIFECYCLE=").append(lifecycleStatus)
            .append(" INSTRUMENTATION_NEW_INTENT=").append(intentHookStatus).append('\n')
            .append("Manual marker seen=").append(markerSeen)
            .append(" marker_ordinal=").append(markerOrdinal)
            .append(" new_intents_disarmed_repost=").append(newIntentsDisarmed).append('\n');
        for(Event e:Event.values()){
            int i=e.ordinal();
            b.append(' ').append(e.name()).append(" total=").append(counts[i])
                .append(" before=").append(phaseCounts[0][i])
                .append(" after=").append(phaseCounts[1][i]).append('\n');
        }
        b.append("D12_SIGNAL_STATUS=")
            .append(counts[Event.USERSESSION_REFERENCE_CHANGED.ordinal()]>0
                ?"OPAQUE_SESSION_REFERENCE_CHANGED_NOT_ACCOUNT_IDENTITY":
                markerSeen && phaseCounts[1][Event.NEW_INTENT_DELIVERED.ordinal()]>0
                  ?"NEW_INTENT_AFTER_MARKER_NOT_ACCOUNT_IDENTITY":
                  markerSeen?"MANUAL_SWITCH_UNCORROBORATED":"NO_MANUAL_SWITCH_MARKER")
            .append(" ACCOUNT_IDENTITY=NOT_VERIFIED AUTO_REARM=FORBIDDEN\n")
            .append("D12_EVENT_ORDER bounded=").append(LIMIT)
            .append(" dropped_oldest=").append(dropped).append(" capped_counters=").append(capped).append('\n');
        for(String event:timeline)b.append(event).append('\n');
        b.append("Lifecycle and new-intent delivery are independent of Repost but are NOT proof of an account switch. ")
         .append("A same-Activity switch may emit neither. No Intent content, identity, tokens or media read.\n");
        return b.toString();
    }
    static synchronized void resetForTest() {
        for(int i=0;i<counts.length;i++)counts[i]=0;
        for(long[] c:phaseCounts)java.util.Arrays.fill(c,0);
        timeline.clear();ordinal=0;markerOrdinal=0;dropped=0;capped=0;newIntentsDisarmed=0;
        markerSeen=false;lastHint="UNOBSERVED";
        intentHookStatus="NOT_INSTALLED";lifecycleStatus="NOT_REGISTERED";
    }
}
