package com.mikeyphw.instaactions;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Read-only observer of visible Instagram-owned resource IDs. Does not inspect
 * View text, click listeners, media models or Instagram internal classes.
 * One observer per resumed Activity, released on pause/destroy.
 */
final class R1BContextMonitor {
    private static final Map<Activity, Session> SESSIONS = new IdentityHashMap<>();
    private R1BContextMonitor() { }

    static void resumed(Activity activity) {
        if (activity == null || !ModuleConstants.TARGET_PACKAGE.equals(activity.getPackageName())) return;
        try {
            PackageInfo info = activity.getPackageManager().getPackageInfo(
                    ModuleConstants.TARGET_PACKAGE, 0);
            if (!R1ProbePolicy.supportedVersion(info.versionName)) return;
            synchronized (SESSIONS) {
                if (SESSIONS.containsKey(activity)) return;
                Window window = activity.getWindow();
                View decor = window == null ? null : window.getDecorView();
                if (decor == null) return;
                Session session = new Session(decor);
                if (session.start()) SESSIONS.put(activity, session);
            }
        } catch (PackageManager.NameNotFoundException ignored) {
            Log.i("INA_R1B", "INA_R1B_ATTACH_SKIP reason=package_missing");
        } catch (Throwable failure) {
            Log.w("INA_R1B", "INA_R1B_ATTACH_FAILED type="
                    + failure.getClass().getSimpleName());
            paused(activity);
        }
    }

    static void paused(Activity activity) {
        if (activity == null) return;
        Session session;
        synchronized (SESSIONS) { session = SESSIONS.remove(activity); }
        if (session != null) session.stop();
    }

    private static final class Node {
        final View view;
        final int depth;
        Node(View v, int d) { view = v; depth = d; }
    }

    private static final class Session implements ViewTreeObserver.OnGlobalLayoutListener,
            ViewTreeObserver.OnScrollChangedListener {
        private final View decor;
        private final Handler ui = new Handler(Looper.getMainLooper());
        private ViewTreeObserver observer;
        private boolean active;
        private boolean pending;
        private long lastSampleMs;
        private R1BContextPolicy.Decision lastDecision;
        private final Runnable sampler = this::sample;

        Session(View decor) { this.decor = decor; }

        boolean start() {
            observer = decor.getViewTreeObserver();
            if (!observer.isAlive()) return false;
            observer.addOnGlobalLayoutListener(this);
            observer.addOnScrollChangedListener(this);
            active = true;
            Log.i("INA_R1B", "INA_R1B_OBSERVER_ATTACHED scope=instagram_main actions=off");
            schedule();
            return true;
        }

        void stop() {
            active = false;
            pending = false;
            ui.removeCallbacks(sampler);
            if (observer != null && observer.isAlive()) {
                observer.removeOnGlobalLayoutListener(this);
                observer.removeOnScrollChangedListener(this);
            }
            observer = null;
            Log.i("INA_R1B", "INA_R1B_OBSERVER_DETACHED actions=off");
        }

        @Override public void onGlobalLayout() { schedule(); }
        @Override public void onScrollChanged() { schedule(); }

        private void schedule() {
            if (!active || pending) return;
            pending = true;
            long remaining = R1BContextPolicy.MIN_SAMPLE_INTERVAL_MS
                    - (SystemClock.uptimeMillis() - lastSampleMs);
            ui.postDelayed(sampler, Math.max(120L, remaining));
        }

        private void sample() {
            pending = false;
            if (!active) return;
            lastSampleMs = SystemClock.uptimeMillis();
            try {
                if (!decor.isShown()) return;
                Set<String> names = new HashSet<>();
                ArrayDeque<Node> queue = new ArrayDeque<>();
                queue.add(new Node(decor, 0));
                int visited = 0;
                Rect bounds = new Rect();
                while (!queue.isEmpty() && visited < R1BContextPolicy.MAX_VIEWS) {
                    Node node = queue.removeFirst();
                    View view = node.view;
                    visited++;
                    if (view.getVisibility() != View.VISIBLE || !view.isShown()
                            || !view.getGlobalVisibleRect(bounds)) continue;
                    int id = view.getId();
                    if (id != View.NO_ID) {
                        try {
                            Resources resources = view.getResources();
                            if (ModuleConstants.TARGET_PACKAGE.equals(
                                    resources.getResourcePackageName(id))) {
                                names.add(resources.getResourceEntryName(id));
                            }
                        } catch (Resources.NotFoundException ignored) {
                            // Obfuscated or transient resource IDs are not evidence.
                        }
                    }
                    if (view instanceof ViewGroup && node.depth < R1BContextPolicy.MAX_DEPTH) {
                        ViewGroup group = (ViewGroup) view;
                        for (int i = 0; i < group.getChildCount(); i++) {
                            View child = group.getChildAt(i);
                            if (child != null) queue.addLast(new Node(child, node.depth + 1));
                        }
                    }
                }
                R1BContextPolicy.Decision result = R1BContextPolicy.classify(
                        names, !queue.isEmpty());
                if (!result.sameState(lastDecision)) {
                    lastDecision = result;
                    Log.i("INA_R1B", "INA_R1B_CONTEXT surface=" + result.surface
                            + " confidence=" + result.confidence
                            + " scores=" + result.reelsScore + "/" + result.storyScore
                            + "/" + result.postScore + " truncated=" + result.truncated
                            + " media_id=unavailable actions=off");
                }
            } catch (Throwable error) {
                Log.w("INA_R1B", "INA_R1B_SAMPLE_FAILED type="
                        + error.getClass().getSimpleName());
            }
        }
    }
}
