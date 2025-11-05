package de.safti.specs.annotations;

import de.safti.specs.io.BinaryReader;
import de.safti.specs.io.BinaryWriter;
import de.safti.specs.layout.SpecContext;
import de.safti.specs.layout.common.StructureDef;
import de.safti.specs.layout.common.TypeDef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;

public interface StructureDefs {

    record ArrayFixedDef(TypeDef inner, Class<?> componentClass, int[] dimensions) implements StructureDef {

        @Override
        public Object read(BinaryReader reader, SpecContext context) {
            return readArray(reader, context, 0);
        }

        private Object readArray(BinaryReader reader, SpecContext context, int depth) {
            int length = dimensions[depth];

            Class<?> elementType;
            if (depth == dimensions.length - 1) {
                elementType = componentClass; // e.g. int.class or String.class
            } else {
                // Build an array class of the base component with (remainingDepth) dims.
                int remaining = dimensions.length - depth - 1;
                elementType = Array.newInstance(componentClass, new int[remaining]).getClass();
            }

            Object array = Array.newInstance(elementType, length);

            if (depth == dimensions.length - 1) {
                for (int i = 0; i < length; i++) {
                    Object value = inner.read(reader, context);
                    Array.set(array, i, value);
                }
            } else {
                for (int i = 0; i < length; i++) {
                    Object nested = readArray(reader, context, depth + 1);
                    Array.set(array, i, nested);
                }
            }

            return array;
        }

        @Override
        public void write(BinaryWriter writer, Object o) {
            if (!o.getClass().isArray()) {
                throw new IllegalArgumentException("Expected array but got " + o.getClass());
            }
            writeArray(writer, o, 0);
        }

        private void writeArray(BinaryWriter writer, Object array, int depth) {
            int length = dimensions[depth];

            if (Array.getLength(array) != length) {
                throw new IllegalArgumentException("Array dimension mismatch at depth " + depth + ": expected " + length + " but got " + Array.getLength(array));
            }

            if (depth == dimensions.length - 1) {
                // innermost array
                for (int i = 0; i < length; i++) {
                    Object element = Array.get(array, i);
                    inner.write(writer, element);
                }
            } else {
                // nested arrays
                for (int i = 0; i < length; i++) {
                    writeArray(writer, Array.get(array, i), depth + 1);
                }
            }
        }

        @Override
        public @NotNull Object createDefault() {
            return Array.newInstance(componentClass, dimensions);
        }
    }


    record ArrayDynamicDef(TypeDef inner, Class<?> componentClass,
                           String lengthField, int bits,
                           boolean unsigned) implements StructureDef {

        @Override
        public Object read(BinaryReader reader, SpecContext context) {
            int length = readLength(reader, context);
            Object array = Array.newInstance(componentClass, length);

            // populate the array
            for (int i = 0; i < length; i++) {
                Array.set(array, i, inner.read(reader, context));
            }

            return array;
        }

        private int readLength(BinaryReader reader, SpecContext context) {
            if(lengthField.equals("@auto")) return Math.toIntExact(unsigned ? reader.readUInt(bits) : reader.readInt(bits));

            Object o = context.getFieldValue(lengthField);
            if(o == null) throw new IllegalStateException("Length field " + lengthField + " not found or not yet initialized.");
            return (int) o;

        }

        @Override
        public void write(BinaryWriter writer, Object o) {
            Class<?> type = o.getClass();
            if (!type.isArray()) {
                throw new IllegalArgumentException("Expected array but got " + type);
            }

            // write dimensions
            int actualLength = Array.getLength(o);
            if(lengthField.equals("@auto")) {
                if(unsigned) {
                    writer.writeUInt(actualLength, bits);
                } else {
                    writer.writeInt(actualLength, bits);
                }
            }

            // write elements
            for (int i = 0; i < actualLength; i++) {
                Object element = Array.get(o, i);
                inner.write(writer, element);
            }
        }
    }

    record ListFixedDef<T>(TypeDef inner, Class<T> componentClass, int length, ListType type) implements StructureDef {

        @Override
        public Object read(BinaryReader reader, SpecContext context) {
            List<T> result = type.create(length);

            for (int i = 0; i < length; i++) {
                result.add((T) inner.read(reader, context));
            }

            return result;
        }

        @Override
        public void write(BinaryWriter writer, Object o) {
            List<?> list = (List<?>) o;
            if(list == null) throw new IllegalArgumentException("List is null");
            if(list.size() != length) throw new IllegalArgumentException("Expected list size " + length + " but got " + list.size());

            for (Object element : list) {
                inner.write(writer, element);
            }

        }
    }


    record ListDynamicDef<T>(TypeDef inner, Class<T> componentClass,
                             String lengthField, int bitSize,
                             boolean useUnsigned, ListType type) implements StructureDef {

        @Override
        public Object read(BinaryReader reader, SpecContext context) {
            // read dimensions and create list
            int length = readLength(reader, context);
            List<T> result = type.create(length);

            // read elements
            for (int i = 0; i < length; i++) {
                result.add((T) inner.read(reader, context));
            }

            return result;
        }

        @Override
        public void write(BinaryWriter writer, Object o) {
            List<?> list = (List<?>) o;
            // populate @auto dimensions field if available
            if(lengthField.equals("@auto")) {
                if(useUnsigned) writer.writeUInt(list.size(), bitSize);
                else writer.writeInt(list.size(), bitSize);
            }

            // write elements
            for (Object element : list) {
                inner.write(writer, element);
            }

        }

        public int readLength(BinaryReader reader, SpecContext context) {
            if(lengthField.equals("@auto")) return Math.toIntExact(useUnsigned ? reader.readUInt(bitSize) : reader.readInt(bitSize));
            Object o = context.getFieldValue(lengthField);
            if(o == null) throw new IllegalArgumentException("Length field " + lengthField + " not found or not yet initialized");
            return (int) o;
        }

    }

    record SetFixedDef<T>(TypeDef inner, Class<T> componentClass,
                          int length, SetType setType) implements StructureDef {


        @Override
        public Object read(BinaryReader reader, SpecContext context) {
            Set<T> set = setType.create(length);
            for (int i = 0; i < length; i++) {
                set.add((T) inner.read(reader, context));
            }

            return set;
        }

        @Override
        public void write(BinaryWriter writer, Object o) {
            Set<T> set = (Set<T>) o;
            if(set == null) throw new IllegalArgumentException("Set is null");
            if(set.size() != length) throw new IllegalArgumentException("Expected set size " + length + " but got " + set.size() + "!");

            for (T element : set) {
                inner.write(writer, element);
            }
        }
    }

    record SetDynamicDef<T>(TypeDef inner, Class<T> componentClass,
                             String lengthField, int bitSize,
                             boolean useUnsigned, SetType type) implements StructureDef {

        @Override
        public Object read(BinaryReader reader, SpecContext context) {
            // read dimensions and create set
            int length = readLength(reader, context);
            Set<T> result = type.isUnmodifiable() ? new HashSet<>(length) : type.create(length);

            // read elements
            for (int i = 0; i < length; i++) {
                result.add((T) inner.read(reader, context));
            }


            if(type.isUnmodifiable()) result = type.copy(result);
            return result;
        }

        @Override
        public void write(BinaryWriter writer, Object o) {
            Set<?> list = (Set<?>) o;
            // populate @auto dimensions field if available
            if(lengthField.equals("@auto")) {
                if(useUnsigned) writer.writeUInt(list.size(), bitSize);
                else writer.writeInt(list.size(), bitSize);
            }

            // write elements
            for (Object element : list) {
                inner.write(writer, element);
            }

        }

        public int readLength(BinaryReader reader, SpecContext context) {
            if(lengthField.equals("@auto")) return Math.toIntExact(useUnsigned ? reader.readUInt(bitSize) : reader.readInt(bitSize));
            Object o = context.getFieldValue(lengthField);
            if(o == null) throw new IllegalArgumentException("Length field " + lengthField + " not found or not yet initialized");
            return (int) o;
        }

    }

    record OptionalDef<T>(TypeDef inner, Class<T> rType) implements StructureDef {

        @Override
        public Object read(BinaryReader reader, SpecContext context) {
            if(reader.readBoolean()) {
                // primitive specific checks
                if(rType == OptionalInt.class) return OptionalInt.of(reader.readInt());
                if(rType == OptionalLong.class) return OptionalLong.of(reader.readLong());
                if(rType == OptionalDouble.class) return OptionalDouble.of(reader.readDouble());

                if(rType == Optional.class) return Optional.of(inner.read(reader, context));
                return inner.read(reader, context);
            }


            // no value present
            return createDefault();
        }

        @Override
        public void write(BinaryWriter writer, Object o) {
            // not quite sure how to apply DRY here
            // since there is no common superclass / superinterface
            if(o instanceof Optional<?> opt) {
                writer.writeBoolean(opt.isPresent());
                opt.ifPresent(object -> inner.write(writer, object));
                return;
            }
            if(o instanceof OptionalInt opt) {
                writer.writeBoolean(opt.isPresent());
                opt.ifPresent(value -> inner.write(writer, value));
                return;
            }
            if(o instanceof OptionalLong opt) {
                writer.writeBoolean(opt.isPresent());
                opt.ifPresent(object -> inner.write(writer, object));
                return;
            }
            if(o instanceof OptionalDouble opt) {
                writer.writeBoolean(opt.isPresent());
                opt.ifPresent(object -> inner.write(writer, object));
                return;
            }

            writer.writeBoolean(o != null);
            if(o != null) inner.write(writer, o);
        }

        @Override
        public @Nullable Object createDefault() {
            if(rType == Optional.class) return Optional.empty();
            if(rType == OptionalInt.class) return OptionalInt.empty();
            if(rType == OptionalLong.class) return OptionalLong.empty();
            if(rType == OptionalDouble.class) return OptionalDouble.empty();
            return inner.createDefault();
        }
    }

}
