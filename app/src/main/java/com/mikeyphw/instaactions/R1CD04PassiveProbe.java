package com.mikeyphw.instaactions;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * D04 exact-APK read-only runtime correlation. Does NOT read Media values,
 * identify a user/post, or enable action hooks. D05 holds only a weak
 * in-memory comparison reference, never a Media value, ID, or account field.
 */
final class R1CD04PassiveProbe {
    private static final String TAG = "INA_R1C";
    private static final String MEDIA = "com.instagram.feed.media.Media";
    private static final String OWNER = "X.07EF";
    private static final String CONTEXT = "X.03jS";
    private static final String METHOD = "A05";
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);
    private static volatile Counter installedCounter;

    /** The FAB may capture only after the exact APK SHA and method are verified. */
    static boolean isInstalled() { return installedCounter != null; }
    static boolean hasCaptureCapacity() {
        Counter counter = installedCounter;
        return counter != null && counter.hasFourEventsRemaining();
    }

    /** Atomically reset D05 weak-reference baseline and start the FAB capture.
     * Counter lock serializes with the passive interceptor, preventing an old
     * pre-capture Media callback from being counted as A1.
     */
    static boolean beginFabCapture(R1CD05FabSession session, long nowMs) {
        Counter counter = installedCounter;
        if (session == null || counter == null) return false;
        synchronized (counter) {
            if (!counter.hasFourEventsRemaining()) return false;
            if (!session.start(true, nowMs)) return false;
            counter.invalidate();
            return true;
        }
    }

    /** Invalidate ephemeral comparison state whenever a resumed Activity leaves. */
    static void paused() {
        Counter counter = installedCounter;
        if (counter != null) { counter.invalidate(); counter.clearActivity(); }
    }

    interface Installer { void install(Method method, Counter counter) throws Throwable; }
    private R1CD04PassiveProbe() { }

    static void onActivityResumed(Activity activity, String targetApk,
            String nativeLibraryDir, ClassLoader instagramLoader, Installer installer) {
        if (activity == null || !ModuleConstants.TARGET_PACKAGE.equals(activity.getPackageName())) return;
        final String version;
        try {
            PackageInfo info = activity.getPackageManager().getPackageInfo(
                    ModuleConstants.TARGET_PACKAGE, 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(TAG, "INA_R1C_D04_GATE status=REFUSED reason=no_package actions=off");
            return;
        }
        if (!R1CD04Policy.TARGET_VERSION.equals(version)) {
            Log.i(TAG, "INA_R1C_D04_GATE status=REFUSED reason=version_mismatch actions=off");
            return;
        }
        if (targetApk == null || instagramLoader == null || installer == null) {
            Log.i(TAG, "INA_R1C_D04_GATE status=REFUSED reason=missing_input actions=off");
            return;
        }
        if (!STARTED.compareAndSet(false, true)) {
            Counter existing = installedCounter;
            if (existing != null) existing.setActivity(activity);
            return;
        }
        Thread worker = new Thread(() -> installVerified(targetApk, nativeLibraryDir,
                        instagramLoader, installer, activity),
                "ina-r1c-d04-passive-install");
        worker.setPriority(Thread.MIN_PRIORITY);
        worker.setDaemon(true);
        worker.start();
    }

    private static void installVerified(String apkPath, String nativeLibraryDir,
            ClassLoader loader, Installer installer, Activity resumedActivity) {
        try {
            File apk = new File(apkPath);
            if (!apk.isFile() || apk.length() != R1CD04Policy.TARGET_APK_BYTES) {
                Log.i(TAG, "INA_R1C_D04_GATE status=REFUSED reason=apk_size_mismatch actions=off");
                return;
            }
            Log.i(TAG, "INA_R1C_D04_GATE status=HASHING mode=background actions=off");
            String sha = sha256(apk);
            if (!R1CD04Policy.validBuild(R1CD04Policy.TARGET_VERSION, sha)) {
                Log.i(TAG, "INA_R1C_D04_GATE status=REFUSED reason=apk_sha256_mismatch actions=off");
                return;
            }
            Log.i(TAG, "INA_R1C_D04_GATE status=PASS version=455 apk_sha256=match actions=off");
            // The target class is loaded without invoking its static initializer.
            Class<?> media = Class.forName(MEDIA, false, loader);
            Class<?> owner = Class.forName(OWNER, false, loader);
            Method[] declared = owner.getDeclaredMethods();
            if (declared.length > R1CD04Policy.MAX_DECLARED_METHODS) {
                Log.i(TAG, "INA_R1C_D04_HOOK status=REFUSED reason=method_limit actions=off");
                return;
            }
            Method target = null;
            int matches = 0;
            for (Method m : declared) {
                if (!METHOD.equals(m.getName()) || Modifier.isStatic(m.getModifiers())
                        || m.getReturnType() != ArrayList.class) continue;
                Class<?>[] args = m.getParameterTypes();
                if (args.length != 2 || !CONTEXT.equals(args[0].getName())
                        || args[1] != media) continue;
                target = m;
                matches++;
            }
            if (matches != 1) {
                Log.i(TAG, "INA_R1C_D04_HOOK status=REFUSED reason=signature_count"
                        + " count=" + matches + " actions=off");
                return;
            }
            // APK DEX access_flags=0x12: PRIVATE FINAL *INSTANCE* method, not static.
            // No setAccessible, invoke, getId, media getter or method result reads.
            Counter counter = new Counter(media, target.getDeclaringClass(), target.getParameterTypes()[0]);
            counter.setActivity(resumedActivity);
            installer.install(target, counter);
            installedCounter = counter;
            // Correlate two exact InstaEclipse source fingerprints with this
            // SHA-qualified IG455 APK. Read-only DexKit, independent background thread.
            R1CD06UpstreamConformance.startAfterExactGate(apkPath, nativeLibraryDir, loader);
            Log.i(TAG, "INA_R1C_D04_HOOK status=INSTALLED"
                    + " signature=exact_ig455 mode=passive_no_mutation actions=off");
        } catch (Throwable failure) {
            Log.w(TAG, "INA_R1C_D04_HOOK status=FAILED type="
                    + failure.getClass().getSimpleName() + " actions=off");
        }
    }

    private static String sha256(File apk) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[64 * 1024];
        long readTotal = 0;
        try (FileInputStream stream = new FileInputStream(apk)) {
            for (int n; (n = stream.read(buffer)) != -1;) {
                readTotal += n;
                if (readTotal > R1CD04Policy.MAX_HASH_BYTES) {
                    throw new IllegalStateException("apk_too_large");
                }
                digest.update(buffer, 0, n);
            }
        }
        if (readTotal != apk.length()) throw new IllegalStateException("apk_changed_during_hash");
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder(64);
        for (byte b : bytes) sb.append(String.format(java.util.Locale.ROOT, "%02x", b & 0xff));
        return sb.toString();
    }

    static final class Counter {
        private final Class<?> media;
        private final Class<?> menuOwnerType;
        private final Class<?> contextType;
        // Weak Activity reference; the UI tree is read only during main-thread
        // callbacks and never persisted across Activity pause/destroy.
        private WeakReference<Activity> activeActivity = new WeakReference<>(null);
        private int events;
        private final R1CD05ReferenceContinuity continuity =
                new R1CD05ReferenceContinuity();
        Counter(Class<?> media, Class<?> ownerType, Class<?> contextType) {
            this.media = media;
            this.menuOwnerType = ownerType;
            this.contextType = contextType;
        }

        synchronized void setActivity(Activity activity) {
            activeActivity = new WeakReference<>(activity);
        }
        synchronized void clearActivity() { activeActivity.clear(); }

        /** Only type/nullness and already-confirmed navigation; never access object fields. */
        synchronized void observe(Object mediaArg, R1BNavigationPolicy.Screen screen,
                Object menuOwner, Object menuContext) {
            if (events >= R1CD04Policy.MAX_RECORDED_EVENTS) return;
            events++;
            final String shape = mediaArg == null ? "NULL"
                    : media.isInstance(mediaArg) ? "MEDIA_TYPED" : "WRONG_TYPE";
            final R1BNavigationPolicy.Screen safeScreen = screen == null
                    ? R1BNavigationPolicy.Screen.UNKNOWN : screen;
            R1CD05ReferenceContinuity.Result continuityResult =
                    continuity.accept(mediaArg, safeScreen, "MEDIA_TYPED".equals(shape));
            Log.i(TAG, "INA_R1C_D04_EVENT ordinal=" + events
                    + " screen=" + safeScreen + " arg=" + shape
                    + " media_id=unavailable retained=weak_only actions=off");
            Log.i(TAG, "INA_R1C_D05_REFERENCE ordinal=" + events
                    + " screen=" + safeScreen + " comparison=" + continuityResult
                    + " media_id=unavailable object_id=excluded retained=weak_only actions=off");
            // Ported upstream ownership invariant: only exact direct typed fields,
            // UNIQUE by reference, can corroborate the selected menu argument.
            // This is not independently verified visible-Reel identity.
            R1CD06MenuOwnership.Verdict ownership = R1CD06MenuOwnership.resolve(
                    menuOwner, menuContext, mediaArg, menuOwnerType, contextType,
                    media, safeScreen);
            Log.i(TAG, "INA_R1C_D06_OWNER ordinal=" + events
                    + " result=" + ownership + " visible_media=unverified"
                    + " media_id=excluded actions=off");
            // Independent AndroidX vertical ViewPager2 active-page corroboration.
            // No Media identifiers, model getters, strong references or UI writes.
            // ReelsControlsHook and ReelDownloadHook in InstaEclipse provide the
            // viewer pager / ambiguity-rejection source patterns.
            R1CD06VisiblePageBinding.Verdict visible = R1CD05FabPanel.hasActiveWindow()
                    ? R1CD06VisiblePageBinding.observe(
                        activeActivity.get(), mediaArg, media, safeScreen, ownership)
                    : R1CD06VisiblePageBinding.Verdict.CAPTURE_INACTIVE;
            Log.i(TAG, "INA_R1C_D06_VISIBLE_PAGE ordinal=" + events
                    + " result=" + visible + " verified=NO actions=off");
            // If the menu hides the pager from the fresh decor scan, reconcile
            // against a recently visible, stage-bound weak page snapshot.
            R1CD06PreMenuReconciliation.Verdict preMenu =
                    R1CD05FabPanel.hasActiveWindow()
                    ? R1CD06PreMenuReconciliation.compare(activeActivity.get(),
                        mediaArg,media,safeScreen,ownership,R1CD05FabPanel.activeStage())
                    : R1CD06PreMenuReconciliation.Verdict.NOT_QUALIFIED;
            Log.i(TAG, "INA_R1C_D06_PREMENU ordinal=" + events
                    + " callback_scan=" + visible + " snapshot=" + preMenu
                    + " media_id=excluded verified=NO actions=off");
            // All callback evidence crossing into the FAB is enum/boolean-only.
            R1CD05FabPanel.record(safeScreen, continuityResult,
                    "MEDIA_TYPED".equals(shape), ownership, visible, preMenu);
            if (events == R1CD04Policy.MAX_RECORDED_EVENTS) {
                Log.i(TAG, "INA_R1C_D04_LIMIT reached=" + events + " actions=off");
            }
        }

        synchronized boolean hasFourEventsRemaining() {
            return events <= R1CD04Policy.MAX_RECORDED_EVENTS - 4;
        }
        synchronized void invalidate() { continuity.invalidate(); }
    }
}
