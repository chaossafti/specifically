package de.safti.specs.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class BinaryIOTest {

    @Test
    void testReadWriteBoolean() {
        BinaryWriter writer = new BinaryWriter();
        writer.writeBoolean(true);
        writer.writeBoolean(false);
        writer.writeBoolean(true);

        BinaryData data = writer.toBinaryData();

        BinaryReader reader = new BinaryReader(data);
        Assertions.assertTrue(reader.readBoolean());
        Assertions.assertFalse(reader.readBoolean());
        Assertions.assertTrue(reader.readBoolean());

        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> reader.readBits(1));

    }

    @Test
    void testReadWriteByte() {
        BinaryWriter writer = new BinaryWriter();
        writer.writeByte((byte) 127, 8);
        writer.writeByte((byte) -128, 8);
        writer.writeByte((byte) 0, 8);
        writer.writeByte((byte) 5, 4); // 0101
        writer.writeByte((byte) -1, 4); // 1111 (-1 in 4 bits)
        BinaryData data = writer.toBinaryData();

        BinaryReader reader = new BinaryReader(data);
        Assertions.assertEquals((byte) 127, reader.readByte(8));
        Assertions.assertEquals((byte) -128, reader.readByte(8));
        Assertions.assertEquals((byte) 0, reader.readByte(8));
        Assertions.assertEquals((byte) 5, reader.readByte(4));
        Assertions.assertEquals((byte) -1, reader.readByte(4));
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> reader.readBits(1));

    }

    @Test
    void testReadWriteUByte() {
        BinaryWriter writer = new BinaryWriter();
        writer.writeUByte(255, 8);
        writer.writeUByte(0, 8);
        writer.writeUByte(10, 8);
        writer.writeUByte(7, 3); // 111
        writer.writeUByte(15, 4); // 1111
        BinaryData data = writer.toBinaryData();

        BinaryReader reader = new BinaryReader(data);
        Assertions.assertEquals(255, reader.readUByte(8));
        Assertions.assertEquals(0, reader.readUByte(8));
        Assertions.assertEquals(10, reader.readUByte(8));
        Assertions.assertEquals(7, reader.readUByte(3));
        Assertions.assertEquals(15, reader.readUByte(4));
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> reader.readBits(1));

    }

    @Test
    void testReadWriteShort() {
        BinaryWriter writer = new BinaryWriter();
        writer.writeShort((short) 32767, 16);
        writer.writeShort((short) -32768, 16);
        writer.writeShort((short) 0, 16);
        writer.writeShort((short) 100, 10); // 0001100100
        writer.writeShort((short) -5, 8); // 11111011 (-5 in 8 bits)
        BinaryData data = writer.toBinaryData();

        BinaryReader reader = new BinaryReader(data);
        Assertions.assertEquals((short) 32767, reader.readShort(16));
        Assertions.assertEquals((short) -32768, reader.readShort(16));
        Assertions.assertEquals((short) 0, reader.readShort(16));
        Assertions.assertEquals((short) 100, reader.readShort(10));
        Assertions.assertEquals((short) -5, reader.readShort(8));
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> reader.readBits(1));

    }

    @Test
    void testReadWriteUShort() {
        BinaryWriter writer = new BinaryWriter();
        writer.writeUShort(65535, 16);
        writer.writeUShort(0, 16);
        writer.writeUShort(12345, 16);
        writer.writeUShort(511, 9); // 111111111
        writer.writeUShort(255, 8); // 11111111
        BinaryData data = writer.toBinaryData();

        BinaryReader reader = new BinaryReader(data);
        Assertions.assertEquals(65535, reader.readUShort(16));
        Assertions.assertEquals(0, reader.readUShort(16));
        Assertions.assertEquals(12345, reader.readUShort(16));
        Assertions.assertEquals(511, reader.readUShort(9));
        Assertions.assertEquals(255, reader.readUShort(8));
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> reader.readBits(1));

    }

    @Test
    void testReadWriteInt() {
        BinaryWriter writer = new BinaryWriter();
        writer.writeInt(2147483647, 32);
        writer.writeInt(-2147483648, 32);
        writer.writeInt(0, 32);
        writer.writeInt(123456, 24);
        writer.writeInt(-100, 16);
        BinaryData data = writer.toBinaryData();

        BinaryReader reader = new BinaryReader(data);
        Assertions.assertEquals(2147483647, reader.readInt(32));
        Assertions.assertEquals(-2147483648, reader.readInt(32));
        Assertions.assertEquals(0, reader.readInt(32));
        Assertions.assertEquals(123456, reader.readInt(24));
        Assertions.assertEquals(-100, reader.readInt(16));
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> reader.readBits(1));

    }

    @Test
    void testReadWriteUInt() {
        BinaryWriter writer = new BinaryWriter();
        writer.writeUInteger(4_294_967_295L, 32);
        writer.writeUInteger(0L, 32);
        writer.writeUInteger(123456789L, 32);
        writer.writeUInteger(16777215L, 24); // Max for 24 bits
        writer.writeUInteger(65535L, 16); // Max for 16 bits
        BinaryData data = writer.toBinaryData();

        BinaryReader reader = new BinaryReader(data);
        Assertions.assertEquals(4_294_967_295L, reader.readUInteger(32));
        Assertions.assertEquals(0L, reader.readUInteger(32));
        Assertions.assertEquals(123456789L, reader.readUInteger(32));
        Assertions.assertEquals(16777215L, reader.readUInteger(24));
        Assertions.assertEquals(65535L, reader.readUInteger(16));
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> reader.readBits(1));
    }

    @Test
    void testReadWriteFloat() {
        BinaryWriter writer = new BinaryWriter();
        writer.writeFloat(0.0f);
        writer.writeFloat(1.0f);
        writer.writeFloat(-234.23f);
        writer.writeFloat(Float.MAX_VALUE);
        writer.writeFloat(Float.MIN_VALUE);
        writer.writeFloat(Float.NaN);
        writer.writeFloat(Float.POSITIVE_INFINITY);
        writer.writeFloat(Float.NEGATIVE_INFINITY);
        writer.writeFloat(123.456f);
        BinaryData data = writer.toBinaryData();

        BinaryReader reader = new BinaryReader(data);
        Assertions.assertEquals(0.0f, reader.readFloat());
        Assertions.assertEquals(1.0f, reader.readFloat());
        Assertions.assertEquals(-234.23f, reader.readFloat());
        Assertions.assertEquals(Float.MAX_VALUE, reader.readFloat());
        Assertions.assertEquals(Float.MIN_VALUE, reader.readFloat());
        Assertions.assertEquals(Float.NaN, reader.readFloat());
        Assertions.assertEquals(Float.POSITIVE_INFINITY, reader.readFloat());
        Assertions.assertEquals(Float.NEGATIVE_INFINITY, reader.readFloat());
        Assertions.assertEquals(123.456f, reader.readFloat());
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> reader.readBits(1));
    }

    @Test
    void testReadWriteDouble() {
        BinaryWriter writer = new BinaryWriter();
        writer.writeDouble(0.0);
        writer.writeDouble(1.0);
        writer.writeDouble(-1.0);
        writer.writeDouble(Double.MAX_VALUE);
        writer.writeDouble(Double.MIN_VALUE);
        writer.writeDouble(Double.NaN);
        writer.writeDouble(Double.POSITIVE_INFINITY);
        writer.writeDouble(Double.NEGATIVE_INFINITY);
        writer.writeDouble(12345.67890123);
        BinaryData data = writer.toBinaryData();

        BinaryReader reader = new BinaryReader(data);
        Assertions.assertEquals(0.0, reader.readDouble());
        Assertions.assertEquals(1.0, reader.readDouble());
        Assertions.assertEquals(-1.0, reader.readDouble());
        Assertions.assertEquals(Double.MAX_VALUE, reader.readDouble());
        Assertions.assertEquals(Double.MIN_VALUE, reader.readDouble());
        Assertions.assertEquals(Double.NaN, reader.readDouble());
        Assertions.assertEquals(Double.POSITIVE_INFINITY, reader.readDouble());
        Assertions.assertEquals(Double.NEGATIVE_INFINITY, reader.readDouble());
        Assertions.assertEquals(12345.67890123, reader.readDouble());
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> reader.readBits(1));
    }

}