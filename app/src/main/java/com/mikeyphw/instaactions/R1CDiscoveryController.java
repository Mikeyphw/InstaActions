package com.mikeyphw.instaactions;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

/** One low-priority read-only inventory per APK identity; never runs on the UI thread. */
final class R1CDiscoveryController {
    private static final Set<String> STARTED = new HashSet<>();
    private static boolean unsupportedLogged;
    private R1CDiscoveryController() { }

    static void onActivityResumed(Activity activity, String apkPath,
            String moduleNativeLibDir, ClassLoader instagramLoader) {
        if (activity == null || !ModuleConstants.TARGET_PACKAGE.equals(activity.getPackageName())) return;
        if (instagramLoader == null) {
            Log.i("INA_R1C", "INA_R1C_SKIP reason=missing_classloader actions=off");
            return;
        }
        final String version;
        try {
            PackageInfo info = activity.getPackageManager().getPackageInfo(
                    ModuleConstants.TARGET_PACKAGE, 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException missing) {
            Log.i("INA_R1C", "INA_R1C_SKIP reason=package_missing actions=off");
            return;
        }
        if (!R1CDiscoveryPolicy.mayInventory(version)) {
            synchronized (STARTED) {
                if (!unsupportedLogged) {
                    unsupportedLogged = true;
                    Log.i("INA_R1C", "INA_R1C_SKIP reason=unsupported_version actions=off");
                }
            }
            return;
        }
        File apk = apkPath == null ? null : new File(apkPath);
        if (apk == null || !apk.isFile() || apk.length() <= 0L) {
            Log.i("INA_R1C", "INA_R1C_SKIP reason=missing_apk actions=off");
            return;
        }
        String key = version + ":" + apk.length() + ":" + apk.lastModified();
        synchronized (STARTED) {
            if (!STARTED.add(key)) return;
        }
        Log.i("INA_R1C", "INA_R1C_SCHEDULED version=" + version
                + " mode=read_only_candidate_inventory media_id=unavailable actions=off");
        Thread worker = new Thread(() -> R1CMediaCandidateProbe.scan(
                apkPath, moduleNativeLibDir, instagramLoader, version),
                "ina-r1c-candidate-inventory");
        worker.setDaemon(true);
        worker.setPriority(Thread.MIN_PRIORITY);
        worker.start();
    }
}
