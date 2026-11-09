package com.mikeyphw.instaactions;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

/** Once-per-process, version-gated probe dispatch; work never runs on the UI thread. */
final class R1ProbeController {
    private static final Set<String> STARTED = new HashSet<>();
    private static boolean unknownLogged;
    private R1ProbeController() { }

    static void onActivityResumed(Activity activity, String targetApk, String moduleLibDir) {
        if (activity == null || !ModuleConstants.TARGET_PACKAGE.equals(activity.getPackageName())) return;
        final String version;
        try {
            PackageInfo info = activity.getPackageManager().getPackageInfo(
                    ModuleConstants.TARGET_PACKAGE, 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException missing) {
            Log.w("INA_R1", "INA_R1_SCAN_SKIP reason=package_metadata_missing");
            return;
        }
        if (!R1ProbePolicy.supportedVersion(version)) {
            synchronized (STARTED) {
                if (!unknownLogged) {
                    unknownLogged = true;
                    Log.i("INA_R1", "INA_R1_SCAN_SKIP reason=unqualified_version actions=off");
                }
            }
            return;
        }
        File apk = targetApk == null ? null : new File(targetApk);
        String key = apk == null ? null : R1ProbePolicy.cacheKey(
                version, apk.length(), apk.lastModified());
        if (key == null) {
            Log.w("INA_R1", "INA_R1_SCAN_SKIP reason=invalid_apk_identity");
            return;
        }
        synchronized (STARTED) {
            if (!STARTED.add(key)) return;
        }
        Log.i("INA_R1", "INA_R1_SCAN_SCHEDULED version=" + version + " actions=off");
        final String versionSnapshot = version;
        Thread worker = new Thread(() -> R1DexKitProbe.scan(targetApk, moduleLibDir, versionSnapshot),
                "ina-r1-dexkit-probe");
        worker.setDaemon(true);
        worker.setPriority(Thread.MIN_PRIORITY);
        worker.start();
    }
}
