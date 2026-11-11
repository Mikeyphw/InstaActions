// SPDX-License-Identifier: GPL-3.0-only
// Derived in part from ReSo7200/InstaEclipse SelectedMediaAdapter.java,
// upstream commit e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d.
// Specifically ports the unique direct typed-field / native menu-ownership
// selection strategy. Copyright of the original remains with its authors.
// See third_party/InstaEclipse/NOTICE.md and LICENSE-GPL-3.0.txt.
package com.mikeyphw.instaactions;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Read-only, fail-closed ownership probe for an EXACT IG455 menu-builder call.
 * No Instagram Media value, account, ID, URL or caption is returned or stored.
 * This proves at most menu ownership; not which Reel is visible on screen.
 */
final class R1CD06MenuOwnership {
    static final int MAX_FIELDS = 64;
    static final int MAX_CLASSES = 6;
    enum Verdict {
        SCREEN_UNSTABLE, WRONG_MEDIA_TYPE, WRONG_OWNER_TYPE,
        OWNER_AND_CONTEXT_MATCH, OWNER_DIRECT_MATCH, CONTEXT_DIRECT_MATCH,
        NO_DIRECT_OWNER, MISMATCH, AMBIGUOUS, REFLECTION_FAILED, BUDGET_EXCEEDED
    }
    private enum State { ABSENT, MATCH, OTHER, AMBIGUOUS, REFLECTION_FAILED, BUDGET_EXCEEDED }
    private R1CD06MenuOwnership() { }

    static Verdict resolve(Object receiver, Object context, Object media,
            Class<?> expectedOwner, Class<?> expectedContext, Class<?> exactMedia,
            R1BNavigationPolicy.Screen screen) {
        if (screen != R1BNavigationPolicy.Screen.REELS) return Verdict.SCREEN_UNSTABLE;
        if (exactMedia == null || media == null || !exactMedia.isInstance(media))
            return Verdict.WRONG_MEDIA_TYPE;
        if (expectedOwner == null || expectedContext == null || receiver == null
                || !expectedOwner.isInstance(receiver)
                || (context != null && !expectedContext.isInstance(context)))
            return Verdict.WRONG_OWNER_TYPE;
        State owned = inspectDirect(receiver, exactMedia, media);
        State contextual = context == null ? State.ABSENT : inspectDirect(context, exactMedia, media);
        if (owned == State.REFLECTION_FAILED || contextual == State.REFLECTION_FAILED)
            return Verdict.REFLECTION_FAILED;
        if (owned == State.BUDGET_EXCEEDED || contextual == State.BUDGET_EXCEEDED)
            return Verdict.BUDGET_EXCEEDED;
        if (owned == State.AMBIGUOUS || contextual == State.AMBIGUOUS)
            return Verdict.AMBIGUOUS;
        if (owned == State.OTHER || contextual == State.OTHER) return Verdict.MISMATCH;
        if (owned == State.MATCH && contextual == State.MATCH)
            return Verdict.OWNER_AND_CONTEXT_MATCH;
        if (owned == State.MATCH) return Verdict.OWNER_DIRECT_MATCH;
        if (contextual == State.MATCH) return Verdict.CONTEXT_DIRECT_MATCH;
        return Verdict.NO_DIRECT_OWNER;
    }

    /** Mirrors the upstream ownedDirectMedia/uniqueDirectTypedField selection rule:
     * exact field type, identity uniqueness, no recursion into unrelated holders.
     */
    private static State inspectDirect(Object holder, Class<?> mediaType, Object expected) {
        if (holder == null || mediaType == null) return State.ABSENT;
        Object selected = null;
        int inspected = 0;
        int classes = 0;
        try {
            for (Class<?> type = holder.getClass(); type != null && type != Object.class;
                    type = type.getSuperclass()) {
                if (++classes > MAX_CLASSES) return State.BUDGET_EXCEEDED;
                Field[] fields = type.getDeclaredFields();
                // Enforce cap BEFORE reading any fields, so truncation cannot hide ambiguity.
                if (fields.length > MAX_FIELDS - inspected) return State.BUDGET_EXCEEDED;
                inspected += fields.length;
                for (Field field : fields) {
                    if (Modifier.isStatic(field.getModifiers()) || field.getType() != mediaType)
                        continue;
                    // Reflection is READ-ONLY; never invoke Instagram methods or change values.
                    field.setAccessible(true);
                    Object value = field.get(holder);
                    if (value == null) continue;
                    if (selected != null && selected != value) return State.AMBIGUOUS;
                    selected = value;
                }
            }
        } catch (Throwable e) { return State.REFLECTION_FAILED; }
        if (selected == null) return State.ABSENT;
        return selected == expected ? State.MATCH : State.OTHER;
    }

}
