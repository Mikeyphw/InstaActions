package com.mikeyphw.instaactions;

/** Deterministic pure-Java tests: no Instagram, Android hooks, credentials, or
 * Activity references. Each assertion is against text-free event accounting. */
public final class R1CD12LifecycleSmoke {
    private static int checks;
    private static void check(boolean ok,String reason) {
        if(!ok)throw new AssertionError(reason);
        checks++;
    }
    public static void main(String[] args) {
        R1CD12AccountLifecycle.resetForTest();
        String r=R1CD12AccountLifecycle.report();
        check(r.contains("ACCOUNT_IDENTITY=NOT_VERIFIED"),"initial identity must be unverified");
        check(r.contains("AUTO_REARM=FORBIDDEN"),"auto rearm forbidden");
        check(r.contains("NO_MANUAL_SWITCH_MARKER"),"initial marker status");
        check(r.contains("NOT_REGISTERED"),"initial framework observation missing");
        R1CD12AccountLifecycle.setLifecycleStatus(true);
        R1CD12AccountLifecycle.setIntentHookStatus("READY_2_AND_3_ARGUMENT_OVERLOADS");
        R1CD12AccountLifecycle.lifecycleResumed();
        R1CD12AccountLifecycle.lifecyclePaused();
        R1CD12AccountLifecycle.lifecycleDestroyed();
        R1CD12AccountLifecycle.screenHint("REELS");
        R1CD12AccountLifecycle.screenHint("REELS");
        R1CD12AccountLifecycle.screenHint("HOME_FEED");
        r=R1CD12AccountLifecycle.report();
        check(r.contains("ACTIVITY_RESUMED total=1 before=1 after=0"),"lifecycle before marker");
        check(r.contains("ACTIVITY_PAUSED total=1 before=1 after=0"),"pause before marker");
        check(r.contains("ACTIVITY_DESTROYED total=1 before=1 after=0"),"destroy before marker");
        check(r.contains("SCREEN_HINT_CHANGED total=2 before=2 after=0"),"screen hints dedup");
        check(r.contains("READY_2_AND_3_ARGUMENT_OVERLOADS"),"intent hook status");
        check(!R1CD07Recovery.isEnabled(),"no autostart recovery");
        R1CD12AccountLifecycle.markManualSwitch();
        R1CD12AccountLifecycle.newIntentDelivered();
        R1CD12AccountLifecycle.lifecycleResumed();
        R1CD12AccountLifecycle.observedSessionReferenceChange();
        r=R1CD12AccountLifecycle.report();
        check(r.contains("NEW_INTENT_DELIVERED total=1 before=0 after=1"),"intent after marker");
        check(r.contains("USERSESSION_REFERENCE_CHANGED total=1 before=0 after=1"),"native ref change after marker");
        check(r.contains("D12_SIGNAL_STATUS=OPAQUE_SESSION_REFERENCE_CHANGED_NOT_ACCOUNT_IDENTITY"),"not account id");
        check(r.contains("MANUAL_SWITCH_MARKER phase=AFTER_MANUAL_MARKER"),"marker visible");
        check(r.contains("ACTIVITY_RESUMED total=2 before=1 after=1"),"event order relative to marker");
        check(r.contains("new_intents_disarmed_repost=0"),"no false disarm while already off");
        check(!R1CD07Recovery.isEnabled(),"intent cannot arm");
        // Actual sentinel should feed D12 with a change but no identity fields.
        R1CD12AccountLifecycle.resetForTest();
        R1CD09AccountSentinel.clear();
        Object a=new Object(),b=new Object();
        R1CD09AccountSentinel.observe(a);
        R1CD09AccountSentinel.markManualSwitch();
        R1CD09AccountSentinel.observe(b);
        r=R1CD12AccountLifecycle.report();
        check(r.contains("USERSESSION_REFERENCE_CHANGED total=1 before=0 after=1"),"sentinel integration");
        check(r.contains("OPAQUE_SESSION_REFERENCE_CHANGED_NOT_ACCOUNT_IDENTITY"),"sentinel identity caution");
        check(R1CD09AccountSentinel.summary().contains("REFERENCE_CHANGED_AFTER_MARKER"),"D09 chronology kept");
        // The bounded timeline must disclose loss explicitly.
        R1CD12AccountLifecycle.resetForTest();
        for(int i=0;i<90;i++)R1CD12AccountLifecycle.lifecycleResumed();
        r=R1CD12AccountLifecycle.report();
        check(r.contains("ACTIVITY_RESUMED total=90 before=90 after=0"),"counters survive timeline eviction");
        check(r.contains("dropped_oldest=26"),"eviction explicit");
        check(r.contains("#90 ACTIVITY_RESUMED"),"latest event available");
        check(!r.contains("#1 ACTIVITY_RESUMED"),"oldest evicted");
        // Unapproved/unknown hook states must not be promoted to readiness.
        R1CD12AccountLifecycle.setIntentHookStatus("MADE_UP_ENABLED");
        check(r.contains("INSTRUMENTATION_NEW_INTENT=NOT_INSTALLED"),"previous status not ready");
        r=R1CD12AccountLifecycle.report();
        check(r.contains("INSTRUMENTATION_NEW_INTENT=NOT_INSTALLED"),"unknown status ignored");
        R1CD12AccountLifecycle.setIntentHookStatus("UNAVAILABLE_FAIL_CLOSED");
        check(R1CD12AccountLifecycle.report().contains("UNAVAILABLE_FAIL_CLOSED"),"unavailable shown");
        System.out.println("INA_D12_ACCOUNT_LIFECYCLE_NEGATIVES_PASS="+checks);
    }
}
