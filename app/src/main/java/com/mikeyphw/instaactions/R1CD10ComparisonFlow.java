package com.mikeyphw.instaactions;

/** A -> B manually-labelled capture lifecycle shared by every active diagnostic.
 * Manual confirmation is not verified account identity. No account/media objects.
 */
final class R1CD10ComparisonFlow {
    enum Stage { IDLE, READY_A, RECORDING_A, A_STOPPED, READY_B, RECORDING_B, B_STOPPED, ABORTED }
    private static Stage stage=Stage.IDLE;
    private static R1CD09DiagnosticGate.Mode mode;
    private static int aSamples, bSamples;
    private static boolean manualSwitchAcknowledged;
    private R1CD10ComparisonFlow() { }
    static synchronized void begin(R1CD09DiagnosticGate.Mode next) {
        mode=next;stage=Stage.READY_A;aSamples=0;bSamples=0;manualSwitchAcknowledged=false;
    }
    static synchronized String startA() {
        if(stage!=Stage.READY_A)return "REFUSED: only Step A1 may start A";
        if(R1CD07Recovery.isEnabled())return "REFUSED: turn Repost recovery OFF";
        stage=Stage.RECORDING_A;return "A_RECORDING · manual affected account; original results only";
    }
    static synchronized void sample() {
        if(stage==Stage.RECORDING_A && aSamples<65535)aSamples++;
        if(stage==Stage.RECORDING_B && bSamples<65535)bSamples++;
    }
    static synchronized String stopA() {
        if(stage!=Stage.RECORDING_A)return "REFUSED: Start A before Stop A";
        if(R1CD07Recovery.isEnabled())return "REFUSED: turn recovery OFF before switching accounts";
        stage=Stage.A_STOPPED;
        return "A_STOPPED · "+aSamples+" observations · OFF before account switch";
    }
    static synchronized String confirmSwitch() {
        if(stage!=Stage.A_STOPPED)return "REFUSED: Stop A first";
        if(R1CD07Recovery.isEnabled())return "REFUSED: recovery must be OFF";
        manualSwitchAcknowledged=true;stage=Stage.READY_B;
        return "MANUAL_SWITCH_ACKNOWLEDGED_NOT_VERIFIED · now Start B";
    }
    static synchronized String startB() {
        if(stage!=Stage.READY_B || !manualSwitchAcknowledged)
            return "REFUSED: Stop A and manually confirm account switch first";
        if(R1CD07Recovery.isEnabled())return "REFUSED: recovery must be OFF";
        stage=Stage.RECORDING_B;return "B_RECORDING · manual unaffected account";
    }
    static synchronized String stopB() {
        if(stage!=Stage.RECORDING_B)return "REFUSED: Start B before Stop B";
        if(R1CD07Recovery.isEnabled())return "REFUSED: recovery must be OFF";
        stage=Stage.B_STOPPED;return "B_STOPPED · "+bSamples+" observations · ready to Finish";
    }
    static synchronized boolean complete(){return stage==Stage.B_STOPPED;}
    static synchronized boolean recording(){return stage==Stage.RECORDING_A || stage==Stage.RECORDING_B;}
    static synchronized int cohort(){return stage==Stage.RECORDING_A?0:stage==Stage.RECORDING_B?1:-1;}
    static synchronized Stage stage(){return stage;}
    static synchronized String summary(){
        return "A_B_CAPTURE stage="+stage+" mode="+mode+" A_observations="+aSamples+
            " B_observations="+bSamples+" manual_switch="+manualSwitchAcknowledged+
            " account_identity=NOT_VERIFIED\n";
    }
    static synchronized void abort(){stage=Stage.ABORTED;}
    static synchronized void finish(){stage=Stage.IDLE;}
}
