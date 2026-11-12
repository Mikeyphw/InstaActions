package com.mikeyphw.instaactions;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Independent, read-only *candidate* visible-page corroboration of the IG455
 * Reels menu Media argument. Pattern source: InstaEclipse ReelsControlsHook's
 * ClipsViewPagerImpl / ViewPager2 pager anchor and ReelDownloadHook's unique
 * live-carousel selection; upstream e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d.
 *
 * No new Instagram hooks, UI writes, data getters, Media identifiers, network,
 * strong retention, or hook-exception propagation. Any doubt fails closed.
 */
final class R1CD06VisiblePageBinding {
    static final int MAX_DECOR_VIEWS = 512;
    static final int MAX_PAGE_VIEWS = R1CD06DirectViewMedia.MAX_VIEWS;
    static final long MAX_RUNTIME_MS = 35L;
    private static final String PAGER = "androidx.viewpager2.widget.ViewPager2";
    private static final String RECYCLER = "androidx.recyclerview.widget.RecyclerView";

    enum Verdict {
        CAPTURE_INACTIVE, SCREEN_UNSTABLE, MENU_UNQUALIFIED, WRONG_MEDIA_TYPE, NO_ACTIVE_ACTIVITY,
        OFF_MAIN_THREAD, NO_VERTICAL_PAGER, AMBIGUOUS_PAGERS,
        NO_RECYCLER, AMBIGUOUS_RECYCLER, NO_CURRENT_PAGE,
        AMBIGUOUS_CURRENT_PAGE, PAGE_NOT_VISIBLE,
        NO_DIRECT_MEDIA, MEDIA_MISMATCH, MEDIA_AMBIGUOUS,
        REFLECTION_FAILED, BUDGET_EXCEEDED,
        DECOR_VIEW_LIMIT, DECOR_TIME_LIMIT, PAGE_VIEW_LIMIT, PAGE_TIME_LIMIT,
        DIRECT_VIEW_COUNT_LIMIT, DIRECT_CLASS_DEPTH_LIMIT,
        DIRECT_DECLARED_FIELD_LIMIT, DIRECT_MEDIA_FIELD_LIMIT,
        PAGER_METHOD_FAILED, RECYCLER_METHOD_FAILED,
        VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE
    }
    private R1CD06VisiblePageBinding() { }

    static boolean isPositiveOwner(R1CD06MenuOwnership.Verdict ownership) {
        return ownership == R1CD06MenuOwnership.Verdict.OWNER_DIRECT_MATCH
                || ownership == R1CD06MenuOwnership.Verdict.CONTEXT_DIRECT_MATCH
                || ownership == R1CD06MenuOwnership.Verdict.OWNER_AND_CONTEXT_MATCH;
    }

    private static boolean hasType(Class<?> clazz, String exact) {
        for (Class<?> type = clazz; type != null; type = type.getSuperclass()) {
            if (exact.equals(type.getName())) return true;
        }
        return false;
    }

    private static int number(Object owner, String method) throws Exception {
        Method m = owner.getClass().getMethod(method);
        if (m.getParameterCount() != 0 || m.getReturnType() != int.class)
            throw new IllegalStateException("unexpected_androidx_method_shape");
        return (Integer) m.invoke(owner);
    }

    private static boolean centered(Rect rect, int x, int y) {
        return rect.contains(x, y);
    }

    private static final class Entry {
        final View view;
        Entry(View view) { this.view = view; }
    }

    static Verdict observe(Activity activity, Object media, Class<?> exactMedia,
            R1BNavigationPolicy.Screen screen, R1CD06MenuOwnership.Verdict ownership) {
        if (screen != R1BNavigationPolicy.Screen.REELS) return Verdict.SCREEN_UNSTABLE;
        if (!isPositiveOwner(ownership)) return Verdict.MENU_UNQUALIFIED;
        if (media == null || exactMedia == null || !exactMedia.isInstance(media))
            return Verdict.WRONG_MEDIA_TYPE;
        if (activity == null || activity.isFinishing() || activity.isDestroyed()
                || !ModuleConstants.TARGET_PACKAGE.equals(activity.getPackageName())
                || !R1BNavigationPolicy.isMainActivity(activity.getClass().getName()))
            return Verdict.NO_ACTIVE_ACTIVITY;
        if (Looper.myLooper() != Looper.getMainLooper()) return Verdict.OFF_MAIN_THREAD;
        final long start = SystemClock.uptimeMillis();
        try {
            View decor = activity.getWindow() == null ? null : activity.getWindow().getDecorView();
            if (!(decor instanceof ViewGroup)) return Verdict.NO_ACTIVE_ACTIVITY;
            Rect bounds = new Rect();
            if (!decor.getGlobalVisibleRect(bounds) || bounds.width() <= 0 || bounds.height() <= 0)
                return Verdict.NO_ACTIVE_ACTIVITY;
            int centerX = bounds.centerX();
            int centerY = bounds.centerY();
            View pager = null;
            ArrayDeque<Entry> queue = new ArrayDeque<>();
            queue.addLast(new Entry(decor));
            int visited = 0;
            while (!queue.isEmpty()) {
                if (++visited > MAX_DECOR_VIEWS) return Verdict.DECOR_VIEW_LIMIT;
                if (SystemClock.uptimeMillis()-start > MAX_RUNTIME_MS)
                    return Verdict.DECOR_TIME_LIMIT;
                View view = queue.removeFirst().view;
                if (!view.isShown()) continue;
                Rect rect = new Rect();
                if (!view.getGlobalVisibleRect(rect)) continue;
                if (hasType(view.getClass(), PAGER)
                        && centered(rect, centerX, centerY)
                        && rect.width() * 10 >= bounds.width() * 7
                        && rect.height() * 10 >= bounds.height() * 6) {
                    final int direction;
                    try { direction = number(view,"getOrientation"); }
                    catch (Throwable methodFailure) { return Verdict.PAGER_METHOD_FAILED; }
                    if (direction != 1) continue; // 1 is vertical ViewPager2
                    if (pager != null) return Verdict.AMBIGUOUS_PAGERS;
                    pager = view;
                }
                if (view instanceof ViewGroup) {
                    ViewGroup group = (ViewGroup) view;
                    for (int i=0; i<group.getChildCount(); i++) {
                        View child = group.getChildAt(i);
                        if (child != null) queue.addLast(new Entry(child));
                    }
                }
            }
            if (!(pager instanceof ViewGroup)) return Verdict.NO_VERTICAL_PAGER;
            ViewGroup pagerGroup = (ViewGroup)pager;
            ViewGroup recycler = null;
            for (int i=0; i<pagerGroup.getChildCount(); i++) {
                View v = pagerGroup.getChildAt(i);
                if (v instanceof ViewGroup && hasType(v.getClass(), RECYCLER)) {
                    if (recycler != null) return Verdict.AMBIGUOUS_RECYCLER;
                    recycler = (ViewGroup)v;
                }
            }
            if (recycler == null) return Verdict.NO_RECYCLER;
            final int currentIndex;
            try { currentIndex = number(pager,"getCurrentItem"); }
            catch (Throwable methodFailure) { return Verdict.PAGER_METHOD_FAILED; }
            if (currentIndex < 0) return Verdict.NO_CURRENT_PAGE;
            View page = null;
            // Unique centered item AND RecyclerView adapter position must equal
            // ViewPager2's independent currentItem, excluding preloaded neighbors.
            final Method adapterPosition;
            try { adapterPosition = recycler.getClass().getMethod("getChildAdapterPosition", View.class); }
            catch (Throwable methodFailure) { return Verdict.RECYCLER_METHOD_FAILED; }
            for (int i=0;i<recycler.getChildCount();i++) {
                View candidate = recycler.getChildAt(i);
                if (candidate == null || !candidate.isShown()) continue;
                Rect r = new Rect();
                if (!candidate.getGlobalVisibleRect(r) || !centered(r,centerX,centerY)) continue;
                final int index;
                try { index = (Integer)adapterPosition.invoke(recycler,candidate); }
                catch (Throwable methodFailure) { return Verdict.RECYCLER_METHOD_FAILED; }
                if (index != currentIndex) return Verdict.AMBIGUOUS_CURRENT_PAGE;
                if (page != null) return Verdict.AMBIGUOUS_CURRENT_PAGE;
                page = candidate;
            }
            if (page == null) return Verdict.NO_CURRENT_PAGE;
            ArrayDeque<View> visible = new ArrayDeque<>();
            List<Object> holders = new ArrayList<>();
            visible.addLast(page);
            while (!visible.isEmpty()) {
                if (holders.size() >= MAX_PAGE_VIEWS) return Verdict.PAGE_VIEW_LIMIT;
                if (SystemClock.uptimeMillis()-start > MAX_RUNTIME_MS)
                    return Verdict.PAGE_TIME_LIMIT;
                View v = visible.removeFirst();
                if (!v.isShown()) continue;
                Rect r = new Rect();
                if (!v.getGlobalVisibleRect(r)) continue;
                holders.add(v);
                if (v instanceof ViewGroup) {
                    ViewGroup g = (ViewGroup)v;
                    for(int i=0;i<g.getChildCount();i++){
                        View child=g.getChildAt(i);
                        if(child!=null) visible.addLast(child);
                    }
                }
            }
            if (holders.isEmpty()) return Verdict.PAGE_NOT_VISIBLE;
            R1CD06DirectViewMedia.Result match = R1CD06DirectViewMedia.compare(
                    holders.toArray(new Object[0]), exactMedia, media);
            if (SystemClock.uptimeMillis()-start > MAX_RUNTIME_MS) return Verdict.PAGE_TIME_LIMIT;
            switch (match) {
                case MATCH: return Verdict.VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE;
                case ABSENT: return Verdict.NO_DIRECT_MEDIA;
                case MISMATCH: return Verdict.MEDIA_MISMATCH;
                case AMBIGUOUS: return Verdict.MEDIA_AMBIGUOUS;
                case VIEW_COUNT_LIMIT: return Verdict.DIRECT_VIEW_COUNT_LIMIT;
                case CLASS_DEPTH_LIMIT: return Verdict.DIRECT_CLASS_DEPTH_LIMIT;
                case DECLARED_FIELD_LIMIT: return Verdict.DIRECT_DECLARED_FIELD_LIMIT;
                case MEDIA_FIELD_LIMIT: return Verdict.DIRECT_MEDIA_FIELD_LIMIT;
                case BUDGET_EXCEEDED: return Verdict.BUDGET_EXCEEDED;
                default: return Verdict.REFLECTION_FAILED;
            }
        } catch (Throwable ignored) {
            return Verdict.REFLECTION_FAILED;
        }
    }
}
