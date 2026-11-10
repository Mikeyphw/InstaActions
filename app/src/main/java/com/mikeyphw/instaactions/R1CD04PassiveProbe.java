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

    /** Invalidate ephemeral comparison state whenever a resumed Activity leaves. */
    static void paused() {
        Counter counter = installedCounter;
        if (counter != null) counter.invalidate();
    }

    interface Installer { void install(Method method, Counter counter) throws Throwable; }
    private R1CD04PassiveProbe() { }

    static void onActivityResumed(Activity activity, String targetApk,
            ClassLoader instagramLoader, Installer installer) {
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
        if (!STARTED.compareAndSet(false, true)) return;
        Thread worker = new Thread(() -> installVerified(targetApk, instagramLoader, installer),
                "ina-r1c-d04-passive-install");
        worker.setPriority(Thread.MIN_PRIORITY);
        worker.setDaemon(true);
        worker.start();
    }

    private static void installVerified(String apkPath, ClassLoader loader, Installer installer) {
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
            Counter counter = new Counter(media);
            installer.install(target, counter);
            installedCounter = counter;
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
        private int events;
        private final R1CD05ReferenceContinuity continuity =
                new R1CD05ReferenceContinuity();
        Counter(Class<?> media) { this.media = media; }

        /** Only type/nullness and already-confirmed navigation; never access object fields. */
        synchronized void observe(Object mediaArg, R1BNavigationPolicy.Screen screen) {
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
            if (events == R1CD04Policy.MAX_RECORDED_EVENTS) {
                Log.i(TAG, "INA_R1C_D04_LIMIT reached=" + events + " actions=off");
            }
        }

        synchronized void invalidate() { continuity.invalidate(); }
    }
}
