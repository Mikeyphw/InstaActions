// SPDX-License-Identifier: GPL-3.0-only
// Exact source anchors verified against ReSo7200/InstaEclipse at
// e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d.
// ReelDownloadHook.installReduceOptionsListPatch & ReelsControlsHook.installScrollLock.
// Source-adapted, read-only fingerprint confirmation: no Instagram hook, mutation or Media getter.
package com.mikeyphw.instaactions;

import android.util.Log;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicBoolean;
import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodDataList;

/** Checks actual IG455 DEX against pinned InstaEclipse fingerprints, once, in background. */
final class R1CD06UpstreamConformance {
    static final String UPSTREAM_SHA = "e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d";
    private static final String OPTION = "Lcom/instagram/feed/media/mediaoption/MediaOption$Option;";
    private static final String PAGER_MARKER =
            "android_purge_26_q3_ClipsViewPagerImpl_getViewAtIndex";
    static final int MAX_QUERY_RESULTS = 32;
    static final int MAX_PAGER_FIELDS = 512;
    enum Menu { NOT_RUN, SCANNING, MATCH_EXACT_D04, DIFFERENT_METHOD,
        NO_MATCH, AMBIGUOUS, QUERY_FAILED }
    enum Pager { NOT_RUN, SCANNING, UNIQUE_WITH_VIEWPAGER2_FIELD,
        UNIQUE_NO_VIEWPAGER2_FIELD, AMBIGUOUS_VIEWPAGER2_FIELDS, NO_MATCH, AMBIGUOUS,
        REFLECTION_FAILED, QUERY_FAILED }
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);
    private static volatile Menu menu = Menu.NOT_RUN;
    private static volatile Pager pager = Pager.NOT_RUN;
    private static volatile int menuCount, pagerCount;
    private static volatile String failure = "NONE";
    private R1CD06UpstreamConformance() { }

    static Menu classifyMenu(int count, boolean exact) {
        if (count <= 0) return Menu.NO_MATCH;
        if (count != 1) return Menu.AMBIGUOUS;
        return exact ? Menu.MATCH_EXACT_D04 : Menu.DIFFERENT_METHOD;
    }
    static Pager classifyPager(int count, int directFields) {
        if (count <= 0) return Pager.NO_MATCH;
        if (count != 1) return Pager.AMBIGUOUS;
        if (directFields < 0) return Pager.REFLECTION_FAILED;
        if (directFields == 1) return Pager.UNIQUE_WITH_VIEWPAGER2_FIELD;
        if (directFields > 1) return Pager.AMBIGUOUS_VIEWPAGER2_FIELDS;
        return Pager.UNIQUE_NO_VIEWPAGER2_FIELD;
    }
    static String summary() {
        return "menu=" + menu + " pager=" + pager;
    }
    static String nextTarget() {
        if (!R1CD04PassiveProbe.isInstalled()) return "VERIFY_IG455_EXACT_GATE";
        if (menu == Menu.NOT_RUN || menu == Menu.SCANNING || pager == Pager.NOT_RUN
                || pager == Pager.SCANNING) return "SOURCE_SCAN_NOT_FINISHED";
        if (menu != Menu.MATCH_EXACT_D04)
            return "CONFIRM_REEL_MENU_BUILDER_DESCRIPTOR_BEFORE_ANY_NEW_HOOK";
        if (pager != Pager.UNIQUE_WITH_VIEWPAGER2_FIELD)
            return "CONFIRM_UPSTREAM_NATIVE_PAGER_BINDER";
        return "ONLY_REMAINING_GATE_ACTIVE_PAGER_PAGE_TO_MENU_MEDIA";
    }
    static String report() {
        return "INA_D06_INSTAECLIPSE_SOURCE_CONFORMANCE_V1\n"
                + "UPSTREAM=ReSo7200/InstaEclipse@" + UPSTREAM_SHA + "\n"
                + "UPSTREAM_MENU_ANCHOR=ReelDownloadHook.reduce_options_PLAYBACK_CONTROLS_UNSAVE\n"
                + "UPSTREAM_PAGER_ANCHOR=ReelsControlsHook.ClipsViewPagerImpl_getViewAtIndex\n"
                + "TARGET_APK_EXACT_HASH_GATE=" + R1CD04PassiveProbe.isInstalled() + "\n"
                + "NATIVE_MENU_DEX_CONFORMANCE=" + menu + "\n"
                + "NATIVE_MENU_MATCH_COUNT=" + menuCount + "\n"
                + "NATIVE_PAGER_DEX_CONFORMANCE=" + pager + "\n"
                + "NATIVE_PAGER_MARKER_COUNT=" + pagerCount + "\n"
                + "DEX_QUERY_FAILURE_CATEGORY=" + failure + "\n"
                + "INSTA_ECLIPSE_HOOK_ACTIVITY=NOT_PROGRAMMATICALLY_VERIFIED\n"
                + "VISIBLE_REEL_IDENTITY=NOT_VERIFIED\nACTIONS=OFF\n";
    }

    /** Runs only after D04 has passed the exact IG455 SHA256 gate and installed. */
    static void startAfterExactGate(String apkPath, String moduleNativeLibraryDir,
            ClassLoader instagramLoader) {
        if (apkPath == null || instagramLoader == null
                || !R1CD04PassiveProbe.isInstalled()
                || !STARTED.compareAndSet(false, true)) return;
        menu = Menu.SCANNING;
        pager = Pager.SCANNING;
        Thread worker = new Thread(() -> scan(apkPath, moduleNativeLibraryDir,
                instagramLoader), "ina-ie-source-conformance");
        worker.setDaemon(true);
        worker.setPriority(Thread.MIN_PRIORITY);
        worker.start();
    }
    private static void scan(String apkPath, String libDir, ClassLoader loader) {
        try {
            File lib = libDir == null ? null : new File(libDir, "libdexkit.so");
            if (lib == null || !lib.isFile()) throw new IllegalStateException("NATIVE_LIB_MISSING");
            // Existing R1/R1C inventory also uses DexKit; same process/classloader.
            System.load(lib.getAbsolutePath());
            try (DexKitBridge bridge = DexKitBridge.create(apkPath)) {
                try {
                    MethodDataList matches = bridge.findMethod(FindMethod.create()
                            .matcher(MethodMatcher.create()
                                .returnType("java.util.ArrayList")
                                .addUsingField(OPTION + "->PLAYBACK_CONTROLS:" + OPTION)
                                .addUsingField(OPTION + "->UNSAVE:" + OPTION)));
                    menuCount = Math.min(MAX_QUERY_RESULTS + 1, matches.size());
                    if (matches.size() > MAX_QUERY_RESULTS) menu = Menu.AMBIGUOUS;
                    else menu = classifyMenu(matches.size(), matches.size() == 1
                            && R1CModernReelsPolicy.exact(matches.get(0).getDescriptor()));
                } catch (Throwable failed) { menu = Menu.QUERY_FAILED; failure = "MENU_QUERY"; }
                try {
                    MethodDataList matches = bridge.findMethod(FindMethod.create()
                            .matcher(MethodMatcher.create().usingStrings(PAGER_MARKER)));
                    pagerCount = Math.min(MAX_QUERY_RESULTS + 1, matches.size());
                    if (matches.size() != 1) {
                        pager = classifyPager(matches.size(), 0);
                    } else {
                        // As in upstream ReelsControlsHook.viewPagerField, only a
                        // declaring-class ViewPager2-typed field is relevant.
                        Class<?> declaring = Class.forName(matches.get(0).getClassName(), false, loader);
                        Field[] fields = declaring.getDeclaredFields();
                        if (fields.length > MAX_PAGER_FIELDS) pager = Pager.REFLECTION_FAILED;
                        else {
                            int pagerFields = 0;
                            for (Field field : fields) {
                                if (!Modifier.isStatic(field.getModifiers()) &&
                                    "androidx.viewpager2.widget.ViewPager2".equals(
                                        field.getType().getName())) pagerFields++;
                            }
                            pager = classifyPager(1, pagerFields);
                        }
                    }
                } catch (Throwable failed) { pager = Pager.QUERY_FAILED; failure = "PAGER_QUERY"; }
            }
        } catch (Throwable failed) {
            if (menu == Menu.SCANNING) menu = Menu.QUERY_FAILED;
            if (pager == Pager.SCANNING) pager = Pager.QUERY_FAILED;
            failure = "INITIALIZE_" + (failed instanceof UnsatisfiedLinkError
                    ? "NATIVE_LINK" : "DEXKIT");
        }
        Log.i("INA_R1C", "INA_D06_IE_SOURCE menu=" + menu + " menu_count=" + menuCount
                + " pager=" + pager + " pager_count=" + pagerCount
                + " failure=" + failure + " actions=off");
    }
}
