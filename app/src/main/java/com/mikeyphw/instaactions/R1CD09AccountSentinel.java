package com.mikeyphw.instaactions;

import java.lang.ref.WeakReference;

/** Observes ONLY reference identity of source-verified X.02xy.A04(UserSession,String).
 * Neither user IDs, usernames, token fields, nor the String argument are read.
 * Absence of callbacks is UNVERIFIED, never evidence that the account is stable.
 */
final class R1CD09AccountSentinel {
    private static WeakReference<Object> previous=new WeakReference<>(null);
    private static int observed, same, changed, missing, reclaimed;
    private static boolean seen;
    private static final int[][] AB_COUNTS=new int[2][4]; // callback same, changed, missing, reclaimed
    private static boolean manualSwitchMarker;
    private static int callbacksBeforeSwitch, callbacksAfterSwitch, markedSwitches, noCallbackAfterSwitch;
    private static final int MAX_EVENTS=48;
    private static final java.util.ArrayDeque<String> timeline=new java.util.ArrayDeque<>();
    private static long ordinal;
    private static int timelineDropped;
    private static void event(String kind){
        // Event type + ordered ordinal + active A/B phase only: never raw objects,
        // account identifiers, strings, names or timestamps.
        String v="#"+(++ordinal)+" "+kind+" phase="+R1CD10ComparisonFlow.stage();
        if(timeline.size()>=MAX_EVENTS){timeline.removeFirst();timelineDropped++;}
        timeline.addLast(v);
    }
    private R1CD09AccountSentinel() { }
    static synchronized void observe(Object userSession) {
        if(userSession==null){missing++;event("SESSION_MISSING");int a=R1CD10ComparisonFlow.cohort();if(a>=0)AB_COUNTS[a][2]++;return;}
        R1CD10PassiveLedger.observe(R1CD10PassiveLedger.Signal.SESSION_CALLBACK);
        if(manualSwitchMarker)callbacksAfterSwitch++;
        if(R1CD09DiagnosticGate.active(R1CD09DiagnosticGate.Mode.ACCOUNT))
            R1CD10ComparisonFlow.recordFeature(0,false);
        R1CD10ComparisonFlow.sample();
        Object old=previous.get();
        observed++;
        if(seen && old!=null) {
            if(old==userSession){same++;int a=R1CD10ComparisonFlow.cohort();if(a>=0)AB_COUNTS[a][0]++;}
            else {
                changed++;event(manualSwitchMarker?"REFERENCE_CHANGED_AFTER_MARKER":"REFERENCE_CHANGED_BEFORE_MARKER");
                R1CD12AccountLifecycle.observedSessionReferenceChange();
                int a=R1CD10ComparisonFlow.cohort();if(a>=0)AB_COUNTS[a][1]++;
                R1CD07Recovery.off("UserSession reference changed; recovery disarmed");
                R1CD09DiagnosticGate.accountReferenceChanged();
            }
        } else if(seen){reclaimed++;event("WEAK_REFERENCE_RECLAIMED");int a=R1CD10ComparisonFlow.cohort();if(a>=0)AB_COUNTS[a][3]++;}
        seen=true;
        previous=new WeakReference<>(userSession);
    }
    static synchronized void markManualSwitch() {
        markedSwitches++;callbacksBeforeSwitch=observed;callbacksAfterSwitch=0;manualSwitchMarker=true;
        event("MANUAL_SWITCH_MARKER_NOT_IDENTITY_PROOF");
        R1CD12AccountLifecycle.markManualSwitch();
    }
    static synchronized void clear() {
        previous.clear();previous=new WeakReference<>(null);
        observed=0;same=0;changed=0;missing=0;reclaimed=0;seen=false;
        manualSwitchMarker=false;callbacksBeforeSwitch=0;callbacksAfterSwitch=0;markedSwitches=0;noCallbackAfterSwitch=0;
        for(int a=0;a<2;a++)java.util.Arrays.fill(AB_COUNTS[a],0);
        timeline.clear();ordinal=0;timelineDropped=0;
    }
    static synchronized boolean hasObserved(){return observed>0;}
    static synchronized String summary() {
        return "D09 ACCOUNT CHANGE SIGNAL (read-only) observed="+observed+
            " same_ref="+same+" changed_ref="+changed+" missing="+missing+
            " reclaimed="+reclaimed+" manual_switch_markers="+markedSwitches+
            " callbacks_before_marker="+callbacksBeforeSwitch+" callbacks_after_marker="+callbacksAfterSwitch+
            " switch_coverage="+(manualSwitchMarker && callbacksAfterSwitch==0?"NO_CALLBACK_AFTER_SWITCH":
               manualSwitchMarker?"CALLBACK_AFTER_SWITCH_NOT_IDENTITY_PROOF":"NO_MANUAL_SWITCH_MARKER")+
            " status="+(changed>0?"REFERENCE_CHANGED":
                observed==0?"NOT_OBSERVED":"OBSERVED_NO_CHANGE_IN_CALLBACKS")+
            "\nA callbacks same/changed/missing/reclaimed="+AB_COUNTS[0][0]+"/"+AB_COUNTS[0][1]+"/"+AB_COUNTS[0][2]+"/"+AB_COUNTS[0][3]+
            " B callbacks same/changed/missing/reclaimed="+AB_COUNTS[1][0]+"/"+AB_COUNTS[1][1]+"/"+AB_COUNTS[1][2]+"/"+AB_COUNTS[1][3]+
            "\nACCOUNT_EVENT_ORDER (bounded) dropped_oldest="+timelineDropped+"\n"+
            String.join("\n",timeline)+"\n"+
            "UserSession reference is weak and opaque; reference change is a signal, NOT verified identity. "+
            "No callback does NOT prove same account. OFF manually before switching; hook may not run on every switch.\n";
    }
}
