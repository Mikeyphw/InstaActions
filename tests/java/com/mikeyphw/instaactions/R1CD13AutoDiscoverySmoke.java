package com.mikeyphw.instaactions;

/** Offline-only A/B screen-classification and transition-accounting tests. */
public final class R1CD13AutoDiscoverySmoke {
    static int checked;
    static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);checked++;}
    static void observe(String screen,boolean feed,boolean clips) {
        R1CD13AutoSurface.observe(screen,true,feed,clips,false);
        R1CD13AutoSurface.observe(screen,true,feed,clips,false);
    }
    public static void main(String[] args) {
        R1CD13AutoSurface.resetForTest();
        require(R1CD13AutoSurface.classify("HOME_FEED",true,true,false,false)
            ==R1CD13AutoSurface.Surface.HOME_FEED,"feed requires selected feed tab");
        require(R1CD13AutoSurface.classify("HOME_FEED",true,false,false,false)
            ==R1CD13AutoSurface.Surface.UNKNOWN,"no selected feed tab = unknown");
        require(R1CD13AutoSurface.classify("REELS",true,true,false,false)
            ==R1CD13AutoSurface.Surface.FEED_OPENED_REEL_CANDIDATE,"feed reel only candidate");
        require(R1CD13AutoSurface.classify("REELS",true,false,true,false)
            ==R1CD13AutoSurface.Surface.DEDICATED_REELS_TAB_HINT,"clips tab hint");
        require(R1CD13AutoSurface.classify("REELS",true,false,false,false)
            ==R1CD13AutoSurface.Surface.REELS_ORIGIN_UNKNOWN,"hidden tabs origin unresolved");
        require(R1CD13AutoSurface.classify("REELS",true,true,true,false)
            ==R1CD13AutoSurface.Surface.UNKNOWN,"tab conflict fails closed");
        require(R1CD13AutoSurface.classify("STORY",true,false,false,false)
            ==R1CD13AutoSurface.Surface.STORY,"anchored story");
        require(R1CD13AutoSurface.classify("REELS",true,false,true,true)
            ==R1CD13AutoSurface.Surface.UNKNOWN,"truncated UI fails closed");
        require(R1CD13AutoSurface.classify("REELS",false,false,true,false)
            ==R1CD13AutoSurface.Surface.REELS_ORIGIN_UNKNOWN,"nonmain tab no proof");
        require(R1CD13AutoSurface.classify("FEED_REEL",true,true,false,false)
            ==R1CD13AutoSurface.Surface.UNKNOWN,"unrecognized screen no guess");
        require(R1CD13AutoSurface.report().contains("ACCOUNT_IDENTITY=NOT_VERIFIED"),"identity never promoted");
        require(R1CD13AutoSurface.report().contains("polls=0"),"process initial no observations");
        // Passive mode: no Begin required. Two stable samples commit one transition.
        observe("HOME_FEED",true,false);
        require(R1CD13AutoSurface.brief().contains("HOME_FEED"),"auto-on passive feed");
        observe("REELS",false,false);
        require(R1CD13AutoSurface.brief().contains("REELS_ORIGIN_UNKNOWN"),"route not guess");
        require(R1CD13AutoSurface.report().contains("from_HOME_FEED_ROUTE_ONLY"),"source history is limited evidence");
        R1CD13AutoSurface.invalidatedByNewIntent();
        require(R1CD13AutoSurface.report().contains("SURFACE_OR_ACCOUNT_NOT_VERIFIED"),"new intent no identity");
        R1CD13AutoSurface.resetForTest();
        require(!R1CD09DiagnosticGate.active(R1CD09DiagnosticGate.Mode.AUTO),"no auto capture before Begin");
        require(R1CD09DiagnosticGate.begin(R1CD09DiagnosticGate.Mode.AUTO).startsWith("STARTED"),"auto mode begin");
        R1CD10ComparisonFlow.begin(R1CD09DiagnosticGate.Mode.AUTO);
        require(R1CD10ComparisonFlow.startA().startsWith("A_RECORDING"),"A begin");
        observe("HOME_FEED",true,false);
        observe("REELS",false,true);
        observe("REELS",true,false);
        observe("STORY",false,false);
        String r=R1CD13AutoSurface.report();
        require(r.contains("manual_A=1"),"A auto evidence retained");
        require(r.contains("FEED_OPENED_REEL_CANDIDATE"),"candidate not proof");
        require(r.contains("DEDICATED_REELS_TAB_HINT"),"clips evidence");
        require(R1CD10ComparisonFlow.stopA().startsWith("A_STOPPED"),"A stop");
        require(R1CD10ComparisonFlow.confirmSwitch().contains("ACKNOWLEDGED_NOT_VERIFIED"),"switch manual not identity");
        require(R1CD10ComparisonFlow.startB().startsWith("B_RECORDING"),"B start");
        observe("HOME_FEED",true,false);
        observe("STORY",false,false);
        require(R1CD10ComparisonFlow.stopB().startsWith("B_STOPPED"),"B stop");
        require(R1CD10ComparisonFlow.quality().equals("PAIRED_AUTO_SCREEN_HINTS_ACCOUNT_AND_MEDIA_NOT_VERIFIED"),"paired still unverified");
        r=R1CD13AutoSurface.report();
        require(r.contains("manual_B=1"),"B auto evidence retained");
        require(r.contains("D13_EVENT_ORDER"),"bounded ordered evidence");
        require(!r.contains("VERIFIED_ACCOUNT_ID="),"never identity exports");
        require(!R1CD07Recovery.isEnabled(),"auto never arms Repost");
        // No automatic feature qualification if A or B never sees a stable screen.
        R1CD10ComparisonFlow.finish(); R1CD09DiagnosticGate.finish();
        R1CD13AutoSurface.resetForTest();
        R1CD09DiagnosticGate.begin(R1CD09DiagnosticGate.Mode.AUTO);
        R1CD10ComparisonFlow.begin(R1CD09DiagnosticGate.Mode.AUTO);
        R1CD10ComparisonFlow.startA();observe("HOME_FEED",true,false);
        R1CD10ComparisonFlow.stopA();R1CD10ComparisonFlow.confirmSwitch();
        R1CD10ComparisonFlow.startB();R1CD10ComparisonFlow.stopB();
        require(R1CD10ComparisonFlow.quality().equals("INCOMPLETE_FEATURE_SNAPSHOTS_NOT_QUALIFIED"),"B missing evidence fails closed");
        System.out.println("INA_D13_AUTO_SURFACE_AB_FAIL_CLOSED_PASS="+checked);
    }
}
