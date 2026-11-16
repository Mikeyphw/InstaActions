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
import android.widget.Checkable;
import java.util.ArrayDeque;
import java.util.HashMap;
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
    private static final Set<String> REPORTED_VERSIONS = new HashSet<>();
    private R1BContextMonitor() { }

    static void resumed(Activity activity) {
        if (activity == null || !ModuleConstants.TARGET_PACKAGE.equals(activity.getPackageName())) return;
        try {
            PackageInfo info = activity.getPackageManager().getPackageInfo(
                    ModuleConstants.TARGET_PACKAGE, 0);
            // R1A DexKit remains version-locked. R1B is only a bounded Android
            // View-tree observer, so it may collect diagnostic hints on newer
            // Instagram builds without granting any action capability.
            boolean versionQualified = R1ProbePolicy.supportedVersion(info.versionName);
            String version = info.versionName == null ? "unknown" : info.versionName;
            synchronized (SESSIONS) {
                if (REPORTED_VERSIONS.add(version)) {
                    Log.i("INA_R1B", "INA_R1B_VERSION version=" + safeVersion(version)
                            + " qualified=" + versionQualified
                            + " mode=read_only_preview dexkit_gate=unchanged actions=off");
                }
                if (SESSIONS.containsKey(activity)) return;
                Window window = activity.getWindow();
                View decor = window == null ? null : window.getDecorView();
                if (decor == null) return;
                Session session = new Session(decor, versionQualified,
                        R1BNavigationPolicy.isMainActivity(activity.getClass().getName()));
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

    private static String safeVersion(String value) {
        // Only a bounded, printable package version is emitted; no account/media data.
        String normalized = value.replaceAll("[^A-Za-z0-9._-]", "_");
        return normalized.length() > 64 ? normalized.substring(0, 64) : normalized;
    }

    /** Read only the current lifecycle-local stable screen. Multiple sessions fail closed.
     * No Activity or user/media object is kept by the caller.
     */
    static R1BNavigationPolicy.Screen stableScreen() {
        synchronized (SESSIONS) {
            R1BNavigationPolicy.Screen current = R1BNavigationPolicy.Screen.UNKNOWN;
            int activeCount = 0;
            for (Session session : SESSIONS.values()) {
                if (!session.active) continue;
                if (++activeCount > 1) return R1BNavigationPolicy.Screen.UNKNOWN;
                current = session.lastNavigation;
            }
            return activeCount == 1 && current != null
                    ? current : R1BNavigationPolicy.Screen.UNKNOWN;
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
        private final boolean versionQualified;
        private final boolean mainActivity;
        private final Handler ui = new Handler(Looper.getMainLooper());
        private final R1BNavigationStabilizer navigation = new R1BNavigationStabilizer();
        private volatile R1BNavigationPolicy.Screen lastNavigation;
        private int lastNavEvidenceBits = -1;
        private int navEvidenceEmitted;
        private ViewTreeObserver observer;
        private volatile boolean active;
        private boolean pending;
        private long lastSampleMs;
        private R1BContextPolicy.Decision lastDecision;
        private int hintsEmitted;
        private long lastHintMs;
        private final Runnable hintTimer = () -> {
            if (active && lastDecision != null
                    && lastDecision.surface == R1BContextPolicy.Surface.UNKNOWN
                    && hintsEmitted < R1BResourceDiagnostics.MAX_LOGS_PER_SESSION) {
                schedule();
            }
        };
        private final Runnable sampler = this::sample;
        // Tab selection can change without relayout or scrolling. This bounded
        // interval is a fallback; active navigation still uses layout events.
        private final Runnable navigationTimer = new Runnable() {
            @Override public void run() {
                if (!active) return;
                schedule();
                ui.postDelayed(this, 1600L);
            }
        };

        Session(View decor, boolean versionQualified, boolean mainActivity) {
            this.decor = decor;
            this.versionQualified = versionQualified;
            this.mainActivity = mainActivity;
        }

        boolean start() {
            observer = decor.getViewTreeObserver();
            if (!observer.isAlive()) return false;
            observer.addOnGlobalLayoutListener(this);
            observer.addOnScrollChangedListener(this);
            active = true;
            Log.i("INA_R1B", "INA_R1B_OBSERVER_ATTACHED scope=instagram_main"
                    + " qualified=" + versionQualified + " mode=read_only_preview actions=off");
            schedule();
            ui.postDelayed(navigationTimer, 1600L);
            return true;
        }

        void stop() {
            active = false;
            pending = false;
            ui.removeCallbacks(sampler);
            ui.removeCallbacks(hintTimer);
            ui.removeCallbacks(navigationTimer);
            navigation.reset();
            lastNavigation = R1BNavigationPolicy.Screen.UNKNOWN;
            if (observer != null && observer.isAlive()) {
                observer.removeOnGlobalLayoutListener(this);
                observer.removeOnScrollChangedListener(this);
            }
            observer = null;
            Log.i("INA_R1B", "INA_R1B_OBSERVER_DETACHED actions=off");
        }

        // Only Android View selection state is read. Never inspect labels,
        // captions, accessibility text, listeners, model objects or touch data.
        private static boolean isExplicitlySelected(View view) {
            return view.isSelected() || view.isActivated()
                    || (view instanceof Checkable && ((Checkable) view).isChecked());
        }

        private static boolean isOtherNavigationTab(String name) {
            switch (name) {
                case "clips_tab": case "direct_tab": case "search_tab":
                case "creation_tab": case "news_tab": case "profile_tab":
                    return true;
                default: return false;
            }
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
                Map<String, Integer> visibleClasses = new HashMap<>();
                int visibleNodes = 0;
                int nodesWithId = 0;
                int unresolvedIds = 0;
                boolean feedTabVisible = false;
                boolean feedTabSelected = false;
                boolean otherTabSelected = false;
                boolean mainFeedBarVisible = false;
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
                    visibleNodes++;
                    String className = view.getClass().getName();
                    if (visibleClasses.containsKey(className)
                            || visibleClasses.size() < R1BResourceDiagnostics.MAX_DISTINCT_CLASSES) {
                        visibleClasses.put(className, visibleClasses.getOrDefault(className, 0) + 1);
                    }
                    int id = view.getId();
                    if (id != View.NO_ID) {
                        nodesWithId++;
                        try {
                            Resources resources = view.getResources();
                            if (ModuleConstants.TARGET_PACKAGE.equals(
                                    resources.getResourcePackageName(id))) {
                                String resourceName = resources.getResourceEntryName(id);
                                names.add(resourceName);
                                if ("main_feed_action_bar".equals(resourceName)) {
                                    mainFeedBarVisible = true;
                                }
                                if ("feed_tab".equals(resourceName)) {
                                    feedTabVisible = true;
                                    if (isExplicitlySelected(view)) feedTabSelected = true;
                                } else if (isOtherNavigationTab(resourceName)
                                        && isExplicitlySelected(view)) {
                                    otherTabSelected = true;
                                }
                            }
                        } catch (Resources.NotFoundException ignored) {
                            // Obfuscated or transient resource IDs are not evidence.
                            unresolvedIds++;
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
                // Diagnostic samples for UNKNOWN screens are independent of context
                // transitions; cap to three per Activity, >=8 s apart. No view contents.
                long now = SystemClock.uptimeMillis();
                if (result.surface == R1BContextPolicy.Surface.UNKNOWN
                        && hintsEmitted < R1BResourceDiagnostics.MAX_LOGS_PER_SESSION
                        && (hintsEmitted == 0
                            || now - lastHintMs >= R1BResourceDiagnostics.HINT_INTERVAL_MS)) {
                    hintsEmitted++;
                    lastHintMs = now;
                    Log.i("INA_R1B", "INA_R1B_RESOURCE_HINTS "
                            + R1BResourceDiagnostics.summarize(names, visibleClasses,
                                    visited, visibleNodes, nodesWithId, unresolvedIds,
                                    !queue.isEmpty()));
                }
                ui.removeCallbacks(hintTimer);
                if (result.surface == R1BContextPolicy.Surface.UNKNOWN
                        && hintsEmitted < R1BResourceDiagnostics.MAX_LOGS_PER_SESSION) {
                    ui.postDelayed(hintTimer, R1BResourceDiagnostics.HINT_INTERVAL_MS);
                }
                if (!result.sameState(lastDecision)) {
                    lastDecision = result;
                    Log.i("INA_R1B", "INA_R1B_CONTEXT surface=" + result.surface
                            + " confidence=" + result.confidence
                            + " scores=" + result.reelsScore + "/" + result.storyScore
                            + "/" + result.postScore + " truncated=" + result.truncated
                            + " qualified=" + versionQualified
                            + " media_id=unavailable actions=off");
                }
                // Distinct evidence channels: VIEW/MEDIA reports only a definite
                // viewer or a Feed-row anchor; NAVIGATION reports a preview of
                // the active screen, not any media's identity or action grant.
                boolean truncated = !queue.isEmpty();
                R1BNavigationPolicy.Screen candidate = R1BNavigationPolicy.infer(
                        mainActivity, feedTabVisible, feedTabSelected,
                        otherTabSelected, mainFeedBarVisible, truncated, result);
                R1BNavigationPolicy.Screen confirmed = navigation.accept(candidate, now);
                int bits = (mainActivity ? 1 : 0) | (feedTabVisible ? 2 : 0)
                        | (feedTabSelected ? 4 : 0) | (otherTabSelected ? 8 : 0)
                        | (mainFeedBarVisible ? 16 : 0) | (truncated ? 32 : 0);
                if (bits != lastNavEvidenceBits && navEvidenceEmitted < 6) {
                    navEvidenceEmitted++;
                    lastNavEvidenceBits = bits;
                    Log.i("INA_R1B", "INA_R1B_NAV_EVIDENCE main=" + mainActivity
                            + " feed_visible=" + feedTabVisible
                            + " feed_selected=" + feedTabSelected
                            + " other_selected=" + otherTabSelected
                            + " feed_bar=" + mainFeedBarVisible
                            + " truncated=" + truncated + " actions=off");
                }
                if (confirmed != lastNavigation) {
                    lastNavigation = confirmed;
                    Log.i("INA_R1B", "INA_R1B_NAVIGATION surface=" + confirmed
                            + " evidence=" + candidate
                            + " media_surface=" + result.surface
                            + " confidence=" + (confirmed == R1BNavigationPolicy.Screen.UNKNOWN
                                    ? "NONE" : "STABLE_PREVIEW")
                            + " media_id=unavailable actions=off");
                }
            } catch (Throwable error) {
                Log.w("INA_R1B", "INA_R1B_SAMPLE_FAILED type="
                        + error.getClass().getSimpleName());
            }
        }
    }
}
