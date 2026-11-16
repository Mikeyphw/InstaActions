package com.mikeyphw.instaactions;

/** Synthetic fail-closed A/B and process archive regression; no Android dependencies. */
public final class R1CD10UniversalSmoke {
    static int n;
    static void ok(boolean value,String msg){if(!value)throw new AssertionError(msg);n++;}
    public static void main(String[] args){
        ok(R1CD09DiagnosticGate.begin(R1CD09DiagnosticGate.Mode.PASSIVE).startsWith("REFUSED"),"passive has no Begin");
        ok(R1CD09DiagnosticGate.begin(R1CD09DiagnosticGate.Mode.FEED).startsWith("STARTED"),"feed begin");
        R1CD10ComparisonFlow.begin(R1CD09DiagnosticGate.Mode.FEED);
        ok(R1CD10ComparisonFlow.stage()==R1CD10ComparisonFlow.Stage.READY_A,"initial state");
        ok(R1CD10ComparisonFlow.startB().startsWith("REFUSED"),"no B before A");
        ok(R1CD10ComparisonFlow.stopA().startsWith("REFUSED"),"no A stop before start");
        ok(R1CD10ComparisonFlow.startA().startsWith("A_RECORDING"),"start A");
        ok(R1CD10ComparisonFlow.startA().startsWith("REFUSED"),"duplicate start A");
        R1CD10ComparisonFlow.sample();R1CD10ComparisonFlow.sample();
        ok(R1CD10ComparisonFlow.cohort()==0,"A cohort");
        ok(R1CD10ComparisonFlow.stopA().startsWith("A_STOPPED"),"stop A");
        ok(R1CD10ComparisonFlow.summary().contains("A_observations=2"),"A samples preserved");
        ok(R1CD10ComparisonFlow.startB().startsWith("REFUSED"),"manual switch ack required");
        ok(R1CD10ComparisonFlow.confirmSwitch().startsWith("MANUAL_SWITCH_ACKNOWLEDGED_NOT_VERIFIED"),"manual ack explicit");
        ok(R1CD10ComparisonFlow.startB().startsWith("B_RECORDING"),"start B");
        R1CD10ComparisonFlow.sample();
        ok(R1CD10ComparisonFlow.cohort()==1,"B cohort");
        ok(R1CD10ComparisonFlow.stopB().startsWith("B_STOPPED"),"stop B");
        ok(R1CD10ComparisonFlow.complete(),"B completion required");
        ok(R1CD10ComparisonFlow.summary().contains("B_observations=1"),"B samples preserved");
        ok(R1CD09DiagnosticGate.begin(R1CD09DiagnosticGate.Mode.AVATAR).startsWith("REFUSED"),"no overlapping mode");
        ok(R1CD09DiagnosticGate.finish().startsWith("FINISHED"),"finish once");
        R1CD10ReportArchive.capture("FEED","FINISHED","safe A/B counters");
        R1CD10PassiveLedger.observe(R1CD10PassiveLedger.Signal.UI_REPOST);
        R1CD10PassiveLedger.observeScreen("REELS");
        R1CD10PassiveLedger.observeScreen("REELS");
        String all=R1CD10ReportArchive.all("PASSIVE", "live current");
        ok(all.contains("safe A/B counters"),"archived result retained");
        ok(all.contains("live current"),"current result retained");
        ok(all.contains("UI_REPOST=1"),"passive event counted");
        ok(all.contains("REELS_HINT=1"),"passive screen deduplicated");
        ok(all.contains("NOT full Android logcat"),"scope stated honestly");
        ok(R1CD09DiagnosticGate.begin(R1CD09DiagnosticGate.Mode.AVATAR).startsWith("STARTED"),"avatar after finish");
        R1CD10ComparisonFlow.begin(R1CD09DiagnosticGate.Mode.AVATAR);
        R1CD10ComparisonFlow.startA();
        R1CD10ComparisonFlow.abort();
        ok(!R1CD10ComparisonFlow.complete(),"abort not a complete comparison");
        ok(R1CD09DiagnosticGate.abort("incomplete").startsWith("ABORTED"),"abort clears mode lock");
        ok(R1CD10ComparisonFlow.startB().startsWith("REFUSED"),"aborted B refused");
        ok(R1CD09DiagnosticGate.begin(R1CD09DiagnosticGate.Mode.ACCOUNT).startsWith("STARTED"),"account after abort");
        R1CD10ComparisonFlow.begin(R1CD09DiagnosticGate.Mode.ACCOUNT);
        R1CD09AccountSentinel.clear();
        R1CD10ComparisonFlow.startA();
        Object a=new Object();R1CD09AccountSentinel.observe(a);R1CD09AccountSentinel.observe(a);
        R1CD10ComparisonFlow.stopA();R1CD09DiagnosticGate.expectManualABSwitch();R1CD09AccountSentinel.markManualSwitch();
        ok(R1CD09AccountSentinel.summary().contains("NO_CALLBACK_AFTER_SWITCH"),"missing post switch callback distinct");
        R1CD10ComparisonFlow.confirmSwitch();R1CD10ComparisonFlow.startB();
        R1CD09AccountSentinel.observe(new Object());
        ok(R1CD09DiagnosticGate.active(R1CD09DiagnosticGate.Mode.ACCOUNT),"account transition diagnostic stays open");
        ok(R1CD09AccountSentinel.summary().contains("CALLBACK_AFTER_SWITCH_NOT_IDENTITY_PROOF"),"positive callback still not verified identity");
        R1CD09DiagnosticGate.abort("done");
        ok(R1CD09DiagnosticGate.begin(R1CD09DiagnosticGate.Mode.REELS).startsWith("STARTED"),"Reels start");
        R1CD10ComparisonFlow.begin(R1CD09DiagnosticGate.Mode.REELS);
        R1CD10ComparisonFlow.startA();R1CD10ComparisonFlow.stopA();
        R1CD09DiagnosticGate.expectManualABSwitch();
        R1CD09DiagnosticGate.accountReferenceChanged();
        ok(R1CD09DiagnosticGate.active(R1CD09DiagnosticGate.Mode.REELS),"manual Reels A to B signal allowed with recovery OFF");
        R1CD09DiagnosticGate.abort("done");
        System.out.println("INA_D10_UNIVERSAL_AB_PASSIVE_EXPORT_NEGATIVES_PASS="+n);
    }
}
