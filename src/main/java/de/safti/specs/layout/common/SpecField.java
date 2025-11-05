package de.safti.specs.layout;

import de.safti.specs.annotations.Type;
import de.safti.specs.layout.common.TypeDef;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;

public record SpecField(MethodHandle setter, MethodHandle getter, TypeDef type) {

    public SpecField(@NotNull Field field) throws IllegalAccessException {
        field.setAccessible(true);


        // lookup TypeDef
        Annotation[] annotations = field.getAnnotations();
        TypeDef type = null;
        for (Annotation annotation : annotations) {
            TypeDef parsed = Type.getTypeDef(annotation);
            if(parsed != null) {
                if(type != null) throw new IllegalStateException("Multiple types have been assigned to the " + field.getName() + " field!");
                type = parsed;
            }

            // TODO: handling for other metadata annotations

        }

        if(type == null) throw new IllegalStateException("No type was found for field: %s, annotations: %s".formatted(field.getName(), Arrays.toString(annotations)));

        // lookup getter/setter
        var lookup = MethodHandles.lookup();
        MethodHandle getter = lookup.unreflectGetter(field);
        MethodHandle setter = lookup.unreflectSetter(field);

        this(setter, getter, type);
    }
}
