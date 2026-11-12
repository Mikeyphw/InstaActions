package com.mikeyphw.instaactions;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Exact-IG455 upstream Repost data check. X.0Ie0 selector 4 reads A03 as
 * X.0FWn and returns a singleton when that input is missing. This probe
 * checks only presence/type, never content, account, posts or friends.
 */
final class R1CD07BranchInputs {
    static final int MISSING = 0, PRESENT = 1, UNAVAILABLE = 2;
    private final Field field;
    private final Class<?> expected;
    private R1CD07BranchInputs(Field f, Class<?> t) { field=f; expected=t; }

    static R1CD07BranchInputs preflight(ClassLoader loader) throws ReflectiveOperationException {
        Class<?> owner=Class.forName("X.0Ie0",false,loader);
        Class<?> type=Class.forName("X.0FWn",false,loader);
        Field f=owner.getDeclaredField("A03");
        if(f.getType()!=Object.class || !Modifier.isPublic(f.getModifiers()))
            throw new NoSuchFieldException("IG455 A03 shape changed");
        return new R1CD07BranchInputs(f,type);
    }
    int classify(Object instance) {
        try {
            Object value=field.get(instance);
            return value==null?MISSING:expected.isInstance(value)?PRESENT:UNAVAILABLE;
        } catch(Throwable ignored) { return UNAVAILABLE; }
    }
}
