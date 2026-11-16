package com.mikeyphw.instaactions;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.lang.ref.WeakReference;

/**
 * Current RCA02-only floating diagnostics (retains RCA01 source filename so no
 * obsolete overlay/delete chain is needed). Legacy D05/D07 FAB remains detached.
 * No account IDs, network access, Repost clicks or eligibility override.
 */
final class R1CRCA01Fab {
    private static final Handler MAIN=new Handler(Looper.getMainLooper());
    private static final int BG=0xff162237, FG=0xfff7fafc, ACCENT=0xff76c9ff;
    private static R1CRCA01Fab current;
    // 0 idle, 1 A1 live, 2 A1 stopped, 3 B confirmed, 4 B live,
    // 5 B stopped, 6 A2 confirmed, 7 A2 live, 8 finished.
    private static int phase;
    private static final String[] NATIVE_SNAPSHOT=new String[3];
    private final WeakReference<Activity> activity;
    private FrameLayout host;
    private Button chip;
    private ScrollView panel;
    private TextView summary;
    private Runnable ticker;
    private String last="Use SAME public Reel: A1 → B → A2. Accounts manually labeled.";
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
        R1CD07Recovery.off("RCA02 diagnostic-only UI; native Repost override disabled");
        R1CRCA01Fab next=new R1CRCA01Fab(owner);
        current=next;
        next.attach();
    }
    static void onLeaving(Activity owner) {
        if(owner==null)return;
        if(Looper.myLooper()!=Looper.getMainLooper()) {
            WeakReference<Activity> weak=new WeakReference<>(owner);
            MAIN.post(()->{Activity a=weak.get();if(a!=null)onLeaving(a);});
            return;
        }
        if(current!=null && current.activity.get()==owner){current.detach();current=null;}
    }
    static String attachStatus(){return current!=null && current.healthy()?"RCA02_DIAGNOSTIC_FAB_ATTACHED":"RCA02_FAB_NOT_ATTACHED";}
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
                last="Instagram main screen not verified; bounded retry";scheduleRetry();return;
            }
            View content=a.findViewById(android.R.id.content);
            if(!(content instanceof FrameLayout)){last="No safe FrameLayout host";scheduleRetry();return;}
            host=(FrameLayout)content;
            chip=new Button(a);
            chip.setText("RCA02");chip.setAllCaps(false);chip.setTextColor(FG);
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
                    if(R1CD07Recovery.isEnabled())R1CD07Recovery.off("RCA02 read-only enforcement");
                    refresh();
                    MAIN.postDelayed(this,1000L);
                }
            };
            MAIN.post(ticker);
            android.util.Log.i("INA_RCA02","INA_RCA02_FAB_ATTACHED old_fab=disabled actions=off");
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
    private void beginPass(int pass,int newPhase){
        if(!R1CD07GateTrace.ready()){last="Original Repost hooks not ready; browse Reels, then retry";return;}
        if(pass==0){
            R1CD07GateTrace.clear();R1CRCA01GateLedger.clear();R1CRCA02LineageLedger.reset();
            for(int i=0;i<NATIVE_SNAPSHOT.length;i++)NATIVE_SNAPSHOT[i]=null;
        }
        String nativeStatus=R1CD07GateTrace.begin(pass==1?1:0);
        if(!nativeStatus.startsWith("INA D07 recording")){last=nativeStatus;return;}
        String started=R1CRCA02LineageLedger.begin(pass);
        if(started.startsWith("REFUSED")){R1CD07GateTrace.stop();last=started;return;}
        phase=newPhase;last=started;
    }
    private void stopPass(int pass,int completedPhase){
        if(R1CRCA02LineageLedger.activePass()!=pass){last="RCA02 pass not recording";return;}
        last=R1CRCA02LineageLedger.stop();
        R1CD07GateTrace.stop();
        String original=R1CD07GateTrace.reportCore();
        final int max=14000;
        NATIVE_SNAPSHOT[pass]=original.length()>max?original.substring(0,max)+"\n[SNAPSHOT_TRUNCATED]":original;
        phase=completedPhase;
    }
    private void createPanel() {
        Activity a=activity.get();if(a==null || host==null)return;
        panel=new ScrollView(a);panel.setFillViewport(false);panel.setBackgroundColor(BG);
        panel.setElevation(dp(12));
        FrameLayout.LayoutParams pp=new FrameLayout.LayoutParams(dp(310),
                Math.min(dp(480),Math.max(dp(230),a.getResources().getDisplayMetrics().heightPixels-dp(145))),
                Gravity.BOTTOM|Gravity.END);
        pp.setMargins(dp(8),dp(8),dp(9),dp(155));
        LinearLayout body=new LinearLayout(a);body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(8),dp(8),dp(8),dp(8));panel.addView(body);
        text(body,"RCA02 · Repost field provenance",17).setTextColor(ACCENT);
        text(body,"READ ONLY · A1/B/A2 are manually selected, not verified account IDs. Use the SAME public Reel. No Repost override.",12);
        summary=text(body,"",12);
        action(body,"Start A1 · affected account",()->{
            if(phase!=0){last="Clear previous run first";return;}
            beginPass(0,1);
        });
        action(body,"Stop A1 · preserve original",()->{
            if(phase!=1){last="Start A1 first";return;}
            stopPass(0,2);
        });
        action(body,"Confirm manual switch to B",()->{
            if(phase!=2){last="Stop A1 before switching";return;}
            R1CD09AccountSentinel.markManualSwitch();phase=3;
            last="B switch manually confirmed; account identity NOT VERIFIED";
        });
        action(body,"Start B · unaffected account",()->{
            if(phase!=3){last="Confirm B switch first";return;}
            beginPass(1,4);
        });
        action(body,"Stop B · preserve original",()->{
            if(phase!=4){last="Start B first";return;}
            stopPass(1,5);
        });
        action(body,"Confirm manual return to A",()->{
            if(phase!=5){last="Stop B before switching";return;}
            R1CD09AccountSentinel.markManualSwitch();phase=6;
            last="A return manually confirmed; account identity NOT VERIFIED";
        });
        action(body,"Start A2 · affected account again",()->{
            if(phase!=6){last="Confirm A return first";return;}
            beginPass(2,7);
        });
        action(body,"Stop A2 · complete comparison",()->{
            if(phase!=7){last="Start A2 first";return;}
            stopPass(2,8);
        });
        action(body,"Copy RCA02 A1/B/A2 report",()->{
            ClipboardManager cm=(ClipboardManager)a.getSystemService(Context.CLIPBOARD_SERVICE);
            if(cm==null){last="Clipboard unavailable";return;}
            cm.setPrimaryClip(ClipData.newPlainText("RCA02 Repost A1/B/A2 read-only",report()));
            last="Report copied by EXPLICIT user press";
        });
        action(body,"Clear samples · Recovery OFF",()->{
            R1CD07Recovery.off("RCA02 reset");R1CD07GateTrace.clear();
            R1CRCA01GateLedger.clear();R1CRCA02LineageLedger.reset();
            for(int i=0;i<NATIVE_SNAPSHOT.length;i++)NATIVE_SNAPSHOT[i]=null;
            phase=0;last="All RCA02 captures cleared; recovery OFF";
        });
        action(body,"Close diagnostics",()->panel.setVisibility(View.GONE));
        host.addView(panel,pp);
    }
    private static String report(){
        StringBuilder out=new StringBuilder(R1CRCA02LineageLedger.report());
        String[] pass={"A1","B","A2"};
        for(int i=0;i<pass.length;i++){
            out.append("\n--- NATIVE REPOST SNAPSHOT ").append(pass[i]).append(" ---\n");
            out.append(NATIVE_SNAPSHOT[i]==null?"NOT_CAPTURED\n":NATIVE_SNAPSHOT[i]);
        }
        out.append("\n--- PASSIVE SOURCE HINTS ---\n")
            .append(R1CD14NativeOriginProbe.report())
            .append("\nACCOUNT_IDENTITY=NOT_VERIFIED SAME_REEL=NOT_PROVEN NO_AUTOMATIC_REPOST=TRUE\n");
        return out.toString();
    }
    private void refresh(){
        if(summary==null)return;
        String[] stages={"IDLE","A1 RECORDING","A1 STOPPED","B SWITCH CONFIRMED",
            "B RECORDING","B STOPPED","A2 RETURN CONFIRMED","A2 RECORDING","A1/B/A2 COMPLETE"};
        summary.setText("Stage: "+stages[Math.min(phase,stages.length-1)]+
            "\nNative hooks: "+R1CD07GateTrace.status()+
            "\nAPK attested: "+R1CD04PassiveProbe.isInstalled()+
            "\nSource writer hooks: "+R1CRCA02LineageLedger.writerStatus()+
            "\nA1/B/A2 events: "+R1CRCA02LineageLedger.events(0)+" / "+
              R1CRCA02LineageLedger.events(1)+" / "+R1CRCA02LineageLedger.events(2)+
            "\n"+last);
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
