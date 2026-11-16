package com.mikeyphw.instaactions;

import android.app.Activity;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * D06 HF02: two-phase, read-only menu-time reconciliation.
 * A normally visible Reel page is weakly referenced before the menu opens.
 * During the native menu callback the window may hide the pager, so this
 * verifier checks the previously visible page's OWN adapter position, not a
 * fresh isShown()/getGlobalVisibleRect() search. Based on InstaEclipse's
 * ReelsControlsHook pager binder and ReelDownloadHook selected-page rules.
 *
 * No Media reference is cached, no IDs or getters, and no Instagram methods
 * are invoked. A match is ONLY a candidate; never verified media identity.
 */
final class R1CD06PreMenuReconciliation {
    static final long MAX_AGE_MS = 3000L;
    static final long MAX_SCAN_MS = 20L;
    static final int MAX_PAGE_VIEWS = 160;
    enum Verdict {
        NOT_QUALIFIED, SNAPSHOT_MISSING, SNAPSHOT_EXPIRED, ACTIVITY_CHANGED,
        WRONG_STAGE, WEAK_VIEW_COLLECTED, PAGE_REBOUND, PAGE_INDEX_CHANGED,
        PAGE_STRUCTURE_FAILED, VIEW_SCAN_LIMIT, VIEW_SCAN_TIME_LIMIT,
        DIRECT_FIELD_LIMIT, NO_DIRECT_MEDIA, MEDIA_MISMATCH, MEDIA_AMBIGUOUS,
        REFLECTION_FAILED, PREMENU_MEDIA_MATCH_CANDIDATE
    }
    private static WeakReference<Activity> owner = new WeakReference<>(null);
    private static WeakReference<View> pagerRef = new WeakReference<>(null);
    private static WeakReference<ViewGroup> recyclerRef = new WeakReference<>(null);
    private static WeakReference<View> pageRef = new WeakReference<>(null);
    private static R1CD05FabSession.Step stage;
    private static int index = -1;
    private static long timestamp;
    private R1CD06PreMenuReconciliation() { }

    static synchronized void clear() {
        owner.clear();pagerRef.clear();recyclerRef.clear();pageRef.clear();
        owner = new WeakReference<>(null);
        pagerRef = new WeakReference<>(null);
        recyclerRef = new WeakReference<>(null);
        pageRef = new WeakReference<>(null);
        stage = null;index=-1;timestamp=0;
    }

    private static int getNumber(Object obj,String method,Class<?>[] types,Object... args)
            throws Exception {
        Method m=obj.getClass().getMethod(method,types);
        if (m.getReturnType()!=int.class)throw new IllegalStateException("bad_androidx_shape");
        return (Integer)m.invoke(obj,args);
    }

    /** Only called after structural preflight established one centered active page. */
    static synchronized void remember(Activity activity,View pager,ViewGroup recycler,
            View page,R1CD05FabSession.Step step) {
        if (Looper.myLooper()!=Looper.getMainLooper() || activity==null || pager==null
                || recycler==null || page==null || !isStage(step)) return;
        try {
            int current=getNumber(pager,"getCurrentItem",new Class<?>[0]);
            int position=getNumber(recycler,"getChildAdapterPosition",
                    new Class<?>[]{View.class},page);
            if (current<0 || current!=position || !isDirectChild(recycler,page)) {
                clear();return;
            }
            owner=new WeakReference<>(activity);
            pagerRef=new WeakReference<>(pager);
            recyclerRef=new WeakReference<>(recycler);
            pageRef=new WeakReference<>(page);
            stage=step;index=current;timestamp=SystemClock.uptimeMillis();
        } catch (Throwable failed) { clear(); }
    }

    private static boolean isStage(R1CD05FabSession.Step step) {
        return step==R1CD05FabSession.Step.A1 || step==R1CD05FabSession.Step.A2
                || step==R1CD05FabSession.Step.B1 || step==R1CD05FabSession.Step.B2;
    }
    private static boolean isDirectChild(ViewGroup group,View child) {
        for(int i=0;i<group.getChildCount();i++)if(group.getChildAt(i)==child)return true;
        return false;
    }

    static synchronized Verdict compare(Activity activity,Object media,Class<?> type,
            R1BNavigationPolicy.Screen screen,R1CD06MenuOwnership.Verdict ownership,
            R1CD05FabSession.Step nowStep) {
        if (screen!=R1BNavigationPolicy.Screen.REELS
                || !R1CD06VisiblePageBinding.isPositiveOwner(ownership)
                || media==null || type==null || !type.isInstance(media) || !isStage(nowStep))
            return Verdict.NOT_QUALIFIED;
        if (stage==null)return Verdict.SNAPSHOT_MISSING;
        if (stage!=nowStep)return Verdict.WRONG_STAGE;
        if (Looper.myLooper()!=Looper.getMainLooper())return Verdict.NOT_QUALIFIED;
        if (owner.get()!=activity)return Verdict.ACTIVITY_CHANGED;
        long now=SystemClock.uptimeMillis();
        if (R1CD06PreMenuPolicy.expired(timestamp,now,MAX_AGE_MS))
            return Verdict.SNAPSHOT_EXPIRED;
        View pager=pagerRef.get(),page=pageRef.get();
        ViewGroup recycler=recyclerRef.get();
        if (pager==null || page==null || recycler==null)return Verdict.WEAK_VIEW_COLLECTED;
        boolean attached=isDirectChild(recycler,page);
        if (!attached)return Verdict.PAGE_REBOUND;
        try {
            int current=getNumber(pager,"getCurrentItem",new Class<?>[0]);
            int childPos=getNumber(recycler,"getChildAdapterPosition",
                    new Class<?>[]{View.class},page);
            if (!R1CD06PreMenuPolicy.samePosition(index,current,childPos))
                return Verdict.PAGE_INDEX_CHANGED;
            // Hidden-by-overflow page views are allowed here because preflight
            // established the page when visible; NOT proof of current visibility.
            long start=SystemClock.uptimeMillis();
            ArrayDeque<View> q=new ArrayDeque<>();
            List<Object> holders=new ArrayList<>();
            q.addLast(page);
            while(!q.isEmpty()) {
                if (holders.size()>=MAX_PAGE_VIEWS)return Verdict.VIEW_SCAN_LIMIT;
                if (SystemClock.uptimeMillis()-start>MAX_SCAN_MS)
                    return Verdict.VIEW_SCAN_TIME_LIMIT;
                View v=q.removeFirst();
                holders.add(v);
                if(v instanceof ViewGroup) {
                    ViewGroup g=(ViewGroup)v;
                    for(int i=0;i<g.getChildCount();i++) {
                        View child=g.getChildAt(i);if(child!=null)q.addLast(child);
                    }
                }
            }
            R1CD06DirectViewMedia.Result result=R1CD06DirectViewMedia.compare(
                    holders.toArray(new Object[0]),type,media);
            if (SystemClock.uptimeMillis()-start>MAX_SCAN_MS)
                return Verdict.VIEW_SCAN_TIME_LIMIT;
            switch(result) {
                case MATCH:return Verdict.PREMENU_MEDIA_MATCH_CANDIDATE;
                case ABSENT:return Verdict.NO_DIRECT_MEDIA;
                case MISMATCH:return Verdict.MEDIA_MISMATCH;
                case AMBIGUOUS:return Verdict.MEDIA_AMBIGUOUS;
                case VIEW_COUNT_LIMIT:return Verdict.VIEW_SCAN_LIMIT;
                case REFLECTION_FAILED:return Verdict.REFLECTION_FAILED;
                default:return Verdict.DIRECT_FIELD_LIMIT;
            }
        } catch(Throwable failed) { return Verdict.PAGE_STRUCTURE_FAILED; }
    }
}
