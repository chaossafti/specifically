package de.safti.specs.annotations;

import de.safti.specs.SpecFormatException;
import de.safti.specs.layout.common.TypeDef;
import de.safti.specs.io.BinaryReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.text.AbstractDocument;
import java.lang.annotation.*;
import java.lang.classfile.attribute.StackMapFrameInfo;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * A class containing a bunch of Type annotations used for annotation based spec definition.
 */
public final class Type {

    static final Map<Class<? extends Annotation>, BiFunction<Class<?>, Annotation, TypeDef>> TYPES = new HashMap<>();

    static {
        // --- ints ---

        TYPES.put(Int.class, (rType, annotation) -> {
            if(!(annotation instanceof Int ann)) throw new IllegalArgumentException(annotation.getClass().getCanonicalName());
            if(!Number.class.isAssignableFrom(rType) && rType != int.class && rType != byte.class && rType != short.class && rType != long.class) throw new SpecFormatException("Expected return setType to extend Number, but got: " + rType.getCanonicalName());

            return new TypeDefs.IntDef(ann.value(), rType);
        });
        TYPES.put(VarInt.class, (rType, annotation) -> {
            if(!(annotation instanceof VarInt)) throw new IllegalArgumentException(annotation.getClass().getCanonicalName());
            if(!Number.class.isAssignableFrom(rType) && rType != int.class && rType != byte.class && rType != short.class && rType != long.class) throw new SpecFormatException("Expected return setType to extend Number, but got: " + rType.getCanonicalName());

            return new TypeDefs.VarIntDef(rType);
        });
        TYPES.put(UVarInt.class, (rType, annotation) -> {
            if(!(annotation instanceof UVarInt)) throw new IllegalArgumentException(annotation.getClass().getCanonicalName());
            if(!Number.class.isAssignableFrom(rType) && rType != int.class && rType != byte.class && rType != short.class && rType != long.class) throw new SpecFormatException("Expected return setType to extend Number, but got: " + rType.getCanonicalName());

            return new TypeDefs.UVarIntDef(rType);
        });


        // --- Decimals ---

        TYPES.put(Float.class, (rType, annotation) -> {
            if(!(annotation instanceof Float)) throw new IllegalArgumentException(annotation.getClass().getCanonicalName());
            if(!rType.isAssignableFrom(java.lang.Float.class) && !rType.isAssignableFrom(float.class)) throw new SpecFormatException("Return setType is not a float!");
            return TypeDefs.FloatDef.INSTANCE;
        });
        TYPES.put(Double.class, (rType, annotation) -> {
            if(!(annotation instanceof Double)) throw new IllegalArgumentException(annotation.getClass().getCanonicalName());
            if(!rType.isAssignableFrom(java.lang.Double.class) && !rType.isAssignableFrom(double.class)) throw new SpecFormatException("Return setType is not a double!");
            return TypeDefs.DoubleDef.INSTANCE;
        });

        // --- Strings ---

        TYPES.put(StringTerminated.class, (rType, annotation) -> {
            if(!(annotation instanceof StringTerminated ann)) throw new IllegalArgumentException(annotation.getClass().getCanonicalName());
            if(rType != String.class) throw new SpecFormatException("Return setType is not a String!");
            return new TypeDefs.StringTerminatedDef(ann.terminator());
        });
        TYPES.put(StringDynamic.class, (rType, annotation) -> {
            if(!(annotation instanceof StringDynamic ann)) throw new IllegalArgumentException(annotation.getClass().getCanonicalName());
            return new TypeDefs.StringDynamicDef(ann.value(), ann.autoFieldBitSize(), ann.useUnsigned());
        });
        TYPES.put(StringFixed.class, (rType, annotation) -> {
            if(!(annotation instanceof StringFixed ann)) throw new IllegalArgumentException(annotation.getClass().getCanonicalName());
            final int length = ann.value();
            return new TypeDefs.StringFixedDef(length, ann.slicing(), ann.autoResize(), ann.cutPadding());
        });


        // Enums
        TYPES.put(Enum.class, (rType, annotation) -> {
            if(!(annotation instanceof Enum ann)) throw new IllegalArgumentException(annotation.getClass().getCanonicalName());
            return new TypeDefs.EnumTypeDef<>(ann.value(), ann.bits());
        });

    }


    // TODO: ParameterizedType for more context
    public static @Nullable TypeDef getTypeDef(Class<?> rType, @NotNull Annotation annotation, String context) {
        // The Annotation class is actually a Proxy and the annotation is represented as an interface.
        // that means we need to extract the proxies interface, which is guaranteed to be the first and only interface
        Class<?> annotationClass = annotation.getClass().getInterfaces()[0];


        try {
            var function = TYPES.get(annotationClass);
            if(function == null) return null;
            return function.apply(rType, annotation);
        } catch (SpecFormatException exception) {
            throw new SpecFormatException(exception.getMessage() + " At " + context);
        }
    }

    public static boolean isType(Annotation annotation) {
        return TYPES.containsKey(annotation.annotationType());
    }

    private Type() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
    }

    // --- Annotation Definitions ---

    /**
     * Marks a field as a signed integer with a specific bit width.
     * The value will be read as an {@code int}.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Int {
        /**
         * @return The bit size (1-32) of the signed integer.
         */
        int value();
    }

    /**
     * LEB128 standard of defining signed VarInts.
     * A VarInt is an integer whose MSB is a continue flag and the other 7 bits defining the number.
     * Uses Two's complement to represent the sign.
     *
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface VarInt {

    }

    /**
     * LEB128 standard of defining unsigned VarInts.
     * A VarInt is an integer whose MSB is a continue flag and the other 7 bits defining the number.
     *
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface UVarInt {

    }

    /**
     * Marks a field as a 32-bit single-precision floating-point number (float).
     * Corresponds to {@link BinaryReader#readFloat()}.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Float {
        // No value needed, implies standard 32 bits
    }

    /**
     * Marks a field as a 64-bit double-precision floating-point number (double).
     * Corresponds to {@link BinaryReader#readDouble()}.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Double {
        // No value needed, implies standard 64 bits
    }

    /**
     * Marks a field as a string that is read byte-by-byte until a
     * specific terminator character is found. The terminator is consumed but not included.
     * The resulting string is decoded as UTF-8.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface StringTerminated {
        /**
         * @return The single-byte terminator character to stop reading at.
         */
        char terminator() default '\0';
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface StringDynamic {
        /**
         * Returns the field that the dimensions is read from. The returned field must appear before the string field.
         * If a field name is provided the field must be manually populated.
         * <p>
         * May be {@code @auto} to automatically prepend a field.
         * Using {@code @auto} does not require you to manually populate a field.
         *
         *
         * @return The name of the field used to generate the dimensions.
         * @see #useUnsigned() toggle sign when using @auto
         * @see #autoFieldBitSize() Specify bitcount when using @auto
         *
         */
        String value();

        /**
         * The returned value is used as the amount of bits to represent a string.
         * Providing this does nothing if {@link #value()} is not {@code @auto}.
         * Returns 16 by default.
         *
         * @return The amount of bits to represent the dimensions of the string with.
         * @see #useUnsigned() Disable unsigned usage.
         */
        int autoFieldBitSize() default 16;

        /**
         * {@link #value()} must be {@code @auto} for this to do anything.
         * Decides if the prepended dimensions field should be an unsigned number or not.
         * Returns true by default.
         *
         * @return true if the string dimensions should be written/read as unsigned number.
         * @see #autoFieldBitSize() Provide amount of bits to use.
         */
        boolean useUnsigned() default true;

    }

    /**
     * Marks a field as a string with a fixed, known dimensions in bytes.
     * Exactly {@link #value()} bytes are read and decoded as a UTF-8 string.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface StringFixed {
        /**
         * @return The fixed dimensions (in bytes) of the string.
         */
        int value();

        /**
         * If auto resizing is enabled, too small strings will be padded with the {@code \0} char.
         * If disabled and a string with too little dimensions is written an exception is thrown.
         *
         *
         * @return If auto resizing should be applied
         * @see #slicing() allowing longer strings
         */
        boolean autoResize() default true;

        /**
         * {@link #autoResize()} must be enabled for this to do anything.
         * If enabled, padding added by {@link #autoResize()} will be removed when read back in.
         * If disabled, {@code \0} may be included in the string you've read back in.
         *
         * @return If padding added by {@link #autoResize()} should be removed when the string is read back in.
         */
        boolean cutPadding() default false;

        /**
         * If enabled and a too long string is provided it will be sliced into taking the exact amount of chars needed.
         * If disabled, an exception will be thrown.
         *
         * @return If too large strings should be sliced.
         * @see #autoResize() allowing shorter strings
         */
        boolean slicing() default false;
    }


    public static final int ENUM_BITS_AUTO = -1;

    /**
     * An enum type. Stores the ordinal as integer.
     * Data will corrupt by changing the order of the enum entries.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Enum {

        /**
         * @return The enum class to use.
         */
        Class<? extends java.lang.Enum<?>> value();

        /**
         * @return The bit size of the enum. If {@link #ENUM_BITS_AUTO} is used, the smallest possible bit size will be used.
         */
        int bits() default ENUM_BITS_AUTO;
    }





}
