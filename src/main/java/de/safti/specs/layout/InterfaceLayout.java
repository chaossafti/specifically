package de.safti.specs.layout;

import de.safti.specs.SpecFormatException;
import de.safti.specs.annotations.Field;
import de.safti.specs.annotations.InterfaceSpecImpl;
import de.safti.specs.annotations.Spec;
import de.safti.specs.layout.common.SpecField;
import de.safti.specs.utils.Checkers;
import de.safti.specs.utils.GenericTypeResolver;
import de.safti.specs.utils.Reflect;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.jar.asm.Opcodes;

import java.lang.reflect.*;
import java.util.*;

public class InterfaceLayout extends ClassLayout {
    private final Map<String, Method> setterMethods;
    private final Map<String, Method> leakerMethods;
    private Class<? extends Spec> generatedClass;

    public InterfaceLayout(Class<? extends Spec> specClass) {
        Method[] methods = Reflect.getDeclaredMethodsInOrder(specClass);

        Map<String, Method> setterMethods = new HashMap<>();
        Map<String, Method> leakerMethods = new HashMap<>();

        for (Method method : methods) {
            if(method.isAnnotationPresent(Field.Setter.class)) {
                Field.Setter annotation = method.getAnnotation(Field.Setter.class);

                // validate method signature
                if(method.getReturnType() != void.class) throw new SpecFormatException("A interface spec setterMethod method must return void! Method: " + method.getName() + " in spec " + specClass.getCanonicalName());
                if(method.getParameterCount() != 1) throw new SpecFormatException("A interface spec setterMethod method must have one parameter! Method: " + method.getName() + " in spec " + specClass.getCanonicalName());
                // we cannot validate parameter setType as the SpecField is not defined yet

                setterMethods.put(annotation.value(), method);

            } else if(method.isAnnotationPresent(Field.Leaker.class)) {
                Field.Leaker annotation = method.getAnnotation(Field.Leaker.class);


                // validate method signature

                boolean returnsSpecField = method.getReturnType() == SpecField.class;
                boolean returnsSpecFieldArray = annotation.value().equals("@all") && method.getReturnType() == SpecField[].class;
                if(!returnsSpecField && !returnsSpecFieldArray) throw new SpecFormatException("A interface spec leaker method must return SpecField! Method: " + method.getName() + " in spec " + specClass.getCanonicalName());
                if(method.getParameterCount() != 0) throw new SpecFormatException("A interface spec leaker method must have no parameter! Method: " + method.getName() + " in spec " + specClass.getCanonicalName());


                leakerMethods.put(annotation.value(), method);
            }
        }

        // populate fields
        List<SpecField> list = new ArrayList<>();
        for (Method method : methods) {
            SpecField specField = SpecField.fromMethod(method, setterMethods.get(method.getName()));
            if(specField != null) {
                list.add(specField);
            }
        }

        SpecField[] fields = list.toArray(SpecField[]::new);
        super(specClass, null, fields);

        this.leakerMethods = leakerMethods;
        this.setterMethods = setterMethods;
    }

    @Override
    public Spec createInstance() {
        if(generatedClass == null) generateClass();

        try {
            return generatedClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private void generateClass() {
        ByteBuddy byteBuddy = new ByteBuddy();

        AnnotationDescription annotation = AnnotationDescription.Builder.ofType(InterfaceSpecImpl.class)
                .define("specClass", specClass)
                .build();

        var builder = byteBuddy.subclass(specClass)
                .annotateType(annotation);

        // define getter methods
        for (SpecField field : fields) {
            builder = builder
                    // field for holding the array
                    .defineField(field.name(), field.clazz(), Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC)
                    // method for getting the array
                    .defineMethod(field.name(), field.clazz(), Opcodes.ACC_PUBLIC)
                    .intercept(FieldAccessor.ofField(field.name()));
        }

        // define setterMethod methods
        for (Map.Entry<String, Method> entry : setterMethods.entrySet()) {
            String fieldName = entry.getKey();
            Method setterMethod = entry.getValue();
            SpecField field = getFieldByNameOrThrow(fieldName, setterMethod.getName() + " setterMethod method");

            builder = builder
                    .defineMethod(setterMethod.getName(), void.class, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC)
                    .withParameters(field.clazz())
                    .intercept(FieldAccessor.ofField(fieldName));


        }

        // define leaker methods
        for (Map.Entry<String, Method> entry : leakerMethods.entrySet()) {
            String fieldName = entry.getKey();
            Method leakerMethod = entry.getValue();

            // see if @all is used
            if(fieldName.equals("@all")) {
                builder = builder
                        .defineMethod(leakerMethod.getName(), SpecField[].class, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC)
                        .intercept(FixedValue.value(fields));
                continue;
            }


            // define normal leaker method
            SpecField field = getFieldByNameOrThrow(fieldName, leakerMethod.getName() + " leaker method");
            builder = builder
                    .defineMethod(leakerMethod.getName(), SpecField.class, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC)
                    .intercept(FixedValue.value(field));
        }


        // equals, hashcode and toString
        SpecObjectMethods methods = new SpecObjectMethods();
        builder = builder
                // equals
                .defineMethod("equals", boolean.class, Modifier.PUBLIC)
                    .withParameters(Object.class)
                    .intercept(MethodDelegation.to(methods))

                // hashcode
                .defineMethod("hashCode", int.class, Modifier.PUBLIC)
                    .intercept(MethodDelegation.to(methods))

                // toString
                .defineMethod("toString", String.class, Modifier.PUBLIC)
                    .intercept(MethodDelegation.to(methods));


        generatedClass = builder
                .make()
                .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                .getLoaded();


        // initialize internalField of SpecField
        for (SpecField field : fields) {
            try {
                field.internalField().setOrThrow(generatedClass.getDeclaredField(field.name()));
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // TODO
    private boolean canStore(Type target, Class<?> given) {
        Class<?> targetClass = Reflect.getClass(target);

        if(targetClass == given) return true;
        if(targetClass == Optional.class) return GenericTypeResolver.findGenericTypeArguments(targetClass, Optional.class)[0] == given;
        if(targetClass == OptionalInt.class) return given == int.class;
        if(targetClass == OptionalLong.class) return given == long.class;
        if(targetClass == OptionalDouble.class) return given == double.class;

        return false;
    }

    private SpecField getFieldByNameOrThrow(String name, String reference) {
        return Arrays.stream(fields)
                .filter(specField -> specField.name().equals(name))
                .findFirst().orElseThrow(() -> new SpecFormatException("Field with name " + name + " not found, referenced at: " + reference));
    }

    public class SpecObjectMethods {

        @RuntimeType
        public boolean equals(@This Object self, @Argument(0) Object other) {
            if (self == other) return true;
            if (other == null) return false;

            // the first (and only) interface is guaranteed to be the user defined spec interface
            Class<?> iface = self.getClass().getInterfaces()[0];
            if (iface != other.getClass().getInterfaces()[0]) return false;

            try {
                for (SpecField field : InterfaceLayout.this.fields) {
                    Object thisValue = field.getter().invoke(self);
                    Object otherValue = field.getter().invoke(other);

                    if(thisValue == null) return otherValue == null;
                    if (Checkers.unequal(thisValue, otherValue)) return false;
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }

            return true;
        }

        @RuntimeType
        public int hashCode(@This Object self) {
            try {
                List<Object> values = new ArrayList<>();
                for (SpecField field : InterfaceLayout.this.fields) {
                    values.add(field.getter().invoke(self));
                }
                return Objects.hash(values.toArray());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        @RuntimeType
        public String toString(@This Object self) {
            StringBuilder sb = new StringBuilder(self.getClass().getInterfaces()[0].getSimpleName());
            sb.append('{');
            try {
                boolean first = true;
                for (SpecField field : InterfaceLayout.this.fields) {
                    if (!first) sb.append(", ");
                    first = false;
                    Object value = field.getter().invoke(self);
                    sb.append(field.name()).append('=').append(value);
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            sb.append('}');
            return sb.toString();
        }


    }


}
