package com.mikeyphw.instaactions;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;

/**
 * RCA02 source-pinned, PASSIVE lineage evidence. No media/account identifiers or
 * raw object values leave memory; same-object comparison uses bounded weak ==.
 * A1/B/A2 labels are MANUAL and GraphQL-to-Media identity remains unverified.
 * Do not mistake a verified caller METHOD for a verified server value source.
 */
final class R1CRCA02LineageLedger {
    static final int DELTA_MERGE_OR_FALLBACK=1;
    static final int LEGACY_FIELD_COPY=2;
    static final int AMBIGUOUS_CALLER=3;
    static final int UNKNOWN_CALLER=0;
    static final int DATA_MISSING=0, DATA_PRESENT=1, DATA_UNKNOWN=2;
    private static final int PASS_COUNT=3, MAX_EVENTS=4096;
    private static final int MAX_OBJECTS=256, MAX_TIMELINE=112;
    private static final String[] PASSES={"A1", "B", "A2"};
    private static final String[] SITE_NAMES={"MEDIA_GET", "MEDIA_SET_SUCCESS", "D3R_ORIGINAL",
            "MOBILECONFIG_ORIGINAL", "UI_PAYLOAD", "UI_SINGLETON_DATA_MISSING",
            "UI_SINGLETON_NEGATIVE_D3R", "UI_SINGLETON_UNRESOLVED", "UI_OTHER"};
    private static final String[] ORIGINS={"UNKNOWN_OR_OTHER", "X06NV_DELTA_MERGE_OR_FALLBACK",
            "X043F_LEGACY_FIELD_COPY", "AMBIGUOUS_NESTED_CALLER"};
    private static final int[][][] COUNTS=new int[PASS_COUNT][SITE_NAMES.length][4];
    private static final int[][][] SET_ORIGIN=new int[PASS_COUNT][4][4];
    private static final int[][] MATCH_UNIQUE=new int[PASS_COUNT][4];
    private static final int[][] MATCH_REPEAT=new int[PASS_COUNT][4];
    private static final int[] OBJECT_OVERFLOW=new int[PASS_COUNT];
    private static final int[] EVENT_OVERFLOW=new int[PASS_COUNT];
    private static final int[] MATCH_MISMATCH=new int[PASS_COUNT];
    private static final int[] EVENTS=new int[PASS_COUNT];
    private static final int[] TIMELINE_DROPPED=new int[PASS_COUNT];
    @SuppressWarnings("unchecked")
    private static final ArrayDeque<WeakSlot>[] OBJECTS=new ArrayDeque[PASS_COUNT];
    @SuppressWarnings("unchecked")
    private static final ArrayDeque<String>[] TIMELINES=new ArrayDeque[PASS_COUNT];
    private static volatile int active=-1;
    private static int finished;
    private static long ordinal;
    private static volatile long generation;
    private static volatile String writerStatus="NOT_INSTALLED";
    // Caller scopes contain primitive method categories only, never an object.
    private static final ThreadLocal<Integer> WRITER=new ThreadLocal<>();
    private static final ThreadLocal<UiFrame> UI=new ThreadLocal<>();
    private R1CRCA02LineageLedger() { }
    static {for(int i=0;i<PASS_COUNT;i++){OBJECTS[i]=new ArrayDeque<>();TIMELINES[i]=new ArrayDeque<>();}}
    static boolean activeMode(){return true;} // Standalone UI: RCA02, not legacy D07.
    static int activePass(){return active;}
    static synchronized int finishedPasses(){return finished;}
    static synchronized int events(int pass){return pass>=0 && pass<PASS_COUNT?EVENTS[pass]:0;}
    static synchronized String begin(int pass){
        if(active!=-1 || pass!=finished || pass<0 || pass>=PASS_COUNT)
            return "REFUSED: expected next pass "+(finished<PASS_COUNT?PASSES[finished]:"none");
        generation++;active=pass;
        return "RCA02 "+PASSES[pass]+" RECORDING; account identity unverified";
    }
    static synchronized String stop(){
        if(active<0)return "REFUSED: RCA02 capture not active";
        int old=active;active=-1;finished=old+1;generation++;
        return "RCA02 "+PASSES[old]+" STOPPED; samples preserved";
    }
    static synchronized void reset(){
        active=-1;finished=0;generation++;ordinal=0;
        for(int p=0;p<PASS_COUNT;p++){
            EVENTS[p]=EVENT_OVERFLOW[p]=OBJECT_OVERFLOW[p]=TIMELINE_DROPPED[p]=MATCH_MISMATCH[p]=0;
            for(int[] c:COUNTS[p])Arrays.fill(c,0);
            for(int[] c:SET_ORIGIN[p])Arrays.fill(c,0);
            Arrays.fill(MATCH_UNIQUE[p],0);Arrays.fill(MATCH_REPEAT[p],0);
            OBJECTS[p].clear();TIMELINES[p].clear();
        }
        WRITER.remove();UI.remove();
    }
    static int enterWriter(int source){
        Integer prior=WRITER.get();int saved=prior==null?UNKNOWN_CALLER:prior;
        if(source!=DELTA_MERGE_OR_FALLBACK && source!=LEGACY_FIELD_COPY)
            source=UNKNOWN_CALLER;
        WRITER.set(saved==UNKNOWN_CALLER?source:AMBIGUOUS_CALLER);
        return saved;
    }
    static void leaveWriter(int prior){
        if(prior==UNKNOWN_CALLER)WRITER.remove();
        else WRITER.set(prior);
    }
    static String writerStatus(){return writerStatus;}
    static synchronized void writerStatus(boolean delta,boolean legacy){
        writerStatus=delta && legacy?"BOTH_VERIFIED_METHOD_HOOKS_INSTALLED":
            delta?"ONLY_X06NV":legacy?"ONLY_X043F":"UNAVAILABLE";
    }
    private static int bucket(Object value){
        if(Boolean.TRUE.equals(value))return 0;
        if(Boolean.FALSE.equals(value))return 1;
        return value==null?2:3;
    }
    private static final class WeakSlot {
        final WeakReference<Object> weak;
        boolean wrote,paired;
        int setResult,source;
        WeakSlot(Object obj){weak=new WeakReference<>(obj);}
    }
    private static WeakSlot lookup(int pass,Object ref){
        if(ref==null)return null;
        ArrayDeque<WeakSlot> slots=OBJECTS[pass];
        for(Iterator<WeakSlot> it=slots.iterator();it.hasNext();){
            WeakSlot row=it.next();Object live=row.weak.get();
            if(live==null){it.remove();continue;}
            if(live==ref)return row;
        }
        if(slots.size()>=MAX_OBJECTS){slots.removeFirst();OBJECT_OVERFLOW[pass]++;}
        WeakSlot created=new WeakSlot(ref);slots.addLast(created);return created;
    }
    private static boolean add(int pass,int site,int value){
        if(pass<0 || pass>=PASS_COUNT || active!=pass)return false;
        if(EVENTS[pass]>=MAX_EVENTS){EVENT_OVERFLOW[pass]++;return false;}
        int b=value>=0 && value<=3?value:3;
        EVENTS[pass]++;COUNTS[pass][site][b]++;
        ArrayDeque<String> lines=TIMELINES[pass];
        if(lines.size()>=MAX_TIMELINE){lines.removeFirst();TIMELINE_DROPPED[pass]++;}
        String flag=b==0?"T":b==1?"F":b==2?"NULL":"OTHER";
        lines.addLast("#"+(++ordinal)+" "+PASSES[pass]+" "+SITE_NAMES[site]+"="+flag);
        return true;
    }
    static synchronized void mediaGet(Object receiver,Object result){
        int p=active,b=bucket(result);
        if(!add(p,0,b))return;
        WeakSlot row=lookup(p,receiver);
        if(row!=null && row.wrote){
            if(!row.paired){MATCH_UNIQUE[p][row.source]++;row.paired=true;}
            else MATCH_REPEAT[p][row.source]++;
            if(b!=row.setResult)MATCH_MISMATCH[p]++;
        }
    }
    static synchronized void mediaSet(Object receiver,Object argument){
        int p=active,b=bucket(argument);
        if(!add(p,1,b))return;
        Integer boxed=WRITER.get();int source=boxed==null?UNKNOWN_CALLER:boxed;
        if(source<0 || source>3)source=UNKNOWN_CALLER;
        SET_ORIGIN[p][source][b]++;
        WeakSlot row=lookup(p,receiver);
        if(row!=null){row.wrote=true;row.paired=false;row.setResult=b;row.source=source;}
    }
    static final class UiFrame {
        final int pass,presence;final long generation;
        final UiFrame previous;
        int d3r=4;
        UiFrame(int p,int presence,long gen,UiFrame prior){
            this.pass=p;this.presence=presence;generation=gen;previous=prior;
        }
    }
    static UiFrame enterUi(int presence){
        int pass=active;
        if(pass<0)return null;
        UiFrame frame=new UiFrame(pass,presence,generation,UI.get());
        UI.set(frame);return frame;
    }
    static synchronized void d3r(Object value){
        int p=active,b=bucket(value);
        if(!add(p,2,b))return;
        UiFrame f=UI.get();
        if(f!=null && f.pass==p && f.generation==generation)f.d3r=b;
    }
    static synchronized void config(Object value){add(active,3,bucket(value));}
    /** shape 0=singleton, 1=payload, otherwise unknown; never coerces objects. */
    static synchronized void finishUi(UiFrame frame,int shape){
        try {
            if(frame==null || active!=frame.pass || frame.generation!=generation)return;
            int site;
            if(shape==1)site=4;
            else if(shape!=0)site=8;
            else if(frame.presence==DATA_MISSING)site=5;
            else if(frame.presence==DATA_PRESENT && frame.d3r==1)site=6;
            else site=7;
            add(frame.pass,site,3);
        } finally {popUi(frame);}
    }
    static void cancelUi(UiFrame frame){popUi(frame);}
    private static void popUi(UiFrame frame){
        if(frame==null)return;
        if(UI.get()==frame){if(frame.previous==null)UI.remove();else UI.set(frame.previous);}
        else UI.remove();
    }
    private static String num(int[] values){return "T="+values[0]+" F="+values[1]+" NULL="+values[2]+" OTHER="+values[3];}
    static synchronized String report(){
        StringBuilder out=new StringBuilder("RCA02 READ-ONLY REPOST PROVENANCE A1/B/A2\n")
            .append("SOURCE=EXACT_IG455_DX_CALLSITES ACCOUNT_IDENTITY=MANUAL_UNVERIFIED ")
            .append("MEDIA_GRAPHQL_LINK=UNVERIFIED SERVER_SOURCE=UNVERIFIED\n")
            .append("WRITER_HOOKS=").append(writerStatus).append(" ")
            .append("SET_COUNTER=SUCCESSFUL_RETURN_ARGUMENT_NOT_PERSISTENCE\n")
            .append("X06NV=KEYED_DELTA_MERGE_OR_FALLBACK; X043F=COPIES_LEGACY_X06XQ_A3F\n")
            .append("SOURCE_METHOD_IS_NOT_PROOF_OF_SERVER_VALUE_OR_EXACT_MEDIA_ACROSS_ACCOUNTS\n")
            .append("MATCH=WEAK_SAME_MEDIA_REFERENCE_WITHIN_PASS_ONLY; NEVER_MEDIA_ID\n");
        for(int p=0;p<PASS_COUNT;p++){
            out.append("\nPASS ").append(PASSES[p]).append(" samples=").append(EVENTS[p])
                .append(" cap_dropped=").append(EVENT_OVERFLOW[p]).append('\n');
            for(int s=0;s<SITE_NAMES.length;s++)out.append(" ").append(SITE_NAMES[s])
                .append(' ').append(num(COUNTS[p][s])).append('\n');
            for(int source=0;source<4;source++)out.append(" SET_SOURCE ").append(ORIGINS[source])
                .append(' ').append(num(SET_ORIGIN[p][source])).append('\n');
            out.append(" WEAK_MEDIA_MATCH_UNIQUE_BY_SOURCE=")
                .append(Arrays.toString(MATCH_UNIQUE[p]))
                .append(" repeat_by_source=").append(Arrays.toString(MATCH_REPEAT[p]))
                .append(" value_mismatch=").append(MATCH_MISMATCH[p])
                .append(" evicted_weak_slots=").append(OBJECT_OVERFLOW[p]).append('\n');
            out.append(" TIMELINE_LAST=").append(TIMELINES[p].size())
                .append(" dropped_oldest=").append(TIMELINE_DROPPED[p]).append('\n');
            for(String e:TIMELINES[p])out.append(e).append('\n');
        }
        boolean sufficient=true,replicated=true;
        for(int p=0;p<PASS_COUNT;p++){
            int[] q=COUNTS[p][2];int valid=q[0]+q[1];
            if(valid<3){sufficient=false;replicated=false;}
            else if(p==1 ? q[0]<=q[1] : q[1]<=q[0])replicated=false;
        }
        out.append("A_B_A2_EXACT_D3R=").append(!sufficient?"INSUFFICIENT_SAMPLES":
            replicated?"REPLICATED_MANUAL_COHORT_ASSOCIATION":"NOT_REPLICATED")
            .append(" NOT_CAUSALITY\n")
            .append("SAME_REEL=UNVERIFIED FRIENDS_GATE=UNVERIFIED CAN_REPOST_JSON_LINK=UNVERIFIED\n");
        return out.toString();
    }
}
