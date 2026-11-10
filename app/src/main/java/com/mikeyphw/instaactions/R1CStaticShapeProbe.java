package com.mikeyphw.instaactions;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import android.content.Context;
import android.view.View;
import org.luckypray.dexkit.result.MethodDataList;

/**
 * D02 HF01: APK-grounded, bounded declaration-only shape inspection.
 * Never initializes Instagram classes, invokes Instagram methods, hooks,
 * touches account/media values, or treats shape evidence as verified identity.
 */
final class R1CStaticShapeProbe {
    private static final String MEDIA = "com.instagram.feed.media.Media";
    private static final String REEL_ITEM = "Lcom/instagram/model/reels/ReelItem;";
    private static final String STORY_OWNER =
            "Linstagram/features/stories/fragment/ReelViewerFragment;";
    private static final String MEDIA_OPTION =
            "com.instagram.feed.media.mediaoption.MediaOption$Option";

    static final class Summary {
        final int raw, owners, candidateMethods, strongMethods, modernListMethods;
        final boolean truncated;
        Summary(int raw, int owners, int candidate, int strong, int modern, boolean truncated) {
            this.raw = raw;
            this.owners = owners;
            this.candidateMethods = candidate;
            this.strongMethods = strong;
            this.modernListMethods = modern;
            this.truncated = truncated;
        }
        String result() {
            if (truncated) return "BOUNDED_INCOMPLETE";
            if (strongMethods > 1) return "AMBIGUOUS_SIGNATURE";
            if (strongMethods == 1) return "UNIQUE_SIGNATURE_CANDIDATE";
            // Modern Reels builds need a separate menu-list strategy;
            // this is a *candidate*, never proof of actual media binding.
            if (modernListMethods > 1) return "AMBIGUOUS_MODERN_LIST";
            if (modernListMethods == 1) return "MODERN_LIST_CANDIDATE_UNVERIFIED";
            return candidateMethods > 0 ? "WEAK_SIGNATURE_ONLY" : "NO_SIGNATURE_MATCH";
        }
    }

    private R1CStaticShapeProbe() { }

    static Summary inspect(String marker, MethodDataList refs, ClassLoader loader) {
        if (refs == null || loader == null) return new Summary(0, 0, 0, 0, 0, false);
        int raw = refs.size(), owners = 0, candidate = 0, strong = 0, modern = 0;
        Set<String> seen = new HashSet<>();
        boolean truncated = raw > R1CDiscoveryPolicy.MAX_RAW_MATCHES;
        for (int i = 0; i < raw && i < R1CDiscoveryPolicy.MAX_RAW_MATCHES; i++) {
            try {
                String className = refs.get(i).getClassName();
                if (className == null) { truncated = true; continue; }
                if ("story_page_bind".equals(marker)) {
                    // The IG455 story viewer has >192 declared methods; inspecting the
                    // *matched* DexKit method avoids a false bounded-incomplete result.
                    // Descriptor is metadata; no getMethodInstance/reflective invocation.
                    String descriptor = refs.get(i).getDescriptor();
                    if (seen.add(className)) owners++;
                    if (descriptor == null) { truncated = true; continue; }
                    if (descriptor.contains(STORY_OWNER) && descriptor.contains("(" + REEL_ITEM)
                            && descriptor.endsWith(")V") && !descriptor.contains("-><")) {
                        candidate++;
                        strong++;
                    }
                    continue;
                }
                if (!seen.add(className)) continue;
                if (owners >= R1CDiscoveryPolicy.MAX_OWNER_CLASSES) {
                    truncated = true;
                    break;
                }
                owners++;
                Class<?> owner = Class.forName(className, false, loader);
                Method[] methods = owner.getDeclaredMethods();
                if (methods.length > R1CDiscoveryPolicy.MAX_METHODS_PER_OWNER) {
                    truncated = true;
                    continue;
                }
                for (Method m : methods) {
                    int shape = shape(marker, m);
                    if (shape >= 1) candidate++;
                    if (shape >= 2) strong++;
                    if ("reels_options".equals(marker) && modernListShape(m)) modern++;
                }
            } catch (Throwable ignored) {
                // Reflection failures are incomplete evidence, never clean negatives.
                truncated = true;
            }
        }
        return new Summary(raw, owners, candidate, strong, modern, truncated);
    }

    private static int shape(String marker, Method method) {
        if (method == null || method.getReturnType() != void.class) return 0;
        Class<?>[] p = method.getParameterTypes();
        if ("reels_options".equals(marker)) {
            if (p.length < 2 || !MEDIA.equals(p[0].getName())
                    || p[1].isPrimitive() || p[1] == String.class) return 0;
            return buttonAdderShape(p[1]) ? 2 : 1;
        }
        if ("feed_options".equals(marker)) {
            if (!Modifier.isStatic(method.getModifiers()) || p.length < 2) return 0;
            boolean hasOption = false, hasArrayList = false;
            for (Class<?> a : p) {
                if (MEDIA_OPTION.equals(a.getName())) hasOption = true;
                if (java.util.ArrayList.class.isAssignableFrom(a)) hasArrayList = true;
            }
            if (!hasOption || !hasArrayList) return 0;
            // IG455 declares two broad option+ArrayList helpers, but only one
            // starts with MediaOption and ends with an int slot. Still candidate-only.
            return p[0].getName().equals(MEDIA_OPTION)
                    && p[p.length - 1] == int.class ? 2 : 1;
        }
        return 0;
    }

    private static boolean modernListShape(Method method) {
        if (method == null || !Modifier.isStatic(method.getModifiers())
                || method.getReturnType() != java.util.ArrayList.class) return false;
        Class<?>[] p = method.getParameterTypes();
        // This is the modern (controller-context, Media) -> ArrayList signature
        // present in the IG455 APK. Contents/behavior remain unverified.
        return p.length == 2 && !p[0].isPrimitive() && MEDIA.equals(p[1].getName());
    }

    private static boolean buttonAdderShape(Class<?> adder) {
        try {
            Method[] methods = adder.getDeclaredMethods();
            if (methods.length > R1CDiscoveryPolicy.MAX_METHODS_PER_OWNER) return false;
            for (Method m : methods) {
                Class<?>[] p = m.getParameterTypes();
                if (p.length == 4 && Context.class.isAssignableFrom(p[0])
                        && View.OnClickListener.class.isAssignableFrom(p[1])
                        && p[2] == String.class && p[3] == int.class) return true;
            }
        } catch (Throwable ignored) { }
        return false;
    }
}
