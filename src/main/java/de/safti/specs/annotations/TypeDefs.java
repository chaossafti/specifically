package de.safti.specs.annotations;

import de.safti.specs.SpecFormatException;
import de.safti.specs.io.BinaryReader;
import de.safti.specs.io.BinaryWriter;
import de.safti.specs.layout.SpecContext;
import de.safti.specs.layout.common.TypeDef;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

interface TypeDefs {

    /**
     * Handles signed integers of a specific bit width
     * read object is a {@link Number}.
     */
    class IntDef implements TypeDef {
        private final int bits;
        private final Class<?> numberClass;

        IntDef(int bits, Class<?> numberClass) {
            this.bits = bits;
            this.numberClass = numberClass;
        }

        @Override
        public Object read(BinaryReader reader, SpecContext context) {
            if(bits > 63) {
                if(numberClass != BigInteger.class) throw new ArithmeticException(numberClass.getCanonicalName() + " too small to number!");
                return reader.readBigInteger(bits);
            }

            return intAsType(numberClass, reader.readSignedBits(bits));
        }

        @Override
        public void write(BinaryWriter writer, Object o) {
            if(o == null) o = 0;

            switch (numberClass.getName()) {
                case "byte", "java.lang.Byte" -> writer.writeByte((byte) o, bits);
                case "short", "java.lang.Short" -> writer.writeShort((short) o, bits);
                case "int", "java.lang.Integer" -> writer.writeInt((int) o, bits);
                case "long", "java.lang.Long" -> writer.writeLong((long) o, bits);
                case "java.math.BigInteger" -> writer.writeBigInteger((BigInteger) o, bits);
                default -> throw new SpecFormatException(
                        "Unknown integer class: " + numberClass + ". Supported types: byte, short, int, long, BigInteger."
                );
            }
        }
    }

    /**
     * Handles signed VarInts of LEB128 standard
     */
    class VarIntDef implements TypeDef {
        private final Class<?> rType;

        public VarIntDef(Class<?> rType) {
            this.rType = rType;
        }


        @Override
        public Object read(BinaryReader reader, SpecContext context) {
            long value = reader.readVarInt();
            return intAsType(rType, value);
        }

        @Override
        public void write(BinaryWriter writer, Object o) {
            long value = (long) o;
            writer.writeVarInt(value);
        }

        private Object throwTypeTooSmall(long value) {
            throw new ArithmeticException(rType.getCanonicalName() + " is too small to store varint (" + value + ")");
        }

    }

    /**
     * Handles unsigned VarInts of LEB128 standard
     */
    class UVarIntDef implements TypeDef {
        private final Class<?> rType;

        public UVarIntDef(Class<?> rType) {
            this.rType = rType;
        }


        @Override
        public Object read(BinaryReader reader, SpecContext context) {
            long value = reader.readUVarInt();
            return intAsType(rType, value);
        }

        @Override
        public void write(BinaryWriter writer, Object o) {
            writer.writeUVarInt((long) o);
        }



    }

    private static Object intAsType(Class<?> rType, long value) {
        if(rType == Byte.class || rType == byte.class) return value > Byte.MAX_VALUE ? throwTypeTooSmall(rType.getCanonicalName(), value) : (byte) value;
        if(rType == Short.class || rType == short.class) return value > Short.MAX_VALUE ? throwTypeTooSmall(rType.getCanonicalName(), value) : (short) value;
        if(rType == Integer.class || rType == int.class) return value > Integer.MAX_VALUE ? throwTypeTooSmall(rType.getCanonicalName(), value) : (int) value;
        if(rType == Long.class || rType == long.class) return value;
        if(rType == BigInteger.class) return BigInteger.valueOf(value);

        throw new ArithmeticException("Unknown/Unsupported integer class: " + rType + ". Supported types include byte, short, int, long and BigInteger.");
    }

    private static Object throwTypeTooSmall(String typeName, long value) {
        throw new ArithmeticException(typeName + " is too small to store varint (" + value + ")");
    }

    /**
     * Handles standard 32-bit single-precision floats.
     * Popped object is a {@link Float}.
     */
    class FloatDef implements TypeDef {
        static final FloatDef INSTANCE = new FloatDef();

        private FloatDef() {
        }

        @Override
        public Object read(BinaryReader reader, SpecContext context) {
            return reader.readFloat();
        }

        @Override
        public void write(BinaryWriter writer, Object o) {
            writer.writeFloat((float) o);
        }
    }

    /**
     * Handles standard 64-bit double-precision doubles.
     * Popped object is a {@link Double}.
     */
    class DoubleDef implements TypeDef {
        static final DoubleDef INSTANCE = new DoubleDef();

        private DoubleDef() {
        }

        @Override
        public Object read(BinaryReader reader, SpecContext context) {
            return reader.readDouble();
        }

        @Override
        public void write(BinaryWriter writer, Object o) {
            writer.writeDouble((Double) o);
        }
    }

    /**
     * Handles null-terminated strings.
     * Popped object is a {@link String}. Only supports ascii strings.
     */
    class StringTerminatedDef implements TypeDef {
        private final byte terminator;

        StringTerminatedDef(char terminator) {
            if (terminator > 127) throw new IllegalArgumentException("Terminator must be ASCII.");
            this.terminator = (byte) terminator;
        }

        @Override
        public Object read(BinaryReader reader, SpecContext context) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while(reader.hasMore()) {
                byte b = reader.readByte();
                if(b == terminator) break;
                baos.write(b);
            }
            return baos.toString(StandardCharsets.UTF_8);
        }

        @Override
        public void write(BinaryWriter writer, Object o) {
            String s = (String) o;
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            for (byte b : bytes) {
                if(b == terminator) throw new IllegalArgumentException(
                        "String contains the terminator byte; cannot write."
                );
                writer.writeByte(b);
            }
            writer.writeByte(terminator);
        }
    }

    /**
     * Handles strings prefixed by their dimensions.
     * Popped object is a {@link String}.
     */
    class StringDynamicDef implements TypeDef {
        private final String fieldName;
        private final int autoBitSize;
        private final boolean useUnsigned;

        StringDynamicDef(String fieldName, int autoBitSize, boolean useUnsigned) {
            if(autoBitSize < 0) throw new IllegalArgumentException("Auto bit size cannot be negative.");

            this.fieldName = fieldName;
            this.autoBitSize = autoBitSize;
            this.useUnsigned = useUnsigned;
        }

        @Override
        public Object read(BinaryReader reader, SpecContext context) {
            int length = getStringLength(reader, context);

            byte[] bytes = new byte[length];
            for (int i = 0; i < length; i++) {
                bytes[i] = reader.readByte();
            }
            return new String(bytes, StandardCharsets.UTF_8);
        }

        private int getStringLength(BinaryReader reader, SpecContext context) {
            if(fieldName.equals("@auto")) {
                return Math.toIntExact(reader.readUInt(autoBitSize));
            } else {
                Object object = context.getFieldValue(fieldName);
                if(object == null) throw new IllegalStateException("Field " + fieldName + " is not found or not initialized");
                if(!(object instanceof Number number)) throw new IllegalArgumentException("Field " + fieldName + " is not a number field");
                return number.intValue();
            }
        }

        @Override
        public void write(BinaryWriter writer, Object o) {
            String s = (String) o;

            // write dimensions if we're using @auto
            if(fieldName.equals("@auto")) {
                if(useUnsigned) writer.writeUInt(s.length(), autoBitSize);
                else writer.writeInt(s.length(), autoBitSize);

                long maxLength = useUnsigned ?
                        (1L << autoBitSize) - 1 : // unsigned calculation: (2^bits) - 1
                        (1L << (autoBitSize - 1)) - 1; // signed calculation: 2^(bits-1) - 1
                // if autoBitSize > 64 maxLength will have overflown; We expect that the string is not longer than Long.MAX_VALUE
                if(autoBitSize < 64 && s.length() > maxLength) throw new ArithmeticException("String longer than dimensions field bit count can represent. Length: %s, bit size: %s (Max number: %s)".formatted(s.length(), autoBitSize, s.length()));
            }

            // write bytes
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            for (byte b : bytes) {
                writer.writeByte(b);
            }
        }
    }

    /**
     * Handles fixed-dimensions strings, supporting truncation and null-padding.
     * Popped object is a {@link String}.
     */
    class StringFixedDef implements TypeDef {
        private final int fixedLength;
        private final boolean slice;
        private final boolean autoResize;
        private final boolean cutPadding;

        StringFixedDef(int fixedLength, boolean slice, boolean autoResize, boolean cutPadding) {
            this.fixedLength = fixedLength;
            this.slice = slice;
            this.autoResize = autoResize;
            this.cutPadding = cutPadding;
        }

        @Override
        public Object read(BinaryReader reader, SpecContext context) {
            byte[] bytes = new byte[this.fixedLength];
            for (int i = 0; i < this.fixedLength; i++) {
                bytes[i] = reader.readByte();
            }

            int len = this.fixedLength;

            if (cutPadding) {
                // remove trailing \0 chars (padded chars)
                while (len > 0 && bytes[len - 1] == 0) {
                    len--;
                }
            }

            return new String(bytes, 0, len, StandardCharsets.UTF_8);
        }

        @Override
        public void write(BinaryWriter writer, Object o) {
            String s = normalizeStringLength((String) o);

            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);

            // write bytes
            for (int i = 0; i < bytes.length && i < this.fixedLength; i++) {
                writer.writeByte(bytes[i]);
            }
        }

        private @NotNull String normalizeStringLength(String o) {
            String s = o;
            if(s.length() > fixedLength) {
                if(!slice) throw new IllegalArgumentException("The given string is larger than the fixed size! Enable slicing to automatically cut the string, or provide a string of dimensions " + fixedLength);
                s = s.substring(0, fixedLength);
            }
            if(s.length() < fixedLength) {
                if(!autoResize) throw new IllegalArgumentException("The given string is smaller than the fixed size! Enable auto resize to automatically pad the string with \\0 chars, or provide a string of dimensions " + fixedLength);
                s = s + "\0".repeat(fixedLength - s.length());
            }
            return s;
        }
    }

    record EnumTypeDef<E extends Enum<E>>(Class<? extends Enum<?>> enumClass, int bits) implements TypeDef {

        public EnumTypeDef(Class<? extends Enum<?>> enumClass, int bits) {
            this.enumClass = enumClass;
            this.bits = bits == -1 ? minBits() : bits;
            if(minBits() < bits) throw new IllegalArgumentException("Cannot store all combinations of enum constants! Given bit count %d is too low to store all constants. Must be a minimum of %d".formatted(bits, minBits()));
        }

        private int minBits() {
            return 32 - Integer.numberOfLeadingZeros(enumClass.getEnumConstants().length);
        }

        @Override
        public Object read(BinaryReader reader, SpecContext context) {
            int ordinal = Math.toIntExact(reader.readUInt(bits));
            return enumClass.getEnumConstants()[ordinal];
        }

        @Override
        public void write(BinaryWriter writer, Object o) {
            if(!(o instanceof Enum<?> enumConstant)) throw new IllegalArgumentException("Expected enum but got " + o.getClass());
            if(o.getClass() != enumClass) throw new IllegalArgumentException("Enum mismatch! Expected %s but got %s".formatted(enumConstant, o.getClass()));

            writer.writeUInt(enumConstant.ordinal(), bits);
        }
    }



}