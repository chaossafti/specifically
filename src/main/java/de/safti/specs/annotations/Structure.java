package de.safti.specs.annotations;

import de.safti.specs.SpecFormatException;
import de.safti.specs.layout.common.StructureDef;
import de.safti.specs.layout.common.TypeDef;
import de.safti.specs.utils.GenericTypeResolver;
import de.safti.specs.utils.Reflect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.*;
import java.util.*;

public class Structure {
    static Map<Class<? extends Annotation>, StructureFactory> STRUCTURES = new HashMap<>();

    static {

        // ARRAY

        STRUCTURES.put(ArrayFixed.class, (rType, structureAnn, typeAnn, context) -> {
            if(!(structureAnn instanceof ArrayFixed ann))
                throw new IllegalStateException(structureAnn.getClass().getCanonicalName());
            if(!Reflect.getClass(rType).isArray())
                throw new IllegalStateException("Expected SpecField setType to be array, but got: " + rType + " at " + context);


            int arrayDimensions = Reflect.getArrayDimensions(Reflect.getClass(rType));
            if(ann.value().length != arrayDimensions)
                throw new SpecFormatException("Expected %d dimension sizes but got %d".formatted(arrayDimensions, ann.value().length));

            Class<?> componentType = Reflect.getComponentClass(Reflect.getClass(rType));
            TypeDef type = Type.getTypeDef(componentType, typeAnn, context);
            return new StructureDefs.ArrayFixedDef(type, Reflect.getComponentClass(Reflect.getClass(rType)), ann.value());
        });
        STRUCTURES.put(ArrayDynamic.class, (rType, structureAnn, typeAnn, context) -> {
            if(!(structureAnn instanceof ArrayDynamic ann))
                throw new IllegalStateException(structureAnn.getClass().getCanonicalName());
            if(!Reflect.getClass(rType).isArray())
                throw new IllegalStateException("Expected SpecField setType to be array, but got: " + rType + " at " + context);

            TypeDef type = Type.getTypeDef(Reflect.getClass(rType).getComponentType(), typeAnn, context);
            return new StructureDefs.ArrayDynamicDef(type, Reflect.getClass(rType).getComponentType(), ann.value(), ann.bitCount(), ann.useUnsigned());
        });


        // LISTS
        STRUCTURES.put(ListFixed.class, (rType, structureAnn, typeAnn, context) -> {
            if(!(structureAnn instanceof ListFixed ann))
                throw new IllegalStateException(structureAnn.getClass().getCanonicalName());
            if(!Collection.class.isAssignableFrom(Reflect.getClass(rType)))
                throw new IllegalStateException("Expected SpecField setType to be list, but got: " + rType + " at " + context);

            java.lang.reflect.Type typeParameter = GenericTypeResolver.findGenericTypeArguments(rType, Collection.class)[0];

            TypeDef type = Type.getTypeDef(Reflect.getClass(typeParameter), typeAnn, context);
            return new StructureDefs.ListFixedDef<>(type, Reflect.getClass(typeParameter), ann.value(), ann.listType());
        });
        STRUCTURES.put(ListDynamic.class, (rType, structureAnn, typeAnn, context) -> {
            if(!(structureAnn instanceof ListDynamic ann))
                throw new IllegalStateException(structureAnn.getClass().getCanonicalName());
            if(!Collection.class.isAssignableFrom(Reflect.getClass(rType)))
                throw new IllegalStateException("Expected SpecField type to be list, but got: " + rType + " at " + context);


            java.lang.reflect.Type typeParameter = GenericTypeResolver.findGenericTypeArguments(rType, Collection.class)[0];

            TypeDef type = Type.getTypeDef(Reflect.getClass(typeParameter), typeAnn, context);
            return new StructureDefs.ListDynamicDef<>(type, Reflect.getClass(typeParameter), ann.value(), ann.autoFieldBitSize(), ann.useUnsigned(), ann.listType());
        });


        // SETS
        STRUCTURES.put(SetFixed.class, (rType, structureAnn, typeAnn, context) -> {
            if(!(structureAnn instanceof SetFixed ann))
                throw new IllegalStateException(structureAnn.getClass().getCanonicalName());

            Class<?> setClass = ann.setType().getSetClass();
            Class<?> rawType = Reflect.getClass(rType);

            if(!Collection.class.isAssignableFrom(rawType))
                throw new IllegalStateException("Expected SpecField type to be a set, but got: " + rType + " at " + context);

            if(!rawType.isAssignableFrom(setClass))
                throw new IllegalStateException(
                        "The declared field type " + rawType.getCanonicalName()
                                + " cannot store instances of " + setClass.getCanonicalName()
                                + " at " + context
                );

            java.lang.reflect.Type typeParameter = GenericTypeResolver.findGenericTypeArguments(rType, Collection.class)[0];
            TypeDef type = Type.getTypeDef(Reflect.getClass(typeParameter), typeAnn, context);
            return new StructureDefs.SetFixedDef<>(type, Reflect.getClass(typeParameter), ann.value(), ann.setType());
        });

        STRUCTURES.put(SetDynamic.class, (rType, structureAnn, typeAnn, context) -> {
            if(!(structureAnn instanceof SetDynamic ann))
                throw new IllegalStateException(structureAnn.getClass().getCanonicalName());
            if(!Collection.class.isAssignableFrom(Reflect.getClass(rType)))
                throw new IllegalStateException("Expected SpecField type to be a set, but got: " + rType + " at " + context);


            java.lang.reflect.Type typeParameter = GenericTypeResolver.findGenericTypeArguments(rType, Collection.class)[0];

            TypeDef type = Type.getTypeDef(Reflect.getClass(typeParameter), typeAnn, context);
            return new StructureDefs.SetDynamicDef<>(type, Reflect.getClass(typeParameter), ann.value(), ann.autoFieldBitSize(), ann.useUnsigned(), ann.setType());
        });


        // MISC

        STRUCTURES.put(Optional.class, (rType, structureAnn, typeAnn, context) -> {
            if(!(structureAnn instanceof Optional ann))
                throw new IllegalStateException(structureAnn.getClass().getCanonicalName());


            Class<?> rClass = Reflect.getClass(rType);
            java.lang.reflect.Type typeParameter;
            if(rClass == Optional.class) typeParameter = GenericTypeResolver.findGenericTypeArguments(rType, Optional.class)[0];
            else if(rClass == OptionalInt.class) typeParameter = int.class;
            else if(rClass == OptionalLong.class) typeParameter = long.class;
            else if(rClass == OptionalDouble.class) typeParameter = double.class;
            else typeParameter = rClass;


            TypeDef type = Type.getTypeDef(Reflect.getClass(typeParameter), typeAnn, context);
            return new StructureDefs.OptionalDef<>(type, Reflect.getClass(rType));
        });
    }

    @Nullable
    public static StructureDef getStructure(@NotNull Annotation structureAnnotation, Annotation typeAnnotation, java.lang.reflect.Type rType, String context) {
        StructureFactory factory = STRUCTURES.get(structureAnnotation.annotationType());
        if(factory == null) return null;
        try {
            return factory.create(rType, structureAnnotation, typeAnnotation, context);
        } catch (Exception e) {
            throw new SpecFormatException("Exception whilst getting structure of " + context, e);
        }
    }

    public static boolean isStructure(@NotNull Annotation annotation) {
        return STRUCTURES.containsKey(annotation.annotationType());
    }

    /**
     * SpecFields annotated with this annotation are expected to be of setType array of fixed size.
     * A {@link Type}.* annotation must be present.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    public @interface ArrayFixed {

        /**
         * Gets the dimensions of the array. Supports multidimensional arrays:
         * if the given return value is [1, 2] and the SpecType type is int[][], int[1][2] will be returned.
         *
         * @return The dimensions of the array.
         */
        int[] value();

    }

    /**
     * SpecFields annotated with this annotation are expected to be of setType array of dynamic size.
     * That means a field of the dimensions of the array must be present.
     * A {@link Type}.* annotation must be present.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    public @interface ArrayDynamic {

        /**
         * @return The field name of the dimensions of the array, or {@code @auto} to auto generate one
         */
        String value();


        /**
         * Defaults to 16 bits (short)
         *
         * @return The bitcount to use when the {@link #value()} field is {@code @auto}.
         */
        int bitCount() default 16;

        /**
         * Only works if {@link #value()} is {@code @auto}
         * Defaults to true
         *
         * @return true if the auto generated field should be unsigned
         */
        boolean useUnsigned() default true;

    }

    /**
     * SpecFields annotated with this annotation are expected to be of setType list of fixed size.
     * That means a fixed number of elements will be read in and written out.
     * A {@link Type}.* annotation must be present.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    public @interface ListFixed {

        /**
         * @return The dimensions of the list.
         */
        int value();

        /**
         * @return The setType of list to instantiate.
         */
        ListType listType() default ListType.ARRAY;

    }

    /**
     * SpecFields annotated with this annotation are expected to be of setType list of fixed size.
     * That means a fixed number of elements will be read in and written out.
     * A {@link Type}.* annotation must be present.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    public @interface ListDynamic {

        /**
         * @return The field name of the dimensions field. Supports {@code @auto} to auto prepend a dimensions field.
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

        /**
         * @return The setType of list to instantiate.
         */
        ListType listType() default ListType.ARRAY;

    }


    /**
     * SpecFields annotated with this annotation are expected to be of setType list of fixed size.
     * That means a fixed number of elements will be read in and written out.
     * A {@link Type}.* annotation must be present.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    public @interface SetFixed {

        /**
         * @return The dimensions of the list.
         */
        int value();

        /**
         * @return The setType of list to instantiate.
         */
        SetType setType() default SetType.HASH_SET;

    }

    /**
     * SpecFields annotated with this annotation are expected to be of type set of fixed size.
     * That means a fixed number of elements will be read in and written out.
     * A {@link Type}.* annotation must be present.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    public @interface SetDynamic {

        /**
         * @return The field name of the dimensions field. Supports {@code @auto} to auto prepend a dimensions field.
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

        /**
         * @return The setType of list to instantiate.
         */
        SetType setType() default SetType.HASH_SET;

    }

    /**
     * Marks a SpecField as optional. Optional SpecFields may have {@code null} supplied to them,
     * or be an {@link java.util.Optional}, {@link OptionalInt}, {@link OptionalDouble} or {@link OptionalLong}.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    public @interface Optional {

    }

    public interface StructureFactory {

        StructureDef create(java.lang.reflect.Type rType, Annotation structureAnn, Annotation typeAnn, String context);

    }


}
