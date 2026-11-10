package com.mikeyphw.instaactions;

import android.util.Log;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicBoolean;
import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodDataList;

/**
 * Source-backed, read-only Instagram 455 class/marker inventory.
 * From InstaEclipse: FeedVideoDownloadHook, ReelDownloadHook,
 * StoryDownloadHook and PostDownloadContextMenuHook. NO internal hooks are
 * installed, NO Media object is accessed and NO model getter is invoked.
 */
final class R1CMediaCandidateProbe {
    private static final AtomicBoolean NATIVE_LOADED = new AtomicBoolean(false);
    private static final String[] MODEL_CLASSES = {
        "com.instagram.feed.media.Media",
        "com.instagram.feed.media.MediaExtKt",
        "com.instagram.feed.media.MutableMediaDictIntf",
        "com.instagram.feed.media.LiveTreeMediaDict",
        "com.instagram.ui.widget.framelayout.MediaFrameLayout"
    };
    private static final String[][] MARKERS = {
        {"story_page_bind", "ReelViewerFragment.onCurrentActiveItemBound"},
        {"reels_options", "ClipsOrganicMediaItemViewMoreOptionsController"},
        {"feed_options", "MediaOptionsOverflowMenuCreator"}
    };

    private R1CMediaCandidateProbe() { }

    static void scan(String apkPath, String nativeLibraryDir, ClassLoader loader, String version) {
        if (!R1CDiscoveryPolicy.mayInventory(version)) return;
        int classesFound = 0;
        // Use Class.forName(..., false, ...) to prevent class initializers.
        for (int i = 0; i < MODEL_CLASSES.length
                && i < R1CDiscoveryPolicy.MAX_CLASS_PROBES; i++) {
            final String className = MODEL_CLASSES[i];
            boolean found = false;
            boolean stringGetIdShape = false;
            try {
                Class<?> klass = Class.forName(className, false, loader);
                found = true;
                if ("com.instagram.feed.media.Media".equals(className)) {
                    // Inspect declaration shape only; NEVER invoke getId or read values.
                    for (Method m : klass.getDeclaredMethods()) {
                        if ("getId".equals(m.getName()) && m.getParameterCount() == 0
                                && m.getReturnType() == String.class
                                && !Modifier.isStatic(m.getModifiers())) {
                            stringGetIdShape = true;
                            break;
                        }
                    }
                }
            } catch (Throwable ignored) {
                // A missing class or linkage error is diagnostic, never fatal.
            }
            if (found) classesFound++;
            Log.i("INA_R1C", "INA_R1C_MODEL slot=" + i + " present=" + found
                    + " string_id_getter_shape=" + stringGetIdShape
                    + " identity=unavailable actions=off");
        }

        int unique = 0, missing = 0, ambiguous = 0, failed = 0;
        File nativeFile = nativeLibraryDir == null ? null
                : new File(nativeLibraryDir, "libdexkit.so");
        if (nativeFile == null || !nativeFile.isFile()) {
            Log.w("INA_R1C", "INA_R1C_QUERY_SKIP reason=native_library_missing actions=off");
        } else if (apkPath == null || apkPath.isEmpty()) {
            Log.w("INA_R1C", "INA_R1C_QUERY_SKIP reason=missing_apk actions=off");
        } else {
            try {
                if (NATIVE_LOADED.compareAndSet(false, true)) {
                    try { System.load(nativeFile.getAbsolutePath()); }
                    catch (Throwable e) { NATIVE_LOADED.set(false); throw e; }
                }
                try (DexKitBridge bridge = DexKitBridge.create(apkPath)) {
                    for (int i = 0; i < MARKERS.length
                            && i < R1CDiscoveryPolicy.MAX_MARKER_QUERIES; i++) {
                        String[] marker = MARKERS[i];
                        try {
                            MethodDataList candidates = bridge.findMethod(FindMethod.create()
                                    .matcher(MethodMatcher.create().usingStrings(marker[1])));
                            int matches = candidates.size();
                            R1CDiscoveryPolicy.Outcome outcome =
                                    R1CDiscoveryPolicy.classify(matches);
                            if (outcome == R1CDiscoveryPolicy.Outcome.UNIQUE_CANDIDATE) unique++;
                            else if (outcome == R1CDiscoveryPolicy.Outcome.MISSING) missing++;
                            else ambiguous++;
                            // Counts only: no obfuscated descriptors or user content in logs.
                            Log.i("INA_R1C", "INA_R1C_ANCHOR id=" + marker[0]
                                    + " status=" + outcome + " count=" + matches
                                    + " identity=unavailable actions=off");
                        } catch (Throwable failure) {
                            failed++;
                            Log.w("INA_R1C", "INA_R1C_ANCHOR id=" + marker[0]
                                    + " status=FAILED type="
                                    + failure.getClass().getSimpleName() + " actions=off");
                        }
                    }
                }
            } catch (Throwable failure) {
                failed++;
                Log.w("INA_R1C", "INA_R1C_QUERY_FAILED type="
                        + failure.getClass().getSimpleName() + " actions=off");
            }
        }
        Log.i("INA_R1C", "INA_R1C_SUMMARY version=" + version
                + " classes=" + classesFound + "/" + R1CDiscoveryPolicy.MAX_CLASS_PROBES
                + " unique=" + unique + " missing=" + missing
                + " ambiguous=" + ambiguous + " failed=" + failed
                + " identity_verified=false binding_hooks=off actions=off");
    }
}
