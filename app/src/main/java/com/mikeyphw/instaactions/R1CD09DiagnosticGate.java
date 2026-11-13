package com.mikeyphw.instaactions;

/** One active diagnostic, one explicit lifecycle. No Android/Instagram object is retained. */
final class R1CD09DiagnosticGate {
    enum Mode { REELS, AB, FEED, MEDIA, ACCOUNT, AVATAR, PASSIVE }
    enum State { IDLE, RUNNING, FINISHED, ABORTED }
    private static Mode mode;
    private static State state=State.IDLE;
    private static int session;
    private static String reason="No diagnostic started";
    private static boolean accountTransitionExpected;
    private R1CD09DiagnosticGate() { }
    static synchronized String begin(Mode requested) {
        if(requested==null || requested==Mode.PASSIVE)return "REFUSED: Passive diagnostics are always on, not manually started";
        if(state==State.RUNNING)return "REFUSED: Finish or Abort "+mode+" before starting another diagnostic";
        if(R1CD07Recovery.isEnabled())return "REFUSED: Recovery OFF before beginning a diagnostic";
        mode=requested;state=State.RUNNING;session++;
        accountTransitionExpected=false;
        reason="Explicitly started";
        return "STARTED diagnostic="+mode+" session="+session+". Finish or Abort to change mode.";
    }
    static synchronized boolean active(Mode expected){return state==State.RUNNING && mode==expected;}
    static synchronized String finish() {
        if(state!=State.RUNNING)return "REFUSED: no active diagnostic";
        state=State.FINISHED;reason="Finished by user; results preserved";
        return "FINISHED "+mode+" session="+session+"; copy its report before changing mode.";
    }
    static synchronized String abort(String why) {
        if(state!=State.RUNNING)return "REFUSED: no active diagnostic";
        state=State.ABORTED;reason=why;
        return "ABORTED "+mode+" session="+session+"; unqualified observations are not promoted.";
    }
    static synchronized void expectManualABSwitch() {
        if(state==State.RUNNING && mode!=Mode.PASSIVE)accountTransitionExpected=true;
    }
    static synchronized void accountReferenceChanged() {
        if(state!=State.RUNNING)return;
        if(mode==Mode.ACCOUNT){
            reason="UserSession weak reference changed during account diagnostic";
            return;
        }
        if(accountTransitionExpected){
            accountTransitionExpected=false; reason="Observed weak UserSession reference transition during manual A/B handoff";
            return;
        }
        // A detected transition outside the explicit A -> B handoff must not
        // leave a stale live capture that would block every future Begin.
        R1CD10ReportArchive.capture(mode.name(),"AUTO_ABORT_ACCOUNT_SIGNAL",
            status()+"\n"+R1CD10ComparisonFlow.summary());
        R1CD10ComparisonFlow.abort();
        abort("Possible account/session change while diagnostic active; fail closed");
    }
    static synchronized boolean running(){return state==State.RUNNING;}
    static synchronized String status(){return "diagnostic="+(mode==null?"NONE":mode)+" state="+state+
            " session="+session+" · "+reason+" · account labels are MANUAL";}
    static synchronized Mode selected(){return mode;}
    static synchronized State state(){return state;}
}
