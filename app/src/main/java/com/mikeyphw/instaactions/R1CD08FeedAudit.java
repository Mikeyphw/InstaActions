package com.mikeyphw.instaactions;

import android.app.Activity;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayDeque;
import java.util.Locale;

/**
 * D08A bounded, user-initiated, read-only visible Repost-label comparison.
 * Manual surface names are NOT verified media identity. Never invokes a Feed
 * action, installs a Feed hook, or exports View contents/media/account data.
 */
final class R1CD08FeedAudit {
    private static final int MAX_VIEWS=512;
    private static final long MAX_MS=30L;
    private static final int[][] SAMPLES=new int[2][3];
    private static final int[][] LABELS=new int[2][3];
    private static final int[][] UNCERTAIN=new int[2][3];
    private static final int[][] TRUNCATED=new int[2][3];
    private static final int[] SESSION_COUNT=new int[2];
    private static int activeCohort=-1;
    private static final String[] NAMES={"ordinary_feed_post","feed_opened_reel","dedicated_reels_tab"};
    private R1CD08FeedAudit() { }

    static synchronized void beginCohort(int cohort) {
        if(cohort<0 || cohort>1)throw new IllegalArgumentException("manual cohort");
        activeCohort=cohort;SESSION_COUNT[cohort]++;
    }
    static synchronized void stopCohort(){activeCohort=-1;}
    static String snapshot(Activity activity, View panel, View chip, int kind) {
        if(!R1CD09DiagnosticGate.active(R1CD09DiagnosticGate.Mode.FEED))
            return "REFUSED: start Feed diagnostic first";
        int cohort;
        synchronized(R1CD08FeedAudit.class){cohort=activeCohort;}
        if(cohort<0)return "REFUSED: select manual affected/unaffected cohort";
        if(kind<0 || kind>=NAMES.length)return "REFUSED: unknown surface";
        if(R1CD07Recovery.isEnabled())return "REFUSED: turn Recovery OFF first";
        if(!R1CD04PassiveProbe.isInstalled())return "REFUSED: exact IG455 D04 attestation missing";
        if(activity==null || Looper.myLooper()!=Looper.getMainLooper())
            return "REFUSED: foreground UI thread required";
        R1BNavigationPolicy.Screen screen=R1BContextMonitor.stableScreen();
        // A claim of Feed-opened Reel is not automatically distinguishable from
        // the native Reels tab. This is recorded as manually claimed only.
        boolean expected=kind==0?screen==R1BNavigationPolicy.Screen.HOME_FEED:
                kind==2?screen==R1BNavigationPolicy.Screen.REELS:
                screen==R1BNavigationPolicy.Screen.REELS || screen==R1BNavigationPolicy.Screen.HOME_FEED;
        int count=0,visited=0;
        boolean capped=false;
        try {
            View root=activity.getWindow().getDecorView();
            ArrayDeque<View> queue=new ArrayDeque<>();queue.add(root);
            long deadline=SystemClock.uptimeMillis()+MAX_MS;
            while(!queue.isEmpty()) {
                if(visited>=MAX_VIEWS || SystemClock.uptimeMillis()>deadline) {capped=true;break;}
                View view=queue.removeFirst();
                if(view==panel || view==chip || !view.isShown())continue;
                visited++;
                if(matches(view.getContentDescription()))count++;
                else if(view instanceof TextView && matches(((TextView)view).getText()))count++;
                if(view instanceof ViewGroup) {
                    ViewGroup group=(ViewGroup)view;
                    for(int i=0;i<group.getChildCount();i++) {
                        View child=group.getChildAt(i);
                        if(child!=null)queue.addLast(child);
                    }
                }
            }
        } catch(Throwable failed) {return "REFUSED: UI scan failed; no observation stored";}
        synchronized(R1CD08FeedAudit.class){
            if(SAMPLES[cohort][kind]>=24)return "REFUSED: 24-sample cap reached";
            SAMPLES[cohort][kind]++;
            LABELS[cohort][kind]+=count;
            if(!expected)UNCERTAIN[cohort][kind]++;
            if(capped)TRUNCATED[cohort][kind]++;
        }
        return "D08A manual cohort " + (cohort==0?"AFFECTED":"UNAFFECTED") + " · " + NAMES[kind] + ": native visible Repost labels=" + count
                + " · screen hint=" + screen
                + (expected?" · manual surface claim":" · MISMATCH/UNKNOWN")
                + (capped?" · SCAN TRUNCATED":"")
                + " · eligibility and exact media NOT VERIFIED";
    }
    private static boolean matches(CharSequence value) {
        if(value==null)return false;
        // Never store the source string; compare against fixed UI words only.
        String v=value.toString().trim().toLowerCase(Locale.ROOT);
        return "repost".equals(v) || "repostar".equals(v)
                || "republicar".equals(v);
    }
    static synchronized void clear(){
        activeCohort=-1;
        for(int a=0;a<2;a++){
            java.util.Arrays.fill(SAMPLES[a],0);
            java.util.Arrays.fill(LABELS[a],0);
            java.util.Arrays.fill(UNCERTAIN[a],0);
            java.util.Arrays.fill(TRUNCATED[a],0);
            SESSION_COUNT[a]=0;
        }
    }
    static synchronized String report() {
        StringBuilder out=new StringBuilder("D08A FEED REPOST VISIBLE-LABEL AUDIT (manual surfaces)\n");
        for(int a=0;a<2;a++) {
            out.append(a==0?" MANUAL_AFFECTED":" MANUAL_UNAFFECTED")
                .append(" sessions=").append(SESSION_COUNT[a]).append('\n');
            for(int i=0;i<NAMES.length;i++)out.append(' ').append(NAMES[i])
                .append(" snapshots=").append(SAMPLES[a][i])
                .append(" repost_labels=").append(LABELS[a][i])
                .append(" screen_mismatch=").append(UNCERTAIN[a][i])
                .append(" scan_truncated=").append(TRUNCATED[a][i]).append('\n');
        }
        out.append("Feed Repost and Feed-opened Reels NOT QUALIFIED; no eligibility hook, native action or identifiers.\n");
        return out.toString();
    }
}
