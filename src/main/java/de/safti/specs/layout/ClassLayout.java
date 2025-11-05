package de.safti.specs.layout;

import de.safti.specs.annotations.Spec;
import de.safti.specs.io.BinaryData;
import de.safti.specs.io.BinaryReader;
import de.safti.specs.io.BinaryWriter;
import de.safti.specs.layout.common.SpecField;
import de.safti.specs.layout.common.TypeDef;
import de.safti.specs.utils.Reflect;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClassLayout implements SpecLayout {

    protected final Class<? extends Spec> specClass;
    protected final MethodHandle constructorHandle;

    protected final SpecField[] fields;

    public ClassLayout(Class<? extends Spec> specClass) {
        this.specClass = specClass;

        try {
            constructorHandle = MethodHandles.lookup()
                    .findConstructor(specClass, MethodType.methodType(void.class));

            Field[] fields = Reflect.getDeclaredFieldsInOrder(specClass);
            List<SpecField> list = new ArrayList<>();

            // populate spec fields
            for (Field field : fields) {
                field.setAccessible(true);
                SpecField specField = SpecField.fromField(field);
                if(specField != null) list.add(specField);
            }

            this.fields = list.toArray(SpecField[]::new);


        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No noarg constructor is provided in spec class " + specClass.getCanonicalName(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public ClassLayout(Class<? extends Spec> specClass, MethodHandle constructorHandle, SpecField[] fields) {
        this.specClass = specClass;
        this.constructorHandle = constructorHandle;
        this.fields = fields;

    }

    @Override
    public void write(Spec spec, BinaryWriter writer) {

        for (SpecField field : fields) {
            TypeDef type = field.type();
            try {
                Object o = field.getter().invoke(spec);
                type.write(writer, o);


            } catch (Throwable e) {
                throw new RuntimeException("Failed to write field " + field.name(), e);
            }
        }



    }

    @Override
    public @Nullable SpecField getField(String name) {
        for (SpecField specField : fields) {
            if(specField.name().equals(name)) {
                return specField;
            }
        }
        return null;
    }

    @Override
    public SpecField[] getFields() {
        return fields;
    }

    @Override
    public Spec create(BinaryData data) {
        Spec instance = createInstance();

        BinaryReader reader = new BinaryReader(data);

        SpecContext context = new SpecContext(instance, this);

        // populate the Spec fields
        for (SpecField field : fields) {
            try {
                field.set(instance, field.type().read(reader, context));
            } catch (Throwable e) {
                throw new RuntimeException("Exception whilst populating field " + field.name() + " from raw data", e);
            }
        }

        if(reader.hasMore()) throw new IllegalStateException("More data is found! It is likely that the wrong spec was provided for reading, or data corruption.");

        return instance;
    }

    @Override
    public Spec createInstance() {
        try {
            return (Spec) constructorHandle.invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<? extends Spec> getSpecClass() {
        return specClass;
    }

    @Override
    public String toString() {
        // ANSI color codes
        final String RESET = "\u001B[0m";
        final String CYAN = "\u001B[36m";
        final String YELLOW = "\u001B[33m";
        final String GREEN = "\u001B[32m";

        String fieldString = Arrays.stream(fields)
                .map(f -> "    " + CYAN + f.name() + RESET + ": " + YELLOW + f.clazz().getCanonicalName() + RESET)
                .collect(Collectors.joining("\n"));

        return GREEN + "ClassLayout {" + RESET + "\n" +
                "  specClass: " + CYAN + specClass.getCanonicalName() + RESET + "\n" +
                GREEN + "  fields" + RESET + ":\n" + fieldString + "\n" +
                GREEN + "}" + RESET;
    }


}
