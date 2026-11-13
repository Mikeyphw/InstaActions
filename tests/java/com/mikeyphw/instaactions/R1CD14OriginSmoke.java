package com.mikeyphw.instaactions;
/** Pure-Java test: exact enum code categories, A/B isolation and no source promotion. */
public final class R1CD14OriginSmoke {
    static int checked;
    static void require(boolean ok,String why) {if(!ok)throw new AssertionError(why);checked++;}
    public static void main(String[] args) {
        R1CD14NativeOriginProbe.resetForTest();
        require(R1CD14NativeOriginProbe.classify("CLIPS_TAB") ==
            R1CD14NativeOriginProbe.Source.CLIPS_TAB_SOURCE,"tab source");
        require(R1CD14NativeOriginProbe.classify("CLIPS_TAB_HOMECOMING") ==
            R1CD14NativeOriginProbe.Source.CLIPS_TAB_SOURCE,"homecoming source");
        require(R1CD14NativeOriginProbe.classify("REEL_FEED_TIMELINE") ==
            R1CD14NativeOriginProbe.Source.REEL_FEED_TIMELINE_SOURCE,"reel timeline source");
        require(R1CD14NativeOriginProbe.classify("FEED_TIMELINE") ==
            R1CD14NativeOriginProbe.Source.FEED_TIMELINE_SOURCE,"feed source");
        require(R1CD14NativeOriginProbe.classify("FEED_TIMELINE_POST_TAP") ==
            R1CD14NativeOriginProbe.Source.OTHER_FEED_FAMILY_SOURCE,"feed variant");
        require(R1CD14NativeOriginProbe.classify("PROFILE") ==
            R1CD14NativeOriginProbe.Source.OTHER_NATIVE_SOURCE,"profile is not feed");
        require(R1CD14NativeOriginProbe.classify("CLIPS_PROFILE") ==
            R1CD14NativeOriginProbe.Source.OTHER_NATIVE_SOURCE,"no guessed clip type");
        require(R1CD14NativeOriginProbe.classify(null) ==
            R1CD14NativeOriginProbe.Source.SOURCE_NULL,"missing != feed");
        require(R1CD14NativeOriginProbe.report().contains("HOOK=REUSE_EXISTING_D04"),"no extra hooks");
        require(R1CD14NativeOriginProbe.report().contains("MEDIA_IDENTITY=NOT_VERIFIED"),"identity not promoted");
        require(R1CD09DiagnosticGate.begin(R1CD09DiagnosticGate.Mode.AUTO).startsWith("STARTED"),"auto begin");
        R1CD10ComparisonFlow.begin(R1CD09DiagnosticGate.Mode.AUTO);
        require(R1CD10ComparisonFlow.startA().startsWith("A_RECORDING"),"A start");
        R1CD14NativeOriginProbe.record(R1CD14NativeOriginProbe.Source.CLIPS_TAB_SOURCE,"REELS");
        R1CD14NativeOriginProbe.record(R1CD14NativeOriginProbe.Source.REEL_FEED_TIMELINE_SOURCE,"UNKNOWN");
        require(R1CD10ComparisonFlow.stopA().startsWith("A_STOPPED"),"A stop");
        require(R1CD10ComparisonFlow.confirmSwitch().contains("NOT_VERIFIED"),"switch not ID proof");
        require(R1CD10ComparisonFlow.startB().startsWith("B_RECORDING"),"B start");
        R1CD14NativeOriginProbe.record(R1CD14NativeOriginProbe.Source.FEED_TIMELINE_SOURCE,"REELS");
        R1CD14NativeOriginProbe.record(R1CD14NativeOriginProbe.Source.SOURCE_NULL,"REELS");
        require(R1CD10ComparisonFlow.stopB().startsWith("B_STOPPED"),"B stop");
        String report=R1CD14NativeOriginProbe.report();
        require(report.contains("CLIPS_TAB_SOURCE total=1 manual_A=1 manual_B=0"),"A isolated");
        require(report.contains("FEED_TIMELINE_SOURCE total=1 manual_A=0 manual_B=1"),"B isolated");
        require(report.contains("SCREEN_OTHER_OR_UNKNOWN=1"),"unknown not promoted");
        require(report.contains("ACCOUNT_IDENTITY=NOT_VERIFIED"),"no account identity");
        require(!R1CD07Recovery.isEnabled(),"no auto arm");
        R1CD10ComparisonFlow.finish();R1CD09DiagnosticGate.finish();
        System.out.println("INA_D14_SOURCE_ENUM_READONLY_AB_PASS="+checked);
    }
}
