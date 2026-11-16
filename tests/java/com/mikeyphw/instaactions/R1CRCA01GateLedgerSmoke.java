package com.mikeyphw.instaactions;

/** No Android dependency: genuine JVM negative/safety and bounded reports. */
public final class R1CRCA01GateLedgerSmoke {
    private static int checks;
    private static void ok(boolean v,String reason){if(!v)throw new AssertionError(reason);checks++;}
    public static void main(String[] args) {
        R1CRCA01GateLedger.clear();
        ok(R1CRCA01GateLedger.outcome(Boolean.TRUE)==0,"true");
        ok(R1CRCA01GateLedger.outcome(Boolean.FALSE)==1,"false");
        ok(R1CRCA01GateLedger.outcome(null)==2,"nullable");
        ok(R1CRCA01GateLedger.outcome(new Object())==3,"other");
        R1CRCA01GateLedger.record(-1,R1CRCA01GateLedger.Site.EXACT_D3R_ORIGINAL,false);
        R1CRCA01GateLedger.record(2,R1CRCA01GateLedger.Site.EXACT_D3R_ORIGINAL,true);
        ok(R1CRCA01GateLedger.eventCount(0)==0,"no implicit A");
        for(int i=0;i<8;i++){
            R1CRCA01GateLedger.record(0,R1CRCA01GateLedger.Site.EXACT_D3R_ORIGINAL,false);
            R1CRCA01GateLedger.record(1,R1CRCA01GateLedger.Site.EXACT_D3R_ORIGINAL,true);
        }
        R1CRCA01GateLedger.record(0,R1CRCA01GateLedger.Site.MEDIA_SET_ARGUMENT_AFTER_SUCCESS,null);
        R1CRCA01GateLedger.recordUi(1,1);
        R1CRCA01GateLedger.recordUi(1,2);
        ok(R1CRCA01GateLedger.count(0,R1CRCA01GateLedger.Site.EXACT_D3R_ORIGINAL,1)==8,"A false");
        ok(R1CRCA01GateLedger.count(1,R1CRCA01GateLedger.Site.EXACT_D3R_ORIGINAL,0)==8,"B true");
        ok(R1CRCA01GateLedger.count(0,R1CRCA01GateLedger.Site.MEDIA_SET_ARGUMENT_AFTER_SUCCESS,2)==1,"setter null");
        ok(R1CRCA01GateLedger.count(1,R1CRCA01GateLedger.Site.REPOST_UI_ORIGINAL_PAYLOAD,3)==1,"UI payload type");
        String report=R1CRCA01GateLedger.report();
        ok(report.contains("opposing_boolean_majority"),"descriptive difference");
        ok(report.contains("SERVER_ORIGIN=UNVERIFIED"),"no invented server source");
        ok(report.contains("CAN_REPOST_JSON_LINK=UNVERIFIED"),"no invented schema link");
        ok(report.contains("FRIENDS_SHARED_GATE=UNVERIFIED"),"no invented feature coupling");
        ok(report.contains("MANUAL_LABEL_ONLY"),"not verified account");
        ok(!report.contains("java.lang.Object@"),"no raw objects");
        for(int i=0;i<5000;i++)R1CRCA01GateLedger.record(0,R1CRCA01GateLedger.Site.MEDIA_GET_ORIGINAL,false);
        ok(R1CRCA01GateLedger.eventCount(0)==4096,"strict cohort cap");
        ok(R1CRCA01GateLedger.report().contains("CAPPED"),"honest cap report");
        R1CRCA01GateLedger.clear();
        ok(R1CRCA01GateLedger.eventCount(0)==0 && R1CRCA01GateLedger.eventCount(1)==0,"clear both phases");
        ok(R1CRCA01GateLedger.report().contains("NOT_ESTABLISHED_OR_INSUFFICIENT_SAMPLES"),"empty != false");
        R1CRCA01GateLedger.mediaHooks("READY_GET_AND_SET");
        R1CRCA01GateLedger.mediaHooks("arbitrary string");
        ok("READY_GET_AND_SET".equals(R1CRCA01GateLedger.mediaHooks()),"bounded status vocabulary");
        System.out.println("INA_RCA01_LEDGER_SAFETY_PASS="+checks);
    }
}
