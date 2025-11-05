package de.safti.specs.utils;

import org.jetbrains.annotations.Contract;

import java.lang.reflect.Array;

public class Checkers {


    public static void assertEquality(Object o1, Object o2) throws IllegalStateException {
        if(unequal(o1, o2)) throw new IllegalStateException("Expected values to be equal: %s (%s) != %s (%s)".formatted(o1, o2, o1.getClass().getCanonicalName(), o2.getClass().getCanonicalName()));
    }

    public static boolean unequal(Object o1, Object o2) {
        return !equality(o1, o2);
    }

    @Contract(pure = true)
    public static boolean equality(Object o1, Object o2) {
        if(o1 == o2) return true;
        if(o1 == null || o2 == null) return false;
        if(o1.getClass() != o2.getClass()) return false;
        if(o1.getClass().isArray()) return arrayEquality(o1, o2);
        return o1.equals(o2);
    }

    @Contract(pure = true)
    public static boolean arrayEquality(Object o1, Object o2) {
        int length1 = Array.getLength(o1);
        int length2 = Array.getLength(o2);
        if(length1 != length2) return false;

        for (int i = 0; i < length1; i++) {
            if(unequal(Array.get(o1, i), Array.get(o2, i))) return false;
        }
        return true;
    }

}
