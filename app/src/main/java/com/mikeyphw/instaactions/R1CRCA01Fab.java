package com.mikeyphw.instaactions;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.lang.ref.WeakReference;

/**
 * RCA01-only floating diagnostics. Replaces (does not invoke) the D05/D07 FAB.
 * No Instagram click handlers, configuration overrides, network or account IDs.
 * The labels A/B are selected by the tester, never verified account identities.
 */
final class R1CRCA01Fab {
    private static final Handler MAIN=new Handler(Looper.getMainLooper());
    private static final int BG=0xff162237, FG=0xfff7fafc, ACCENT=0xff76c9ff;
    private static R1CRCA01Fab current;
    private static int phase; // 0 idle, 1 A recording, 2 A stopped, 3 B recording, 4 finished
    private final WeakReference<Activity> activity;
    private FrameLayout host;
    private Button chip;
    private ScrollView panel;
    private TextView summary;
    private Runnable ticker;
    private String last="Use the same public Reel: A -> B -> A (new capture).";
    private boolean detached;
    private int retries;
    private static final long[] RETRY_DELAYS={140L,420L,1000L};
    private R1CRCA01Fab(Activity owner) { activity=new WeakReference<>(owner); }

    private int dp(int n) {
        Activity a=activity.get();
        return Math.round(n*(a==null?1f:a.getResources().getDisplayMetrics().density));
    }
    static void onResumed(Activity owner) {
        if(owner==null || !ModuleConstants.TARGET_PACKAGE.equals(owner.getPackageName()) ||
                owner.isFinishing() || owner.isDestroyed())return;
        if(Looper.myLooper()!=Looper.getMainLooper()) {
            WeakReference<Activity> weak=new WeakReference<>(owner);
            MAIN.post(()->{Activity a=weak.get();if(a!=null)onResumed(a);});
            return;
        }
        if(current!=null && current.activity.get()==owner && current.healthy())return;
        if(current!=null)current.detach();
        R1CD07Recovery.off("RCA01 diagnostic-only UI; native Repost override disabled");
        R1CRCA01Fab next=new R1CRCA01Fab(owner);
        current=next;
        next.attach();
    }
    static void onLeaving(Activity owner) {
        if(Looper.myLooper()!=Looper.getMainLooper()) {
            WeakReference<Activity> weak=new WeakReference<>(owner);
            MAIN.post(()->{Activity a=weak.get();if(a!=null)onLeaving(a);});
            return;
        }
        if(current!=null && current.activity.get()==owner) {
            current.detach();current=null;
        }
    }
    static String attachStatus(){return current!=null && current.healthy()?"RCA01_DIAGNOSTIC_FAB_ATTACHED":"RCA01_FAB_NOT_ATTACHED";}
    private boolean healthy(){return !detached && host!=null && chip!=null && chip.getParent()==host;}
    private void scheduleRetry(){
        if(detached || retries>=RETRY_DELAYS.length)return;
        long delay=RETRY_DELAYS[retries++];
        MAIN.postDelayed(()->{
            Activity a=activity.get();
            if(!detached && current==this && a!=null && !a.isFinishing() &&
                    !a.isDestroyed() && !healthy())attach();
        },delay);
    }
    private void attach() {
        Activity a=activity.get();if(a==null || detached)return;
        try {
            R1CD16AnchorPolicy.Decision main=R1CD16MainSurface.inspect(a);
            if(main!=R1CD16AnchorPolicy.Decision.MAIN_CLASS &&
                    main!=R1CD16AnchorPolicy.Decision.VERIFIED_MAIN_ANCHORS){
                last="Main screen not yet verified; retry bounded";scheduleRetry();return;
            }
            View content=a.findViewById(android.R.id.content);
            if(!(content instanceof FrameLayout)) {last="No safe FrameLayout host";scheduleRetry();return;}
            host=(FrameLayout)content;
            chip=new Button(a);
            chip.setText("RCA01");chip.setAllCaps(false);chip.setTextColor(FG);
            chip.setBackgroundColor(BG);chip.setElevation(dp(9));
            FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(dp(92),dp(52),Gravity.BOTTOM|Gravity.END);
            cp.setMargins(dp(12),dp(12),dp(12),dp(98));
            host.addView(chip,cp);
            chip.setOnClickListener(v->{
                if(panel==null)createPanel();
                if(panel!=null){panel.setVisibility(panel.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);refresh();}
            });
            ticker=new Runnable() {
                @Override public void run(){
                    if(detached || current!=R1CRCA01Fab.this || !healthy())return;
                    Activity currentActivity=activity.get();
                    if(currentActivity!=null)InstaActionsEntry.requestD07(currentActivity);
                    // RCA01 must not inherit a prior in-process manual recovery lease.
                    if(R1CD07Recovery.isEnabled())R1CD07Recovery.off("RCA01 read-only enforcement");
                    refresh();
                    MAIN.postDelayed(this,1000L);
                }
            };
            MAIN.post(ticker);
            android.util.Log.i("INA_RCA01","INA_RCA01_FAB_ATTACHED old_fab=disabled actions=off");
        }catch(Throwable failure){last="Attach failed: "+failure.getClass().getSimpleName();detach();}
    }
    private TextView text(LinearLayout parent,String value,int size) {
        Activity a=activity.get();TextView tv=new TextView(a);
        tv.setText(value);tv.setTextColor(FG);tv.setTextSize(size);
        tv.setPadding(dp(8),dp(6),dp(8),dp(6));parent.addView(tv);return tv;
    }
    private void action(LinearLayout parent,String caption,Runnable perform) {
        Activity a=activity.get();Button b=new Button(a);b.setText(caption);
        b.setAllCaps(false);b.setTextColor(FG);b.setBackgroundColor(0xff31475f);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(49));lp.bottomMargin=dp(5);
        parent.addView(b,lp);b.setOnClickListener(v->{perform.run();refresh();});
    }
    private void createPanel() {
        Activity a=activity.get();if(a==null || host==null)return;
        panel=new ScrollView(a);panel.setFillViewport(false);panel.setBackgroundColor(BG);
        panel.setElevation(dp(12));
        FrameLayout.LayoutParams pp=new FrameLayout.LayoutParams(dp(306),
                Math.min(dp(430),Math.max(dp(230),a.getResources().getDisplayMetrics().heightPixels-dp(145))),
                Gravity.BOTTOM|Gravity.END);
        pp.setMargins(dp(8),dp(8),dp(9),dp(155));
        LinearLayout body=new LinearLayout(a);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(8),dp(8),dp(8),dp(8));
        panel.addView(body);
        text(body,"RCA01 · Repost gate diagnostics",17).setTextColor(ACCENT);
        text(body,"READ ONLY. No Repost override or automated actions. A/B are manual labels; switch accounts only after Stop A.",12);
        summary=text(body,"",12);
        action(body,"Start A · affected account",()->{
            if(phase==1 || phase==3){last="Stop current capture first";return;}
            if(!R1CD07GateTrace.ready()){last="Native observers not ready; browse Reels and retry";return;}
            R1CRCA01GateLedger.clear();R1CD07GateTrace.clear();
            last=R1CD07GateTrace.begin(0);phase=1;
        });
        action(body,"Stop A · keep samples",()->{
            if(phase!=1){last="Start A first";return;}
            last=R1CD07GateTrace.stop();phase=2;
        });
        action(body,"Start B · after manual account switch",()->{
            if(phase!=2){last="Stop A before selecting B";return;}
            if(!R1CD07GateTrace.ready()){last="Native observers unavailable";return;}
            R1CD09AccountSentinel.markManualSwitch();
            last=R1CD07GateTrace.begin(1);phase=3;
        });
        action(body,"Stop B · keep both samples",()->{
            if(phase!=3){last="Start B first";return;}
            last=R1CD07GateTrace.stop();phase=4;
        });
        action(body,"Copy RCA01 A/B report",()->{
            ClipboardManager cm=(ClipboardManager)a.getSystemService(Context.CLIPBOARD_SERVICE);
            if(cm==null){last="Clipboard unavailable";return;}
            cm.setPrimaryClip(ClipData.newPlainText("RCA01 Repost A/B read-only",report()));
            last="Copied bounded A/B report by explicit button press";
        });
        action(body,"Clear samples · OFF",()->{
            R1CD07Recovery.off("RCA01 reset");R1CD07GateTrace.clear();R1CRCA01GateLedger.clear();
            phase=0;last="All RCA01 A/B samples cleared";
        });
        action(body,"Close diagnostics",()->{panel.setVisibility(View.GONE);});
        host.addView(panel,pp);
    }
    private static String report(){return R1CRCA01GateLedger.report()+"\n--- NATIVE REPOST PREDICATE ---\n"+
            R1CD07GateTrace.reportCore()+"\n--- SOURCE HINTS ---\n"+R1CD14NativeOriginProbe.report()+
            "\nACCOUNT_IDENTITY=NOT_VERIFIED NO_AUTOMATIC_REPOST=TRUE\n";}
    private void refresh(){
        if(summary==null)return;
        String stage=phase==1?"A RECORDING":phase==2?"A STOPPED":phase==3?"B RECORDING":
                phase==4?"A/B COMPLETE":"IDLE";
        summary.setText("Stage: "+stage+"\nNative hooks: "+R1CD07GateTrace.status()+
                "\nAPK attested: "+R1CD04PassiveProbe.isInstalled()+
                "\nMedia field hooks: "+R1CRCA01GateLedger.mediaHooks()+
                "\nA events: "+R1CRCA01GateLedger.eventCount(0)+
                " · B events: "+R1CRCA01GateLedger.eventCount(1)+"\n"+last);
    }
    private void detach(){
        detached=true;
        if(ticker!=null)MAIN.removeCallbacks(ticker);
        if(host!=null){
            if(panel!=null && panel.getParent()==host)host.removeView(panel);
            if(chip!=null && chip.getParent()==host)host.removeView(chip);
        }
        panel=null;chip=null;summary=null;host=null;
    }
}
