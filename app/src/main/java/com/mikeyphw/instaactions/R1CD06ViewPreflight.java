package com.mikeyphw.instaactions;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import java.lang.reflect.Method;
import java.util.ArrayDeque;

/**
 * D06 deep diagnostics: independent, content-free structural observation of
 * Instagram's live viewer, sampled from the IA FAB on the Android main thread.
 * NO Media reference, account, class name, view ID/tag/text, method hook, or
 * action leaves this class. The scan cannot qualify visible-media identity.
 */
final class R1CD06ViewPreflight {
    static final int MAX_NODES = 384;
    static final long MAX_MS = 20L;
    static final int MAX_SAMPLES_PER_WINDOW = 12;
    private static final String PAGER = "androidx.viewpager2.widget.ViewPager2";
    private static final String RECYCLER = "androidx.recyclerview.widget.RecyclerView";
    enum Status {
        NO_ACTIVE_ACTIVITY, OFF_MAIN_THREAD, NO_VISIBLE_DECOR,
        NODE_LIMIT, TIME_LIMIT, NO_VIEWPAGER2, NO_VERTICAL_PAGER,
        VERTICAL_PAGER_OFFCENTER, PAGER_TOO_SMALL, AMBIGUOUS_PAGERS,
        NO_RECYCLER, AMBIGUOUS_RECYCLERS, NO_CENTERED_PAGE,
        AMBIGUOUS_CENTERED_PAGES, PAGER_QUERY_FAILED, VIEWER_STRUCTURE_FOUND
    }
    static final class Snapshot {
        final Status status;
        final int visited, shown, pagers, vertical, centered, eligible, recyclers, centeredPages;
        final int durationMs;
        Snapshot(Status status, int visited, int shown, int pagers, int vertical,
                int centered, int eligible, int recyclers, int centeredPages, int durationMs) {
            this.status = status;
            this.visited=cap(visited); this.shown=cap(shown); this.pagers=cap(pagers);
            this.vertical=cap(vertical); this.centered=cap(centered); this.eligible=cap(eligible);
            this.recyclers=cap(recyclers); this.centeredPages=cap(centeredPages);
            this.durationMs=cap(durationMs);
        }
        private static int cap(int n) { return Math.max(0,Math.min(2048,n)); }
    }
    private R1CD06ViewPreflight() { }
    private static boolean hasType(Class<?> type, String name) {
        for (Class<?> c=type; c!=null; c=c.getSuperclass())
            if (name.equals(c.getName())) return true;
        return false;
    }
    private static int orientation(View pager) throws Exception {
        Method method=pager.getClass().getMethod("getOrientation");
        if (method.getParameterCount()!=0 || method.getReturnType()!=int.class)
            throw new IllegalStateException("unexpected_viewpager_api");
        return (Integer)method.invoke(pager);
    }
    private static boolean coversCenter(Rect r, Rect bounds) {
        return r.contains(bounds.centerX(),bounds.centerY());
    }
    private static Snapshot snap(Status s, int[] n, long start) {
        int ms=(int)Math.max(0,Math.min(2048,SystemClock.uptimeMillis()-start));
        return new Snapshot(s,n[0],n[1],n[2],n[3],n[4],n[5],n[6],n[7],ms);
    }
    static Snapshot inspect(Activity activity) {
        long started=SystemClock.uptimeMillis();
        int[] n=new int[8];
        if (Looper.myLooper()!=Looper.getMainLooper()) return snap(Status.OFF_MAIN_THREAD,n,started);
        if (activity==null || activity.isFinishing() || activity.isDestroyed()
                || !ModuleConstants.TARGET_PACKAGE.equals(activity.getPackageName())
                || !R1BNavigationPolicy.isMainActivity(activity.getClass().getName()))
            return snap(Status.NO_ACTIVE_ACTIVITY,n,started);
        View decor=activity.getWindow()==null?null:activity.getWindow().getDecorView();
        Rect bounds=new Rect();
        if (!(decor instanceof ViewGroup) || !decor.getGlobalVisibleRect(bounds)
                || bounds.width()<=0 || bounds.height()<=0)
            return snap(Status.NO_VISIBLE_DECOR,n,started);
        View selectedPager=null;
        try {
            ArrayDeque<View> q=new ArrayDeque<>();
            q.add(decor);
            while(!q.isEmpty()) {
                if (n[0]>=MAX_NODES) return snap(Status.NODE_LIMIT,n,started);
                if (SystemClock.uptimeMillis()-started>MAX_MS) return snap(Status.TIME_LIMIT,n,started);
                View v=q.removeFirst(); n[0]++;
                if (!v.isShown())continue;
                Rect r=new Rect();
                if (!v.getGlobalVisibleRect(r))continue;
                n[1]++;
                if (hasType(v.getClass(),PAGER)) {
                    n[2]++;
                    int orient=orientation(v);
                    if (orient==1) {
                        n[3]++;
                        if (coversCenter(r,bounds)) {
                            n[4]++;
                            if (r.width()*10>=bounds.width()*7 && r.height()*10>=bounds.height()*6) {
                                n[5]++;
                                selectedPager=v;
                            }
                        }
                    }
                }
                if (v instanceof ViewGroup) {
                    ViewGroup g=(ViewGroup)v;
                    for(int i=0;i<g.getChildCount();i++) {
                        View child=g.getChildAt(i);
                        if(child!=null)q.addLast(child);
                    }
                }
            }
            if(n[2]==0)return snap(Status.NO_VIEWPAGER2,n,started);
            if(n[3]==0)return snap(Status.NO_VERTICAL_PAGER,n,started);
            if(n[5]==0)return snap(n[4]>0 ? Status.PAGER_TOO_SMALL
                    : Status.VERTICAL_PAGER_OFFCENTER,n,started);
            if(n[5]!=1)return snap(Status.AMBIGUOUS_PAGERS,n,started);
            if (!(selectedPager instanceof ViewGroup))return snap(Status.NO_RECYCLER,n,started);
            ViewGroup pager=(ViewGroup)selectedPager;
            for(int i=0;i<pager.getChildCount();i++) {
                View v=pager.getChildAt(i);
                if (v!=null && hasType(v.getClass(),RECYCLER) && v instanceof ViewGroup) {
                    n[6]++;
                    ViewGroup recycler=(ViewGroup)v;
                    for(int k=0;k<recycler.getChildCount();k++) {
                        View page=recycler.getChildAt(k);
                        Rect rect=new Rect();
                        if(page!=null && page.isShown() && page.getGlobalVisibleRect(rect)
                                && coversCenter(rect,bounds))n[7]++;
                    }
                }
            }
            if(n[6]==0)return snap(Status.NO_RECYCLER,n,started);
            if(n[6]>1)return snap(Status.AMBIGUOUS_RECYCLERS,n,started);
            if(n[7]==0)return snap(Status.NO_CENTERED_PAGE,n,started);
            if(n[7]>1)return snap(Status.AMBIGUOUS_CENTERED_PAGES,n,started);
            return snap(Status.VIEWER_STRUCTURE_FOUND,n,started);
        } catch(Throwable failure) {return snap(Status.PAGER_QUERY_FAILED,n,started);}
    }
}
