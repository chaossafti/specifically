package de.safti.specs.utils;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.io.InputStream;

// https://github.com/wmacevoy/kiss/blob/master/src/main/java/kiss/util/Reflect.java - modified to support fields and other methods added
public class Reflect {


    /**
     * Get the underlying class for a setType, or null if the setType is a variable setType.
     * @param type the setType
     * @return the underlying class
     */
    @NotNull
    public static Class<?> getClass(@NotNull Type type) {
        switch (type) {
            case Class<?> clazz -> {
                return clazz;
            }
            case ParameterizedType parameterizedType -> {
                return getClass(parameterizedType.getRawType());
            }
            case GenericArrayType genericArrayType -> {
                Type componentType = genericArrayType.getGenericComponentType();
                Class<?> componentClass = getClass(componentType);
                return Array.newInstance(componentClass, 0).getClass();
            }
            case null -> throw new IllegalArgumentException("Got null");
            default -> throw new IllegalArgumentException("Got " + type + "(" + type.getClass().getCanonicalName() + ")");
        }
    }

    /**
     * Gets the number of dimensions of an array class.
     *
     * @param arrayClass The class to check.
     * @return The number of dimensions.
     */
    public static int getArrayDimensions(@NotNull Class<?> arrayClass) {
        Objects.requireNonNull(arrayClass);

        if (!arrayClass.isArray()) {
            throw new IllegalArgumentException("Given argument is not an array: " + arrayClass);
        }

        String className = arrayClass.getName();
        int dimensions = 0;

        // Count the number of leading '['
        while (dimensions < className.length() && className.charAt(dimensions) == '[') {
            dimensions++;
        }

        return dimensions;
    }

    public static Class<?> getComponentClass(Class<?> arrayClass) {
        Objects.requireNonNull(arrayClass);

        if (!arrayClass.isArray()) {
            throw new IllegalArgumentException("Given argument is not an array: " + arrayClass);
        }

        // Unwrap array dimensions until we reach the base component type
        while (arrayClass.isArray()) {
            arrayClass = arrayClass.getComponentType();
        }

        return arrayClass;
    }

    // Reuse the same offset logic for both methods and fields
    private record MemberOffset<T extends Member>(T member, int offset) implements Comparable<MemberOffset<T>> {

        @Override
        public int compareTo(MemberOffset<T> o) {
            return Integer.compare(this.offset, o.offset);
        }
    }

    static class ByLength implements Comparator<Member> {
        @Override
        public int compare(Member a, Member b) {
            return b.getName().length() - a.getName().length();
        }
    }

    private static String readClassFileAsString(Class<?> clazz) throws IOException {
        String resource = clazz.getName().replace('.', '/') + ".class";
        try(InputStream is = clazz.getClassLoader().getResourceAsStream(resource)) {
            if(is == null) return "";
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[16 * 1024];
            int n;
            while((n = is.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }
            String sdata = baos.toString(java.nio.charset.StandardCharsets.UTF_8);
            int lnt = sdata.indexOf("LineNumberTable");
            if(lnt != -1) sdata = sdata.substring(lnt + "LineNumberTable".length() + 3);
            int cde = sdata.lastIndexOf("SourceFile");
            if(cde != -1) sdata = sdata.substring(0, cde);
            return sdata;
        }
    }

    /**
     * Shared ordering logic
     */
    private static <T extends Member> T[] orderMembersByClassPosition(T[] members, String classText) {
        if(classText.isEmpty() || members.length == 0) return members;

        Arrays.sort(members, new ByLength());
        @SuppressWarnings("unchecked")
        MemberOffset<T>[] offsets = new MemberOffset[members.length];

        for (int i = 0; i < members.length; i++) {
            int pos = -1;
            for (; ; ) {
                pos = classText.indexOf(members[i].getName(), pos + 1);
                if(pos == -1) break;
                boolean subset = false;
                for (int j = 0; j < i; ++j) {
                    if(offsets[j].offset >= 0 &&
                            offsets[j].offset <= pos &&
                            pos < offsets[j].offset + offsets[j].member.getName().length()) {
                        subset = true;
                        break;
                    }
                }
                if(!subset) break;
            }
            offsets[i] = new MemberOffset<>(members[i], pos);
        }

        Arrays.sort(offsets);
        for (int i = 0; i < offsets.length; i++) {
            members[i] = offsets[i].member;
        }

        return members;
    }

    /**
     * Grok the bytecode to get declared method order
     */
    public static Method[] getDeclaredMethodsInOrder(Class<?> clazz) {
        try {
            String classText = readClassFileAsString(clazz);
            Method[] methods = clazz.getDeclaredMethods();
            return orderMembersByClassPosition(methods, classText);
        } catch (Exception ex) {
            ex.printStackTrace();
            return clazz.getDeclaredMethods();
        }
    }

    /**
     * Grok the bytecode to get declared field order
     */
    public static Field[] getDeclaredFieldsInOrder(Class<?> clazz) {
        try {
            String classText = readClassFileAsString(clazz);
            Field[] fields = clazz.getDeclaredFields();
            return orderMembersByClassPosition(fields, classText);
        } catch (Exception ex) {
            ex.printStackTrace();
            return clazz.getDeclaredFields();
        }
    }

    // --- existing methods like getClass(Type) and getTypeArguments(...) remain untouched ---
}

