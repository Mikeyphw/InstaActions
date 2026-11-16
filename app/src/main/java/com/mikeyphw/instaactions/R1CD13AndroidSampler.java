package com.mikeyphw.instaactions;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;

/** Passive auto-discovery from R1B stable screens and exact Instagram view IDs.
 * No Instagram method hooks, labels, media objects, account fields or IDs.
 * Bounded to 512 views / 24 ms per sample, every 1.5 seconds, UI thread only.
 */
final class R1CD13AndroidSampler {
    private static final Handler MAIN=new Handler(Looper.getMainLooper());
    private static final int MAX_VIEWS=512;
    private static final long MAX_MS=24;
    private static WeakReference<Activity> active=new WeakReference<>(null);
    private static boolean scheduled;
    private static final Runnable SAMPLE=new Runnable(){
        @Override public void run() {
            scheduled=false;
            Activity owner=active.get();
            if(owner==null || owner.isFinishing() || owner.isDestroyed()){
                R1CD13AutoSurface.notAvailable("NO_ACTIVE_ACTIVITY");return;
            }
            if(!ModuleConstants.TARGET_PACKAGE.equals(owner.getPackageName()))return;
            if(!R1CD04PassiveProbe.isInstalled()){
                R1CD13AutoSurface.notAvailable("NO_SUPPORTED_APK");schedule();return;
            }
            try {sample(owner);} catch(Throwable ignored){
                R1CD13AutoSurface.notAvailable("OBSERVER_INACTIVE");
            }
            schedule();
        }
    };
    private R1CD13AndroidSampler(){ }
    static void resumed(Activity activity){
        if(activity==null || !ModuleConstants.TARGET_PACKAGE.equals(activity.getPackageName()))return;
        if(Looper.myLooper()!=Looper.getMainLooper())return;
        if(active.get()==activity){schedule();return;}
        active=new WeakReference<>(activity);
        MAIN.removeCallbacks(SAMPLE);scheduled=false;
        schedule();
    }
    static void paused(Activity activity){
        if(activity!=null && active.get()==activity){
            MAIN.removeCallbacks(SAMPLE);scheduled=false;
            active.clear();active=new WeakReference<>(null);
            R1CD13AutoSurface.notAvailable("ACTIVITY_PAUSED");
        }
    }
    private static void schedule(){
        if(!scheduled && active.get()!=null){scheduled=true;MAIN.postDelayed(SAMPLE,1500L);}
    }
    private static boolean selected(View view){
        return view.isSelected() || view.isActivated() ||
            view instanceof Checkable && ((Checkable)view).isChecked();
    }
    private static void sample(Activity activity){
        View root=activity.getWindow()==null?null:activity.getWindow().getDecorView();
        if(root==null || !root.isShown()){
            R1CD13AutoSurface.notAvailable("OBSERVER_INACTIVE");return;
        }
        boolean feed=false, clips=false, truncated=false;
        int seen=0;long deadline=SystemClock.uptimeMillis()+MAX_MS;
        ArrayDeque<View> queue=new ArrayDeque<>();queue.addLast(root);
        while(!queue.isEmpty()){
            if(++seen>MAX_VIEWS || SystemClock.uptimeMillis()>deadline){truncated=true;break;}
            View v=queue.removeFirst();
            if(v.getVisibility()!=View.VISIBLE || !v.isShown())continue;
            int id=v.getId();
            if(id!=View.NO_ID){
                try {
                    Resources res=v.getResources();
                    if(ModuleConstants.TARGET_PACKAGE.equals(res.getResourcePackageName(id))){
                        String name=res.getResourceEntryName(id);
                        if("feed_tab".equals(name) && selected(v))feed=true;
                        if("clips_tab".equals(name) && selected(v))clips=true;
                    }
                }catch(Resources.NotFoundException ignored){ }
            }
            if(v instanceof ViewGroup){
                ViewGroup g=(ViewGroup)v;
                for(int i=0;i<g.getChildCount();i++){
                    View child=g.getChildAt(i);if(child!=null)queue.addLast(child);
                }
            }
        }
        R1CD13AutoSurface.observe(R1BContextMonitor.stableScreen().name(),
            R1BNavigationPolicy.isMainActivity(activity.getClass().getName()),
            feed,clips,truncated);
    }
}
