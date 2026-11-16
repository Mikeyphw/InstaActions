// SPDX-License-Identifier: GPL-3.0-only
// Source-guided by ReSo7200/InstaEclipse SelectedMediaAdapter.java at
// e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d, which rejects non-unique
// exact-Media ownership; see third_party/InstaEclipse/NOTICE.md.
package com.mikeyphw.instaactions;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Read-only unique direct-Media-field comparison across *visible page views*.
 * Holders are supplied by the independent Android ViewPager2 page scan, never
 * by the Reel menu owner/context. No IDs, getters, URLs or models escape.
 * A match is candidate evidence until proven on physical IG455.
 */
final class R1CD06DirectViewMedia {
    static final int MAX_VIEWS = 160;
    static final int MAX_FIELDS = 4096;
    static final int MAX_FIELDS_PER_CLASS = 1024;
    static final int MAX_MEDIA_FIELDS = 16;
    static final int MAX_CLASS_DEPTH = 8;
    enum Result { ABSENT, MATCH, MISMATCH, AMBIGUOUS, REFLECTION_FAILED,
        BUDGET_EXCEEDED, VIEW_COUNT_LIMIT, CLASS_DEPTH_LIMIT,
        DECLARED_FIELD_LIMIT, MEDIA_FIELD_LIMIT }
    private R1CD06DirectViewMedia() { }

    static Result compare(Object[] visiblePageViews, Class<?> mediaClass, Object expected) {
        if (visiblePageViews == null || mediaClass == null || expected == null
                || !mediaClass.isInstance(expected) || visiblePageViews.length == 0)
            return Result.ABSENT;
        if (visiblePageViews.length > MAX_VIEWS) return Result.VIEW_COUNT_LIMIT;
        Object selected = null;
        int declared = 0;
        int candidates = 0;
        try {
            for (Object holder : visiblePageViews) {
                if (holder == null) return Result.REFLECTION_FAILED;
                int depth = 0;
                for (Class<?> type = holder.getClass(); type != null && type != Object.class;
                        type = type.getSuperclass()) {
                    String className = type.getName();
                    if (className.startsWith("android.") || className.startsWith("androidx.")
                            || className.startsWith("java.") || className.startsWith("kotlin.")) break;
                    if (++depth > MAX_CLASS_DEPTH) return Result.CLASS_DEPTH_LIMIT;
                    Field[] fields = type.getDeclaredFields();
                    if (fields.length > MAX_FIELDS_PER_CLASS
                            || fields.length > MAX_FIELDS - declared)
                        return Result.DECLARED_FIELD_LIMIT;
                    declared += fields.length;
                    for (Field field : fields) {
                        if (Modifier.isStatic(field.getModifiers())
                                || field.getType() != mediaClass) continue;
                        if (++candidates > MAX_MEDIA_FIELDS) return Result.MEDIA_FIELD_LIMIT;
                        field.setAccessible(true);
                        Object value = field.get(holder);
                        if (value == null) continue;
                        if (selected != null && selected != value) return Result.AMBIGUOUS;
                        selected = value;
                    }
                }
            }
        } catch (Throwable failed) { return Result.REFLECTION_FAILED; }
        if (selected == null) return Result.ABSENT;
        return selected == expected ? Result.MATCH : Result.MISMATCH;
    }
}
