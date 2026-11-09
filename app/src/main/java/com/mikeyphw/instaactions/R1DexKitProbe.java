package com.mikeyphw.instaactions;

import android.util.Log;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.ClassDataList;
import org.luckypray.dexkit.result.MethodDataList;

/** Bounded read-only DexKit query sweep. Never installs hooks on Instagram internals. */
final class R1DexKitProbe {
    private static final AtomicBoolean LIB_LOADED = new AtomicBoolean(false);
    private R1DexKitProbe() { }

    static void scan(String apkPath, String nativeLibraryDir, String version) {
        if (!R1ProbePolicy.supportedVersion(version)) return;
        if (apkPath == null || apkPath.isEmpty()) {
            Log.w("INA_R1", "INA_R1_SCAN_SKIP reason=no_apk_path");
            return;
        }
        File library = nativeLibraryDir == null ? null : new File(nativeLibraryDir, "libdexkit.so");
        if (library == null || !library.isFile()) {
            Log.w("INA_R1", "INA_R1_SCAN_SKIP reason=native_library_missing");
            return;
        }
        try {
            if (LIB_LOADED.compareAndSet(false, true)) {
                try { System.load(library.getAbsolutePath()); }
                catch (Throwable failure) {
                    LIB_LOADED.set(false);
                    throw failure;
                }
            }
            try (DexKitBridge bridge = DexKitBridge.create(apkPath)) {
                int found = 0, ambiguous = 0, missing = 0, failed = 0;
                for (R1ProbePolicy.Marker marker : R1ProbePolicy.MARKERS) {
                    try {
                        int matches;
                        String uniqueDescriptor = "";
                        if (marker.kind == R1ProbePolicy.Kind.METHOD) {
                            MethodDataList methods = bridge.findMethod(FindMethod.create()
                                    .matcher(MethodMatcher.create().usingStrings(marker.needle)));
                            matches = methods.size();
                            if (matches == 1) uniqueDescriptor = methods.get(0).getDescriptor();
                        } else {
                            ClassDataList classes = bridge.findClass(FindClass.create()
                                    .matcher(ClassMatcher.create().usingStrings(marker.needle)));
                            matches = classes.size();
                            if (matches == 1) uniqueDescriptor = classes.get(0).getName();
                        }
                        R1ProbePolicy.Result result = R1ProbePolicy.classify(matches);
                        if (result == R1ProbePolicy.Result.UNIQUE) found++;
                        else if (result == R1ProbePolicy.Result.MISSING) missing++;
                        else if (result == R1ProbePolicy.Result.AMBIGUOUS) ambiguous++;
                        // Unique descriptors contain method/class symbols only, never media, users or URLs.
                        Log.i("INA_R1", "INA_R1_MARKER id=" + marker.id + " state=" + result
                                + " count=" + matches + (matches == 1 ? " symbol=" + uniqueDescriptor : ""));
                    } catch (Throwable error) {
                        failed++;
                        Log.w("INA_R1", "INA_R1_MARKER id=" + marker.id + " state=FAILED"
                                + " error=" + error.getClass().getSimpleName());
                    }
                }
                Log.i("INA_R1", "INA_R1_SUMMARY version=" + version + " unique=" + found
                        + " ambiguous=" + ambiguous + " missing=" + missing + " failed=" + failed
                        + " actions=off");
            }
        } catch (Throwable failure) {
            Log.w("INA_R1", "INA_R1_SCAN_FAILED type=" + failure.getClass().getSimpleName());
        }
    }
}
