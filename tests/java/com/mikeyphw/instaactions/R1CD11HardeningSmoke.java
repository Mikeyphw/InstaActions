package com.mikeyphw.instaactions;

/** Offline fail-closed D11 diagnostic-quality negative tests, no Android runtime. */
public final class R1CD11HardeningSmoke {
    private static int assertions;
    private static void check(boolean yes,String reason) {
        assertions++;
        if(!yes)throw new AssertionError(reason);
    }
    private static void start(R1CD09DiagnosticGate.Mode mode) {
        if(R1CD09DiagnosticGate.running())R1CD09DiagnosticGate.abort("test cleanup");
        check(R1CD09DiagnosticGate.begin(mode).startsWith("STARTED"),"mode start");
        R1CD10ComparisonFlow.begin(mode);
    }
    private static void toB() {
        check(R1CD10ComparisonFlow.stopA().startsWith("A_STOPPED"),"Stop A");
        check(R1CD10ComparisonFlow.confirmSwitch().startsWith("MANUAL_SWITCH"),"manual transition");
        check(R1CD10ComparisonFlow.startB().startsWith("B_RECORDING"),"Start B");
    }
    private static void finish() {
        check(R1CD10ComparisonFlow.stopB().startsWith("B_STOPPED"),"Stop B");
        R1CD10ComparisonFlow.finish();
        R1CD09DiagnosticGate.finish();
    }
    public static void main(String[] args) {
        start(R1CD09DiagnosticGate.Mode.FEED);
        check(R1CD10ComparisonFlow.startA().startsWith("A_RECORDING"),"start A");
        for(int i=0;i<120;i++)R1CD10ComparisonFlow.sample();
        toB();
        for(int i=0;i<130;i++)R1CD10ComparisonFlow.sample();
        check(R1CD10ComparisonFlow.stopB().startsWith("B_STOPPED"),"stop B");
        check(!R1CD10ComparisonFlow.hasFeatureCoverage(),"callback counts must not qualify Feed");
        check(R1CD10ComparisonFlow.quality().startsWith("INCOMPLETE"),"missing explicit Feed scans");
        check(R1CD10ComparisonFlow.missingFeatureHint().contains("A/1"),"missing per surface A");
        check(R1CD10ComparisonFlow.missingFeatureHint().contains("B/3"),"missing per surface B");
        R1CD10ComparisonFlow.finish();R1CD09DiagnosticGate.finish();

        start(R1CD09DiagnosticGate.Mode.FEED);
        R1CD10ComparisonFlow.startA();
        for(int i=0;i<3;i++)R1CD10ComparisonFlow.recordFeature(i, i%2==0);
        toB();
        for(int i=0;i<3;i++)R1CD10ComparisonFlow.recordFeature(i, true);
        check(R1CD10ComparisonFlow.stopB().startsWith("B_STOPPED"),"stop B paired");
        check(R1CD10ComparisonFlow.hasFeatureCoverage(),"per-surface coverage");
        check(R1CD10ComparisonFlow.quality().contains("MISMATCH"),"one mismatched screen makes pair unqualified");
        check(R1CD10ComparisonFlow.summary().contains("A_explicit=[1, 1, 1]"),"explicit A counts");
        check(R1CD10ComparisonFlow.summary().contains("B_screen_hint=[1, 1, 1]"),"hint counts");
        R1CD10ComparisonFlow.finish();R1CD09DiagnosticGate.finish();
        check(R1CD10ComparisonFlow.quality().contains("MISMATCH"),
                "post-Finish Copy Single must retain saved quality");

        start(R1CD09DiagnosticGate.Mode.AVATAR);
        R1CD10ComparisonFlow.startA();
        R1CD10ComparisonFlow.recordFeature(0,false);
        R1CD10ComparisonFlow.recordFeature(1,false);
        toB();
        for(int i=0;i<3;i++)R1CD10ComparisonFlow.recordFeature(i,false);
        R1CD10ComparisonFlow.stopB();
        check(!R1CD10ComparisonFlow.hasFeatureCoverage(),"missing third avatar surface A");
        check(R1CD10ComparisonFlow.missingFeatureHint().equals("A/3"),"missing A category 3");
        R1CD10ComparisonFlow.finish();R1CD09DiagnosticGate.finish();

        start(R1CD09DiagnosticGate.Mode.ACCOUNT);
        R1CD09AccountSentinel.clear();R1CD10ComparisonFlow.startA();
        Object one=new Object(), two=new Object();
        R1CD09AccountSentinel.observe(one);
        R1CD10ComparisonFlow.stopA();
        R1CD09AccountSentinel.markManualSwitch();
        R1CD10ComparisonFlow.confirmSwitch();
        R1CD10ComparisonFlow.startB();
        R1CD09AccountSentinel.observe(two);
        String log=R1CD09AccountSentinel.summary();
        check(log.contains("MANUAL_SWITCH_MARKER_NOT_IDENTITY_PROOF"),"switch recorded");
        check(log.contains("REFERENCE_CHANGED_AFTER_MARKER"),"transition ordering");
        check(!log.contains("UserSession@"),"no object string exposed");
        check(R1CD10ComparisonFlow.summary().contains("A_explicit=[1, 0, 0]"),"A account callbacks");
        check(R1CD10ComparisonFlow.summary().contains("B_explicit=[1, 0, 0]"),"B account callbacks");
        R1CD10ComparisonFlow.stopB();
        check(R1CD10ComparisonFlow.quality().contains("ACCOUNT_NOT_VERIFIED"),"never account identity");
        R1CD10ComparisonFlow.finish();R1CD09DiagnosticGate.finish();

        String big="x".repeat(17_500);
        R1CD10ReportArchive.capture("FEED","SOURCE_AUDIT",big);
        String all=R1CD10ReportArchive.all("FEED","");
        check(all.contains("clipped_records=1"),"truncation counter");
        check(all.contains("ARCHIVE_INCOMPLETE"),"truncation disclosed prominently");
        check(all.contains("NOT full Android logcat"),"no false logcat claim");
        check(all.contains("D06_MEDIA_IDENTITY=NOT_VERIFIED ACTIONS=OFF"),"identity gate retained");
        System.out.println("INA_D11_HARDENED_DIAGNOSTIC_NEGATIVES_PASS="+assertions);
    }
}
