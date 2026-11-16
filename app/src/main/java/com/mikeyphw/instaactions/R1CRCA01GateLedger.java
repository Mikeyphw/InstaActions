package com.mikeyphw.instaactions;

import java.util.ArrayDeque;

/**
 * RCA01: memory-only, read-only and MANUALLY labeled A/B field-origin evidence.
 * Neither Media/GraphQL object identities nor raw values, arguments, IDs, names,
 * keys, URLs, account objects, timestamps or content are retained.
 * These sites are observationally separate; sharing an int hash is NOT proof
 * they refer to one object or one upstream experiment.
 */
final class R1CRCA01GateLedger {
    enum Site {
        MEDIA_GET_ORIGINAL, MEDIA_SET_ARGUMENT_AFTER_SUCCESS,
        EXACT_D3R_ORIGINAL,
        MOBILECONFIG_FIRST_KEY_ORIGINAL,
        REPOST_UI_ORIGINAL_SINGLETON, REPOST_UI_ORIGINAL_PAYLOAD,
        REPOST_UI_ORIGINAL_OTHER
    }
    private static final int MAX_EVENTS_PER_COHORT=4096;
    private static final int MAX_TIMELINE=96;
    // Outcome ordinal: true, false, null, non-Boolean. UI events are classed
    // by source name, and counted with outcome OTHER rather than fake booleans.
    private static final int[][][] COUNTS=new int[2][Site.values().length][4];
    private static final int[] EVENTS={0,0};
    private static final ArrayDeque<String> ORDERED=new ArrayDeque<>();
    private static int dropped;
    private static long ordinal;
    private static String mediaHooks="NOT_INSTALLED";
    private R1CRCA01GateLedger() { }

    static int outcome(Object value) {
        if (Boolean.TRUE.equals(value)) return 0;
        if (Boolean.FALSE.equals(value)) return 1;
        return value==null ? 2 : 3;
    }
    static synchronized void mediaHooks(String status) {
        if (status!=null && status.matches("READY_GET_AND_SET|PARTIAL_GET_ONLY|PARTIAL_SET_ONLY|UNAVAILABLE"))
            mediaHooks=status;
    }
    static synchronized String mediaHooks() {return mediaHooks;}
    static synchronized void record(int cohort, Site site, Object original) {
        if (cohort<0 || cohort>1 || site==null || EVENTS[cohort]>=MAX_EVENTS_PER_COHORT) return;
        int result=outcome(original);
        recordOutcome(cohort,site,result);
    }
    static synchronized void recordUi(int cohort, int nativeShape) {
        if (cohort<0 || cohort>1 || EVENTS[cohort]>=MAX_EVENTS_PER_COHORT)return;
        Site site=nativeShape==0 ? Site.REPOST_UI_ORIGINAL_SINGLETON :
            nativeShape==1 ? Site.REPOST_UI_ORIGINAL_PAYLOAD :
                Site.REPOST_UI_ORIGINAL_OTHER;
        recordOutcome(cohort,site,3);
    }
    private static void recordOutcome(int cohort, Site site, int result) {
        EVENTS[cohort]++;
        COUNTS[cohort][site.ordinal()][result]++;
        String label=result==0?"T":result==1?"F":result==2?"NULL":"OTHER";
        String event="#"+(++ordinal)+" "+(cohort==0?"A":"B")+" "+site.name()+"="+label;
        if(ORDERED.size()>=MAX_TIMELINE){ORDERED.removeFirst();dropped++;}
        ORDERED.addLast(event);
    }
    static synchronized int count(int cohort, Site site, int outcome) {
        return COUNTS[cohort][site.ordinal()][outcome];
    }
    static synchronized int eventCount(int cohort) {return EVENTS[cohort];}
    static synchronized void clear() {
        for(int[][] phase:COUNTS)for(int[] site:phase)java.util.Arrays.fill(site,0);
        EVENTS[0]=0;EVENTS[1]=0;ORDERED.clear();dropped=0;ordinal=0;
        // Keep installation status: clearing samples never uninstalls hooks.
    }
    static synchronized String report() {
        StringBuilder out=new StringBuilder("RCA01 READ-ONLY REPOST FIELD ORIGIN A/B\n");
        out.append("MEDIA_HOOKS=").append(mediaHooks)
           .append(" ACCOUNT_BINDING=MANUAL_LABEL_ONLY REPOST_INTERVENTION=EXCLUDED\n")
           .append("Same key in Media LiveTree and GraphQL TypeModelData does NOT prove same object, field schema, server flag or rollout.\n")
           .append("SET is observed argument AFTER method returned; not proof that value persisted.\n")
           .append("A and B may contain different Media and delayed async callbacks; compare the SAME Reel and repeat A-B-A.\n");
        for(int a=0;a<2;a++){
            out.append("manual_").append(a==0?'A':'B').append(" samples=").append(EVENTS[a]);
            if(EVENTS[a]>=MAX_EVENTS_PER_COHORT)out.append(" CAPPED");
            out.append('\n');
            for(Site site:Site.values()){
                int[] n=COUNTS[a][site.ordinal()];
                out.append(" ").append(site.name()).append(" true=").append(n[0])
                   .append(" false=").append(n[1]).append(" null=").append(n[2])
                   .append(" other=").append(n[3]).append('\n');
            }
        }
        out.append("A_B_DIFFERENCE: ");
        boolean found=false;
        for(Site site:Site.values()){
            int[] a=COUNTS[0][site.ordinal()],b=COUNTS[1][site.ordinal()];
            int aa=a[0]+a[1],bb=b[0]+b[1];
            if(aa>=3 && bb>=3 && ((a[0]>a[1] && b[0]<b[1]) ||
                   (a[0]<a[1] && b[0]>b[1]))) {
                if(found)out.append(",");
                out.append(site.name()).append("=opposing_boolean_majority");found=true;
            }
        }
        if(!found)out.append("NOT_ESTABLISHED_OR_INSUFFICIENT_SAMPLES");
        out.append(" (association only, NOT causality)\n");
        out.append("ORDERED_SITE_EVENTS last=").append(ORDERED.size())
           .append(" dropped_oldest=").append(dropped).append('\n');
        for(String e:ORDERED)out.append(e).append('\n');
        out.append("SERVER_ORIGIN=UNVERIFIED CAN_REPOST_JSON_LINK=UNVERIFIED ")
           .append("FRIENDS_SHARED_GATE=UNVERIFIED MEDIA_IDENTITY=NOT_VERIFIED\n");
        return out.toString();
    }
}
