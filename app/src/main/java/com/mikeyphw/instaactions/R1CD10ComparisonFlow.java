package com.mikeyphw.instaactions;

/** A -> B manually-labelled capture lifecycle shared by every active diagnostic.
 * Manual confirmation is not verified account identity. No account/media objects.
 */
final class R1CD10ComparisonFlow {
    enum Stage { IDLE, READY_A, RECORDING_A, A_STOPPED, READY_B, RECORDING_B, B_STOPPED, ABORTED }
    private static Stage stage=Stage.IDLE;
    private static R1CD09DiagnosticGate.Mode mode;
    private static int aSamples, bSamples;
    // Provenance is explicitly feature-specific, NOT a count of passive callbacks.
    // 0/1/2 refer to the user-selected diagnostic sub-surfaces, not media IDs.
    private static final int[][] featureScans = new int[2][3];
    private static final int[][] screenHintValid = new int[2][3];
    private static boolean manualSwitchAcknowledged;
    private static String lastCompletedQuality="NOT_FINISHED_NOT_QUALIFIED";
    private R1CD10ComparisonFlow() { }
    static synchronized void begin(R1CD09DiagnosticGate.Mode next) {
        mode=next;stage=Stage.READY_A;aSamples=0;bSamples=0;manualSwitchAcknowledged=false;
        lastCompletedQuality="NOT_FINISHED_NOT_QUALIFIED";
        for(int a=0;a<2;a++){
            java.util.Arrays.fill(featureScans[a],0);
            java.util.Arrays.fill(screenHintValid[a],0);
        }
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
    /** Counts an explicit mode-specific observation only while the matching A/B
     * cohort is recording. 'screenBound' means the navigation hint matched,
     * NOT that we verified a Media, avatar, account or action. */
    static synchronized void recordFeature(int surface,boolean screenBound) {
        if(surface<0 || surface>2)return;
        int a=cohort();
        if(a<0)return;
        if(featureScans[a][surface]<65535)featureScans[a][surface]++;
        if(screenBound && screenHintValid[a][surface]<65535)screenHintValid[a][surface]++;
    }
    static synchronized boolean hasFeatureCoverage() {
        // Require all declared Feed and Avatar sub-surfaces, rather than a
        // misleading aggregate of callback events unrelated to this feature.
        int surfaces=(mode==R1CD09DiagnosticGate.Mode.FEED ||
                mode==R1CD09DiagnosticGate.Mode.AVATAR)?3:1;
        for(int a=0;a<2;a++)for(int s=0;s<surfaces;s++)
            if(featureScans[a][s]==0)return false;
        return true;
    }
    static synchronized String quality() {
        if(stage==Stage.IDLE)return lastCompletedQuality;
        if(stage==Stage.ABORTED)return "ABORTED_UNQUALIFIED";
        if(!complete())return "NOT_FINISHED_NOT_QUALIFIED";
        if(!hasFeatureCoverage())return "INCOMPLETE_FEATURE_SNAPSHOTS_NOT_QUALIFIED";
        // Neither view shape counts, enum callbacks nor weak account references
        // establish the underlying identity or enable actions.
        if(mode==R1CD09DiagnosticGate.Mode.FEED){
            for(int a=0;a<2;a++)for(int s=0;s<3;s++)
                if(screenHintValid[a][s]==0)
                    return "PAIRED_SCANS_SCREEN_HINT_MISMATCH_NOT_QUALIFIED";
            return "PAIRED_MANUAL_SCREEN_SCANS_MEDIA_NOT_VERIFIED";
        }
        if(mode==R1CD09DiagnosticGate.Mode.AVATAR)
            return "PAIRED_VIEW_CANDIDATES_AVATAR_NOT_VERIFIED";
        if(mode==R1CD09DiagnosticGate.Mode.AUTO)
            return "PAIRED_AUTO_SCREEN_HINTS_ACCOUNT_AND_MEDIA_NOT_VERIFIED";
        if(mode==R1CD09DiagnosticGate.Mode.ACCOUNT)
            return "PAIRED_CALLBACK_COVERAGE_ACCOUNT_NOT_VERIFIED";
        if(mode==R1CD09DiagnosticGate.Mode.MEDIA)
            return "PAIRED_MENU_CALLBACKS_MEDIA_NOT_VERIFIED";
        return "PAIRED_REPOST_OBSERVATIONS_ACCOUNT_NOT_VERIFIED";
    }
    static synchronized String missingFeatureHint() {
        StringBuilder b=new StringBuilder();
        int n=(mode==R1CD09DiagnosticGate.Mode.FEED ||
               mode==R1CD09DiagnosticGate.Mode.AVATAR)?3:1;
        for(int a=0;a<2;a++)for(int s=0;s<n;s++)if(featureScans[a][s]==0){
            if(b.length()>0)b.append(", ");
            b.append(a==0?'A':'B').append('/').append(s+1);
        }
        return b.length()==0?"NONE":b.toString();
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
            " account_identity=NOT_VERIFIED\n"+
            "FEATURE_EVIDENCE A_explicit="+java.util.Arrays.toString(featureScans[0])+
            " B_explicit="+java.util.Arrays.toString(featureScans[1])+
            " A_screen_hint="+java.util.Arrays.toString(screenHintValid[0])+
            " B_screen_hint="+java.util.Arrays.toString(screenHintValid[1])+
            " (screen hint != verified feature owner)\n"+
            "FEATURE_COVERAGE="+quality()+" missing_explicit="+missingFeatureHint()+"\n";
    }
    static synchronized void abort(){stage=Stage.ABORTED;lastCompletedQuality="ABORTED_UNQUALIFIED";}
    static synchronized void finish(){lastCompletedQuality=quality();stage=Stage.IDLE;}
}
