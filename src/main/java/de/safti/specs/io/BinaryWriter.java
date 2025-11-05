package de.safti.specs.io;

import java.math.BigDecimal;
import java.math.BigInteger;

import java.io.*;

/**
 * A bit-level output writer that allows writing primitive types and arbitrary
 * bit amounts to an underlying dynamic byte array.
 * This class uses a custom runtime exception.
 */
public class BinaryWriter {

    // closing is not required as ByteArrayOutputStream is noop.
    private final ByteArrayOutputStream byteOut;
    private int buffer;       // Internal buffer for bits (up to 8 bits)
    private int bitsInBuffer; // Number of bits currently in the buffer (0-7)

    /**
     * Creates a BitWriter. The written bytes are stored internally and
     * can be retrieved using the toByteArray() method.
     */
    public BinaryWriter() {
        this.byteOut = new ByteArrayOutputStream();
        this.buffer = 0;
        this.bitsInBuffer = 0;
    }

    /**
     * Writes the least significant 'numBits' of 'value' to the stream.
     * This is the core method for all writing operations.
     *
     * @param value The long value containing the bits to write.
     * @param numBits The number of bits to write (1 to 64).
     * @throws BitStreamWriteException If an underlying I/O error occurs.
     */
    public void writeBits(long value, int numBits) {
        if (numBits < 1 || numBits > 64) {
            throw new IllegalArgumentException("Number of bits must be between 1 and 64 (both inclusive). Requested: " + numBits);
        }

        long maskedValue = (numBits == 64) ? value : (value & ((1L << numBits) - 1));

        for (int i = numBits - 1; i >= 0; i--) {
            int bit = (int) ((maskedValue >>> i) & 1);
            buffer = (buffer << 1) | bit;
            bitsInBuffer++;

            if (bitsInBuffer == 8) {
                byteOut.write(buffer);
                buffer = 0;
                bitsInBuffer = 0;
            }
        }
    }


    /**
     * Writes a signed integer value using a custom number of bits.
     *
     * @param value The value to write (up to 63 bits of signed array).
     * @param numBits The number of bits to encode the signed value (1 to 63).
     */
    public void writeInt(long value, int numBits) {
        // For signed numbers, we simply write the bits,
        // relying on Java's two's complement representation.
        writeBits(value, numBits);
    }

    /**
     * Writes an unsigned integer value using a custom number of bits.
     *
     * @param value The value to write (must be non-negative).
     * @param numBits The number of bits to encode the unsigned value (1 to 63).
     */
    public void writeUInt(long value, int numBits) {
        if (value < 0) {
            throw new IllegalArgumentException("Unsigned value must be non-negative.");
        }
        writeBits(value, numBits);
    }

    // --- Arbitrary Bit Length Primitives (Signed) ---

    /**
     * Writes a signed byte value using a custom number of bits.
     *
     * @param value The byte value to write.
     * @param numBits The number of bits to encode the value (1 to 8).
     */
    public void writeByte(byte value, int numBits) {
        if (numBits < 1 || numBits > 8) {
            throw new IllegalArgumentException("Number of bits for byte must be between 1 and 8.");
        }
        writeInt(value, numBits);
    }

    /**
     * Writes a signed short value using a custom number of bits.
     *
     * @param value The short value to write.
     * @param numBits The number of bits to encode the value (1 to 16).
     */
    public void writeShort(short value, int numBits) {
        if (numBits < 1 || numBits > 16) {
            throw new IllegalArgumentException("Number of bits for short must be between 1 and 16.");
        }
        writeInt(value, numBits);
    }

    /**
     * Writes a signed integer value using a custom number of bits.
     *
     * @param value The int value to write.
     * @param numBits The number of bits to encode the value (1 to 32).
     */
    public void writeInteger(int value, int numBits) {
        if (numBits < 1 || numBits > 32) {
            throw new IllegalArgumentException("Number of bits for int must be between 1 and 32.");
        }
        writeInt(value, numBits);
    }

    /**
     * Writes a signed long value using a custom number of bits.
     *
     * @param value The long value to write.
     * @param numBits The number of bits to encode the value (1 to 63).
     */
    public void writeLong(long value, int numBits) {
        // writeInt(value, numBits) already handles up to 63 bits.
        writeInt(value, numBits);
    }

    // --- Floating Point Types (Arbitrary Bit Length) ---

    /**
     * Writes the IEEE 754 bit pattern of a float using a custom number of bits.
     *
     * @param value The float value to write.
     */
    public void writeFloat(float value) {
        writeFloat(value, 32);
    }

    /**
     * Writes the IEEE 754 bit pattern of a float using a custom number of bits.
     *
     * @param value The float value to write.
     * @param numBits The number of bits to encode the value (1 to 32).
     */
    public void writeFloat(float value, int numBits) {
        if (numBits < 1 || numBits > 32) {
            throw new IllegalArgumentException("Number of bits for float must be between 1 and 32.");
        }
        // Get the raw 32-bit integer representation
        int bits = Float.floatToIntBits(value);
        writeInteger(bits, numBits);
    }

    /**
     * Writes the IEEE 754 bit pattern of a double using a custom number of bits.
     *
     * @param value The double value to write.
     */
    public void writeDouble(double value) {
        writeDouble(value, 64);
    }

    /**
     * Writes the IEEE 754 bit pattern of a double using a custom number of bits.
     *
     * @param value The double value to write.
     * @param numBits The number of bits to encode the value (1 to 64).
     */
    public void writeDouble(double value, int numBits) {
        if (numBits < 1 || numBits > 64) {
            throw new IllegalArgumentException("Number of bits for double must be between 1 and 64.");
        }
        // Get the raw 64-bit long representation
        long bits = Double.doubleToLongBits(value);
        writeLong(bits, numBits);
    }

    // --- Arbitrary Bit Length Primitives (Unsigned) ---

    /**
     * Writes an unsigned byte value using a custom number of bits.
     *
     * @param value The unsigned byte value to write (as an int, 0-255).
     * @param numBits The number of bits to encode the value (1 to 8).
     */
    public void writeUByte(int value, int numBits) {
        if (numBits < 1 || numBits > 8) {
            throw new IllegalArgumentException("Number of bits for unsigned byte must be between 1 and 8.");
        }
        writeUInt((long) value, numBits);
    }

    /**
     * Writes an unsigned short value using a custom number of bits.
     *
     * @param value The unsigned short value to write (as an int, 0-65535).
     * @param numBits The number of bits to encode the value (1 to 16).
     */
    public void writeUShort(int value, int numBits) {
        if (numBits < 1 || numBits > 16) {
            throw new IllegalArgumentException("Number of bits for unsigned short must be between 1 and 16.");
        }
        writeUInt((long) value, numBits);
    }

    /**
     * Writes an unsigned integer value using a custom number of bits.
     *
     * @param value The unsigned int value to write (as a long).
     * @param numBits The number of bits to encode the value (1 to 32).
     */
    public void writeUInteger(long value, int numBits) {
        if (numBits < 1 || numBits > 32) {
            throw new IllegalArgumentException("Number of bits for unsigned int must be between 1 and 32.");
        }
        writeUInt(value, numBits);
    }

    /**
     * Writes an unsigned long value using a custom number of bits.
     *
     * @param value The unsigned long value to write.
     * @param numBits The number of bits to encode the value (1 to 63).
     */
    public void writeULong(long value, int numBits) {
        // writeUInt(value, numBits) already handles up to 63 bits.
        writeUInt(value, numBits);
    }

    // --- Primitive Type Aliases (Standard Bit Lengths) ---

    public void writeBoolean(boolean value) { writeBits(value ? 1 : 0, 1); }
    public void writeByte(byte value) { writeInt(value, 8); }
    public void writeUByte(int value) { writeUInt(value, 8); }
    public void writeShort(short value) { writeInt(value, 16); }
    public void writeUShort(int value) { writeUInt(value, 16); }
    public void writeInt(int value) { writeInt(value, 32); }
    public void writeUInt(long value) { writeUInt(value, 32); }
    public void writeLong(long value) { writeInt(value, 64); }

    // --- Big Data Types ---

    /**
     * Writes a BigInteger by first writing its dimensions (in bytes) and then its two's complement byte array.
     * Note: BigInteger is written aligned to the byte boundary, using 32 bits for the dimensions.
     *
     * @param value The BigInteger to write.
     */
    public void writeBigInteger(BigInteger value) {
        byte[] data = value.toByteArray();

        // Write the dimensions of the byte array (using 32-bit signed int)
        writeInt(data.length, 32);

        // Write the bytes themselves (always 8-bit aligned)
        for (byte b : data) {
            writeByte(b);
        }
    }

    /**
     * Writes the least significant 'numBits' of a BigInteger to the stream,
     * in 63-bit chunks from MSB to LSB.
     * * @param value The BigInteger to write.
     * @param numBits The number of bits to encode the value (>= 1).
     * @throws IllegalArgumentException If numBits is less than 1.
     */
    public void writeBigInteger(BigInteger value, int numBits) {
        if (numBits < 1) {
            throw new IllegalArgumentException("Number of bits for BigInteger must be at least 1.");
        }

        BigInteger mask = BigInteger.ONE.shiftLeft(numBits).subtract(BigInteger.ONE);
        BigInteger bitsToWrite = value.and(mask);

        int remainingBits = numBits;

        // write the big integer in chunks
        while (remainingBits > 0) {
            int bitsInChunk = Math.min(64, remainingBits);
            int shiftAmount = remainingBits - bitsInChunk;
            long chunk = bitsToWrite.shiftRight(shiftAmount).longValue();

            writeBits(chunk, bitsInChunk);

            remainingBits -= bitsInChunk;
        }
    }

    /**
     * Writes a BigDecimal by writing its scale (32-bit signed int) and then its unscaled value (BigInteger).
     *
     * @param value The BigDecimal to write.
     */
    public void writeBigDecimal(BigDecimal value) {
        // 1. Write the scale
        writeInt(value.scale(), 32);

        // 2. Write the unscaled value
        writeBigInteger(value.unscaledValue());
    }

    /**
     * Writes a BigDecimal by writing its scale (32-bit signed int) and then
     * its unscaled value (BigInteger) using a custom number of bits.
     *
     * @param value The BigDecimal to write.
     * @param numBits The number of bits to encode the unscaled value (>= 1).
     */
    public void writeBigDecimal(BigDecimal value, int numBits) {
        // 1. Write the scale (fixed 32-bit size for structural component)
        writeInt(value.scale(), 32);

        // 2. Write the unscaled value using the arbitrary bit dimensions
        writeBigInteger(value.unscaledValue(), numBits);
    }


    // --- VAR INTS ---
    /**
     * Writes an unsigned LEB128 (ULEB128) value to the stream.
     *
     * @param value The non-negative long value to encode.
     */
    public void writeUVarInt(long value) {
        if (value < 0) throw new IllegalArgumentException("Unsigned value cannot be negative for ULEB128.");

        do {
            int byteVal = (int) (value & 0x7F);
            value >>>= 7;
            if (value != 0) {
                byteVal |= 0x80; // set continuation bit
            }
            writeByte((byte) byteVal);
        } while (value != 0);
    }

    /**
     * Writes a signed LEB128 (SLEB128) value to the stream.
     *
     * @param value The signed long value to encode.
     */
    public void writeVarInt(long value) {
        boolean more;
        do {
            int byteVal = (int) (value & 0x7F);
            boolean signBitSet = (byteVal & 0x40) != 0;
            value >>= 7;

            more = !((value == 0 && !signBitSet) || (value == -1 && signBitSet));
            if (more) {
                byteVal |= 0x80; // set continuation bit
            }

            writeByte((byte) byteVal);
        } while (more);
    }


    /**
     * Writes any remaining bits in the buffer, padding the last byte with zeros,
     * and returns the complete encoded byte array.
     *
     * @return The resulting byte array containing the bit stream.
     * @throws BitStreamWriteException If an underlying I/O error occurs during final byte write.
     */
    public byte[] toByteArray() {
        if (bitsInBuffer > 0) {
            // Pad with zeros on the right and write the final byte
            // The buffer holds the bits aligned to the left side of the final byte.
            // We need to shift them left to fill the 8-bit byte space.
            buffer <<= (8 - bitsInBuffer);
            byteOut.write(buffer);
            buffer = 0;
            bitsInBuffer = 0;
        }
        return byteOut.toByteArray();
    }

    public BinaryData toBinaryData() {
        final int bitsInBuffer = this.bitsInBuffer;
        int padding = (bitsInBuffer == 0) ? 0 : (8 - bitsInBuffer);
        return new BinaryData(toByteArray(), padding);
    }

}