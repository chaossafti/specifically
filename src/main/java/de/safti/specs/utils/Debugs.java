package de.safti.specs.utils;

import de.safti.specs.annotations.Structure;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;

public class Debugs {

    public static String toString(Object o) {
        if(o == null) return "null";
        if(o.getClass().isArray()) return arrayToString(o);
        return o.toString();
    }

    public static @NotNull String toStringWClass(Object o) {
        if(o == null) return "null";
        String classPostfix = " (%s)".formatted(o.getClass().getCanonicalName());
        if(o.getClass().isArray()) return arrayToString(o) + classPostfix;
        return o + classPostfix;
    }

    public static @NotNull String arrayToString(Object o) {
        int length = Array.getLength(o);
        if(length == 0) return "[]";

        StringBuilder sb = new StringBuilder("[");

        for (int i = 0; i < length; i++) {
            Object element = Array.get(o, i);
            sb.append(toString(element)).append(", ");
        }

        return sb.substring(0, sb.length() - 2) + "]";
    }

}
