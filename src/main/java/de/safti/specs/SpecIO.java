package de.safti.specs;

import de.safti.specs.annotations.InterfaceSpecImpl;
import de.safti.specs.annotations.Spec;
import de.safti.specs.io.BinaryData;
import de.safti.specs.io.BinaryWriter;
import de.safti.specs.layout.ClassLayout;
import de.safti.specs.layout.InterfaceLayout;
import de.safti.specs.layout.SpecLayout;
import de.safti.specs.layout.common.SpecField;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class SpecIO {
    private static final Map<Class<?>, SpecLayout> LAYOUTS = new HashMap<>();

    @Contract(pure = true)
    public static BinaryData write(@NotNull Spec spec) {
        // get layout
        Class<? extends Spec> clazz = spec.getClass();
        SpecLayout specLayout = getLayout(clazz);

        // write fields
        BinaryWriter writer = new BinaryWriter();
        specLayout.write(spec, writer);

        // return
        return writer.toBinaryData();
    }

    @Contract(pure = true)
    public static Spec read(BinaryData data, @NotNull Class<? extends Spec> clazz) {
        SpecLayout specLayout = getLayout(clazz);
        return specLayout.create(data);
    }

    /**
     * In the case of a class spec, an instance is created using reflection.
     * In the case of an interface, a class is generated at runtime (once per spec) and instantiated.
     * No default values are supplied.
     *
     * @param clazz The Spec class/interface to generate
     * @return A newly generated Spec.
     * @see #generate(Class)
     */
    @Contract(pure = true)
    public static <T extends Spec> T generateEmpty(Class<T> clazz) {
        SpecLayout specLayout = getLayout(clazz);
        return (T) specLayout.createInstance();
    }


    /**
     * Similar to {@link #generateEmpty(Class)} but this method also populates default values.
     * see {@link #generateEmpty(Class)} for more information.
     *
     * @param clazz The Spec class/interface to generate
     * @return A newly generated Spec.
     * @see #generateEmpty(Class)
     */
    @Contract(pure = true)
    public static <T extends Spec> T generate(Class<T> clazz) {
        SpecLayout layout = getLayout(clazz);
        T spec = (T) layout.createInstance();

        SpecField[] fields = layout.getFields();
        for (SpecField field : fields) {
            try {
                field.set(spec, field.type().createDefault());
            } catch (Throwable e) {
                throw new RuntimeException("Failed to populate field " + field.name() + " with default value", e);
            }
        }

        return spec;
    }

    public static SpecLayout getLayout(Spec spec) {
        if(Proxy.isProxyClass(spec.getClass())) {
            return getLayout((Class<? extends Spec>) spec.getClass().getInterfaces()[0]);
        }
        return getLayout(spec.getClass());
    }


    @NotNull
    public static SpecLayout getLayout(Class<? extends Spec> clazz) {
        // the byte code generated class might get passed into this function
        // we can fix that by resolving the actual spec class
        if(clazz.isAnnotationPresent(InterfaceSpecImpl.class)) {
            InterfaceSpecImpl annotation = clazz.getAnnotation(InterfaceSpecImpl.class);
            clazz = annotation.specClass();
        }


        if(LAYOUTS.containsKey(clazz))
            return LAYOUTS.get(clazz);

        if(clazz.isInterface()) {
            InterfaceLayout layout = new InterfaceLayout(clazz);
            LAYOUTS.put(clazz, layout);
            return layout;
        }

        // is a regular class
        if(!clazz.isArray() && !clazz.isEnum() && !clazz.isPrimitive() && !clazz.isRecord()) {
            ClassLayout layout = new ClassLayout(clazz);
            LAYOUTS.put(clazz, layout);
            return layout;
        }

        throw new IllegalStateException(clazz.getCanonicalName() + " is a class that is not supported! Supported class types for layouts are interfaces and regular classes.");
    }

}
