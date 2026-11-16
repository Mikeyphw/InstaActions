package com.mikeyphw.instaactions;

/** Pure JVM RCA02 negative and A1/B/A2 provenance smoke; no Android runtime. */
public final class R1CRCA02LineageSmoke {
    static int verified;
    static void ok(boolean cond,String label) {if(!cond)throw new AssertionError(label);verified++;}
    static void pass(int index,Boolean answer,int source){
        String begin=R1CRCA02LineageLedger.begin(index);
        ok(begin.contains("RECORDING"),"begin "+index);
        Object media=new Object();
        int old=R1CRCA02LineageLedger.enterWriter(source);
        R1CRCA02LineageLedger.mediaSet(media,answer);
        R1CRCA02LineageLedger.leaveWriter(old);
        R1CRCA02LineageLedger.mediaGet(media,answer);
        R1CRCA02LineageLedger.UiFrame f=R1CRCA02LineageLedger.enterUi(
            R1CRCA02LineageLedger.DATA_PRESENT);
        R1CRCA02LineageLedger.config(false);
        R1CRCA02LineageLedger.d3r(answer);
        R1CRCA02LineageLedger.finishUi(f,index==1?1:0);
        R1CRCA02LineageLedger.d3r(answer);
        R1CRCA02LineageLedger.d3r(answer);
        ok(R1CRCA02LineageLedger.events(index)==7,"events "+index);
        String stopped=R1CRCA02LineageLedger.stop();
        ok(stopped.contains("STOPPED"),"stop "+index);
        ok(R1CRCA02LineageLedger.activePass()==-1,"no capture after stop");
        R1CRCA02LineageLedger.mediaGet(media,true);
        ok(R1CRCA02LineageLedger.events(index)==7,"late event excluded");
    }
    public static void main(String[] args){
        R1CRCA02LineageLedger.reset();
        ok(R1CRCA02LineageLedger.activeMode(),"mode");
        ok(R1CRCA02LineageLedger.begin(1).startsWith("REFUSED"),"no skip to B");
        ok(R1CRCA02LineageLedger.stop().startsWith("REFUSED"),"no empty stop");
        R1CRCA02LineageLedger.writerStatus(true,true);
        pass(0,false,R1CRCA02LineageLedger.LEGACY_FIELD_COPY);
        ok(R1CRCA02LineageLedger.begin(0).startsWith("REFUSED"),"no A1 repeat");
        pass(1,true,R1CRCA02LineageLedger.DELTA_MERGE_OR_FALLBACK);
        pass(2,false,R1CRCA02LineageLedger.LEGACY_FIELD_COPY);
        ok(R1CRCA02LineageLedger.finishedPasses()==3,"all passes");
        ok(R1CRCA02LineageLedger.begin(2).startsWith("REFUSED"),"no A2 replay");
        String report=R1CRCA02LineageLedger.report();
        for(String t:new String[]{"PASS A1 samples=7","PASS B samples=7",
                "PASS A2 samples=7","X043F_LEGACY_FIELD_COPY", "X06NV_DELTA_MERGE_OR_FALLBACK",
                "UI_SINGLETON_NEGATIVE_D3R","UI_PAYLOAD", "REPLICATED"})
            ok(report.contains(t),"report "+t);
        ok(report.contains("MEDIA_GRAPHQL_LINK=UNVERIFIED"),"no false link");
        ok(report.contains("SERVER_SOURCE=UNVERIFIED"),"no false server");
        ok(!report.contains("java.lang.Object@"),"no object address");
        ok(!report.contains("username="),"no username");
        R1CRCA02LineageLedger.reset();
        ok(R1CRCA02LineageLedger.events(0)==0,"clear A1");
        ok(R1CRCA02LineageLedger.finishedPasses()==0,"clear sequence");
        ok(R1CRCA02LineageLedger.begin(0).contains("RECORDING"),"fresh run");
        R1CRCA02LineageLedger.UiFrame missing=R1CRCA02LineageLedger.enterUi(
            R1CRCA02LineageLedger.DATA_MISSING);
        R1CRCA02LineageLedger.finishUi(missing,0);
        ok(R1CRCA02LineageLedger.report().contains("UI_SINGLETON_DATA_MISSING T=0 F=0 NULL=0 OTHER=1"),"missing separate from false");
        R1CRCA02LineageLedger.UiFrame unknown=R1CRCA02LineageLedger.enterUi(
            R1CRCA02LineageLedger.DATA_UNKNOWN);
        R1CRCA02LineageLedger.finishUi(unknown,0);
        ok(R1CRCA02LineageLedger.report().contains("UI_SINGLETON_UNRESOLVED T=0 F=0 NULL=0 OTHER=1"),"unknown not false");
        int prev=R1CRCA02LineageLedger.enterWriter(R1CRCA02LineageLedger.DELTA_MERGE_OR_FALLBACK);
        int prior=R1CRCA02LineageLedger.enterWriter(R1CRCA02LineageLedger.LEGACY_FIELD_COPY);
        R1CRCA02LineageLedger.mediaSet(new Object(),true);
        R1CRCA02LineageLedger.leaveWriter(prior);
        R1CRCA02LineageLedger.leaveWriter(prev);
        ok(R1CRCA02LineageLedger.report().contains("AMBIGUOUS_NESTED_CALLER T=1"),"nested ambiguity");
        R1CRCA02LineageLedger.stop();
        System.out.println("INA_RCA02_LINEAGE_NEGATIVES_PASS="+verified);
    }
}
