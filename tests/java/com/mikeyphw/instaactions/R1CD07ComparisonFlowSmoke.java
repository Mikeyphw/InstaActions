package com.mikeyphw.instaactions;

/** Android-independent A/B control state and preserved-count regression. */
public final class R1CD07ComparisonFlowSmoke {
    private static int n;
    private static void ok(boolean yes) {if(!yes)throw new AssertionError("case " + n); n++;}
    public static void main(String[] args) {
        ok(!R1CD07ComparisonFlow.canStopA(false, -1, false));
        ok(R1CD07ComparisonFlow.canStopA(true, 0, false));
        ok(!R1CD07ComparisonFlow.canStopA(true, 1, false));
        ok(!R1CD07ComparisonFlow.canStopA(true, 0, true));
        ok(!R1CD07ComparisonFlow.canStartB(true, 0, 10, false, true));
        ok(!R1CD07ComparisonFlow.canStartB(false, -1, 0, false, true));
        ok(!R1CD07ComparisonFlow.canStartB(false, -1, 1, true, true));
        ok(!R1CD07ComparisonFlow.canStartB(false, -1, 1, false, false));
        ok(!R1CD07ComparisonFlow.canStartB(false, 0, 1, false, true));
        ok(R1CD07ComparisonFlow.canStartB(false, -1, 5, false, true));
        ok(R1CD07ComparisonFlow.canStopB(true, 1, false));
        ok(!R1CD07ComparisonFlow.canStopB(true, 0, false));
        ok(!R1CD07ComparisonFlow.canStopB(false, -1, false));
        ok(!R1CD07ComparisonFlow.canStopB(true, 1, true));
        ok(R1CD07ComparisonFlow.progress(true,0,6,0,false).contains("Stop A"));
        ok(R1CD07ComparisonFlow.progress(false,-1,6,0,false).contains("Start B"));
        ok(R1CD07ComparisonFlow.progress(true,1,6,4,false).contains("Stop B"));
        ok(R1CD07ComparisonFlow.progress(false,-1,6,4,false).contains("Copy combined"));
        ok(R1CD07ComparisonFlow.progress(false,-1,6,0,true).contains("Recovery OFF"));
        R1CD07TraceSummary capture = new R1CD07TraceSummary();
        capture.start(0);
        capture.record(4, Boolean.FALSE);
        capture.record(12, Boolean.FALSE);
        ok(capture.eventsFor(0)==2);
        capture.stop();
        ok(!capture.recording() && capture.eventsFor(0)==2);
        capture.start(1);
        capture.record(4, Boolean.TRUE);
        capture.record(11, null);
        capture.stop();
        ok(capture.eventsFor(0)==2 && capture.eventsFor(1)==2);
        String both=capture.render();
        ok(both.contains("ACCOUNT A") && both.contains("ACCOUNT B"));
        ok(both.contains("samples=1 true=0 false=1"));
        ok(both.contains("samples=1 true=1 false=0"));
        capture.clear();
        ok(capture.eventsFor(0)==0 && capture.eventsFor(1)==0);
        ok(!capture.recording());
        System.out.println("INA_D07_UX03_AB_FLOW_PASS="+n);
    }
}
