package de.safti.specs.layout.common;

import de.safti.specs.SpecFormatException;
import de.safti.specs.annotations.Field.Leaker;
import de.safti.specs.annotations.Field.Setter;
import de.safti.specs.annotations.Spec;
import de.safti.specs.annotations.Structure;
import de.safti.specs.annotations.Type;
import de.safti.specs.utils.Reflect;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

// TODO: setter MethodHandle to internal field instead of Field reference
public record SpecField(String name, Class<?> clazz,
                        @Nullable MethodHandle setterMethod, MethodHandle getter,
                        StableValue<Field> internalField, TypeDef type
) {

    public SpecField(String name, Class<?> clazz, @Nullable MethodHandle setterMethod, MethodHandle getter, TypeDef type) {
        this(name, clazz, setterMethod, getter, StableValue.of(), type);
    }

    public static @Nullable SpecField fromField(@NotNull Field field) {
        try {
            field.setAccessible(true);
            TypeDef type = extractType(field.getGenericType(), field.getAnnotations(), "Field %s in Spec %s".formatted(field.getName(), field.getDeclaringClass()));
            if(type == null) return null;

            var lookup = MethodHandles.lookup();
            MethodHandle getter = lookup.unreflectGetter(field);
            MethodHandle setter = lookup.unreflectSetter(field);

            return new SpecField(field.getName(), field.getType(), setter, getter, StableValue.of(field), type);
        } catch (Throwable t) {
            throw new RuntimeException("Error whilst turning Field into SpecField: ", t);
        }
    }

    public static @Nullable SpecField fromMethod(@NotNull Method getterMethod, @Nullable Method setter) {
        try {
            getterMethod.setAccessible(true);
            TypeDef type = extractType(getterMethod.getGenericReturnType(), getterMethod.getAnnotations(), "Method %s in Spec %s".formatted(getterMethod.getName(), getterMethod.getDeclaringClass().getName()));
            if(type == null) return null;

            var lookup = MethodHandles.lookup();

            MethodHandle setterHandle = null;
            if(setter != null) {
                // validate setterMethod parameter setType and define setterHandle
                Class<?> p1Type = setter.getParameters()[0].getType();
                if(!getterMethod.getReturnType().equals(p1Type))
                    throw new SpecFormatException("The setterMethod method " + setter.getName() + " does not take the same setType as " + getterMethod.getName() + "!");

                // setterMethod handle
                setterHandle = lookup.unreflect(setter);
            }

            MethodHandle getter = lookup.unreflect(getterMethod);


            return new SpecField(getterMethod.getName(), getterMethod.getReturnType(), setterHandle, getter, type);
        } catch (Throwable t) {
            throw new RuntimeException("Error whilst turning Method into SpecField: ", t);
        }
    }

    private static @Nullable TypeDef extractType(java.lang.reflect.Type rType, Annotation @NotNull [] annotations, String context) {
        Annotation typeAnnotation = null;
        Annotation structureAnnotation = null;
        for (Annotation annotation : annotations) {
            // check for structure
            if(Structure.isStructure(annotation)) {
                if(structureAnnotation != null) {
                    throw new SpecFormatException(context + " has multiple structures! Only one is supported."); // TODO: support multiple
                }

                structureAnnotation = annotation;
                continue;
            }

            // check for TypeDef
            if(Type.isType(annotation)) {
                if(typeAnnotation != null) {
                    throw new SpecFormatException(context + " has multiple types!");
                }
                typeAnnotation = annotation;
            } else {
                Class<? extends Annotation> annType = annotation.annotationType();
                if(annType != NotNull.class && annType != Nullable.class && annType
                        != UnknownNullability.class && annType != Contract.class &&
                        annType != Setter.class && annType != Leaker.class

                ) {
                    System.err.println("Unknown/Unhandled annotation: " + annType); // TODO: logger + debug level
                }
            }
        }


        // a structure can only be present along with a setType
        if(typeAnnotation == null && structureAnnotation != null) {
            throw new IllegalStateException("A structure was provided but no setType was provided!");
        }

        // not a member we should touch
        if(typeAnnotation == null) return null;


        // create a setType
        TypeDef type;
        if(structureAnnotation != null) {
            type = Structure.getStructure(structureAnnotation, typeAnnotation, rType, context);
        } else {
            type = Type.getTypeDef(Reflect.getClass(rType), typeAnnotation, context);

        }

        return type;
    }

    public void set(Spec spec, Object o) {
        try {
            internalField.orElseThrow().set(spec, o);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull String toString() {
        return "SpecField{" +
                "setType=" + type.getClass().getSimpleName() +
                ", clazz=" + clazz.getCanonicalName() +
                ", name='" + name + '\'' +
                '}';
    }
}
