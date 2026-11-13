package com.mikeyphw.instaactions;

/** Safety regression for one-mode-at-a-time, weak account signal and session toggle. */
public final class R1CD09SessionAndModesSmoke {
    private static int checks;
    private static long now=100_000_000_000L;
    private static void ok(boolean condition,String name){if(!condition)throw new AssertionError(name);checks++;}
    private static void fresh() {
        R1CD07Recovery.off("test reset");
        R1CD07ExactGateTrial.reset();
        R1CD07ExactGateTrial.setDeoptimized(true);
        R1CD07ExactGateTrial.onCaptureStart(0);
        for(int i=0;i<12;i++)R1CD07ExactGateTrial.recordFrame(0,
                R1CD07BranchInputs.PRESENT,new int[]{-1,1},0);
    }
    public static void main(String[] args) {
        R1CD07Recovery.setClockForTest(()->now);
        R1CD09AccountSentinel.clear();
        ok(R1CD09DiagnosticGate.begin(R1CD09DiagnosticGate.Mode.AB).startsWith("STARTED"),"start A/B");
        ok(R1CD09DiagnosticGate.begin(R1CD09DiagnosticGate.Mode.FEED).startsWith("REFUSED"),"cannot mix modes");
        ok(R1CD09DiagnosticGate.active(R1CD09DiagnosticGate.Mode.AB),"A/B remains selected");
        Object a=new Object(),b=new Object();
        R1CD09AccountSentinel.observe(a);
        R1CD09AccountSentinel.observe(a);
        ok(R1CD09AccountSentinel.summary().contains("same_ref=1"),"same reference bucket");
        R1CD09DiagnosticGate.expectManualABSwitch();
        R1CD09AccountSentinel.observe(b);
        ok(R1CD09AccountSentinel.summary().contains("changed_ref=1"),"changed reference bucket");
        ok(R1CD09DiagnosticGate.active(R1CD09DiagnosticGate.Mode.AB),"explicit A/B handoff allowed");
        ok(R1CD09DiagnosticGate.finish().startsWith("FINISHED"),"finish A/B");
        ok(R1CD09DiagnosticGate.begin(R1CD09DiagnosticGate.Mode.FEED).startsWith("STARTED"),"switch after finish");
        R1CD09AccountSentinel.observe(new Object());
        ok(R1CD09DiagnosticGate.state()==R1CD09DiagnosticGate.State.ABORTED,"unexpected transition aborts Feed");
        ok(R1CD09DiagnosticGate.begin(R1CD09DiagnosticGate.Mode.ACCOUNT).startsWith("STARTED"),"account diagnostic started");
        R1CD09AccountSentinel.observe(new Object());
        ok(R1CD09DiagnosticGate.active(R1CD09DiagnosticGate.Mode.ACCOUNT),"account diagnostic retains changes as evidence");
        ok(R1CD09DiagnosticGate.abort("manual").startsWith("ABORTED"),"explicit abort");
        R1CD09AccountSentinel.clear();
        ok(R1CD09AccountSentinel.summary().contains("NOT_OBSERVED"),"no callback is unknown");
        fresh();
        ok(R1CD07Recovery.armSession(0,true,true).startsWith("ENABLED"),"session arm");
        ok(R1CD07Recovery.isSessionMode(),"session-mode flag");
        now+=R1CD07Recovery.LEASE_NS*3;
        ok(R1CD07Recovery.isEnabled(),"no 90-second timer in explicit foreground session");
        long epoch=R1CD07Recovery.epoch();
        ok(Boolean.TRUE.equals(R1CD07Recovery.maybeOverride(epoch,0,
                R1CD07BranchInputs.PRESENT,false,Boolean.FALSE)),"eligible false native result changed");
        ok(!R1CD07Recovery.isSessionMode() == false,"still in session mode");
        ok(Boolean.TRUE.equals(R1CD07Recovery.maybeOverride(R1CD07Recovery.epoch(),0,
                R1CD07BranchInputs.PRESENT,false,Boolean.TRUE)),"native TRUE unchanged");
        ok(!R1CD07Recovery.isEnabled(),"native predicate change disarms long session");
        fresh();R1CD07Recovery.armSession(0,true,true);
        Object p=new Object(),q=new Object();
        R1CD09AccountSentinel.observe(p);
        R1CD09AccountSentinel.observe(q);
        ok(!R1CD07Recovery.isEnabled(),"UserSession reference transition disarms");
        fresh();R1CD07Recovery.armSession(0,true,true);
        for(int i=0;i<R1CD07Recovery.SESSION_LIMIT;i++) {
            if(!Boolean.TRUE.equals(R1CD07Recovery.maybeOverride(
                    R1CD07Recovery.epoch(),0,R1CD07BranchInputs.PRESENT,
                    false,Boolean.FALSE)))throw new AssertionError("session cap iteration "+i);
        }
        ok(!R1CD07Recovery.isEnabled(),"4096-call session cap disarms");
        ok(Boolean.FALSE.equals(R1CD07Recovery.maybeOverride(
                    R1CD07Recovery.epoch(),0,R1CD07BranchInputs.PRESENT,
                    false,Boolean.FALSE)),"beyond call cap stays native");
        fresh();R1CD07Recovery.armSession(0,true,true);
        R1CD07Recovery.onActivityExit();
        ok(!R1CD07Recovery.isEnabled(),"activity exit disarms");
        fresh();R1CD07Recovery.armSession(0,true,true);
        R1CD07Recovery.off("user OFF");
        ok(!R1CD07Recovery.isEnabled(),"user OFF disarms");
        R1CD07Recovery.restoreClockForTest();
        System.out.println("INA_D09_SESSION_AND_MODE_NEGATIVES_PASS="+checks);
    }
}
