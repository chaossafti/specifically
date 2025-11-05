package de.safti.specs.io;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import java.io.*;

import java.io.*;

/**
 * A bit-level output stream that allows writing primitive types and arbitrary
 * bit amounts to an underlying OutputStream.
 */
class BitWriter implements Closeable, Flushable {

    private final OutputStream out;
    private int buffer;       // Internal buffer for bits (up to 8 bits)
    private int bitsInBuffer; // Number of bits currently in the buffer (0-7)

    /**
     * Creates a BitWriter wrapping the given OutputStream.
     * @param out The underlying byte stream.
     */
    public BitWriter(OutputStream out) {
        this.out = out;
        this.buffer = 0;
        this.bitsInBuffer = 0;
    }

    /**
     * Writes the least significant 'numBits' of 'value' to the stream.
     * This is the core method for all writing operations.
     *
     * @param value The long value containing the bits to write.
     * @param numBits The number of bits to write (1 to 63).
     * @throws IOException If an I/O error occurs.
     */
    public void writeBits(long value, int numBits) {
        if (numBits < 1 || numBits > 63) {
            throw new IllegalArgumentException("Number of bits must be between 1 and 63. Requested: " + numBits);
        }

        // Mask the value to ensure only the relevant bits are used
        long maskedValue = value & ((1L << numBits) - 1);

        for (int i = numBits - 1; i >= 0; i--) {
            // Extract the current bit (0 or 1)
            int bit = (int) ((maskedValue >>> i) & 1);

            // Shift the bit into the buffer
            buffer = (buffer << 1) | bit;
            bitsInBuffer++;

            // If the buffer is full (8 bits), write it out
            if (bitsInBuffer == 8) {
                out.write(buffer);
                buffer = 0;
                bitsInBuffer = 0;
            }
        }
    }

    /**
     * Writes a signed integer value using a custom number of bits.
     *
     * @param value The value to write (up to 63 bits of signed data).
     * @param numBits The number of bits to encode the signed value (1 to 63).
     * @throws IOException If an I/O error occurs.
     */
    public void writeInt(long value, int numBits) throws IOException {
        // For signed numbers, we simply write the bits,
        // relying on Java's two's complement representation.
        writeBits(value, numBits);
    }

    /**
     * Writes an unsigned integer value using a custom number of bits.
     *
     * @param value The value to write (must be non-negative).
     * @param numBits The number of bits to encode the unsigned value (1 to 63).
     * @throws IOException If an I/O error occurs.
     */
    public void writeUInt(long value, int numBits) throws IOException {
        if (value < 0) {
            throw new IllegalArgumentException("Unsigned value must be non-negative.");
        }
        writeBits(value, numBits);
    }

    // --- Primitive Type Aliases (Standard Bit Lengths) ---

    public void writeBoolean(boolean value) throws IOException { writeBits(value ? 1 : 0, 1); }
    public void writeByte(byte value) throws IOException { writeInt(value, 8); }
    public void writeUByte(int value) throws IOException { writeUInt(value, 8); }
    public void writeShort(short value) throws IOException { writeInt(value, 16); }
    public void writeUShort(int value) throws IOException { writeUInt(value, 16); }
    public void writeInt(int value) throws IOException { writeInt(value, 32); }
    public void writeUInt(long value) throws IOException { writeUInt(value, 32); }
    public void writeLong(long value) throws IOException { writeInt(value, 64); }

    // --- Big Data Types ---

    /**
     * Writes a BigInteger by first writing its length (in bytes) and then its two's complement byte array.
     *
     * @param value The BigInteger to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeBigInteger(BigInteger value) throws IOException {
        byte[] data = value.toByteArray();

        // Write the length of the byte array (using 32-bit signed int)
        writeInt(data.length, 32);

        // Write the bytes themselves (always 8-bit aligned)
        for (byte b : data) {
            writeByte(b);
        }
    }

    /**
     * Writes a BigDecimal by writing its scale (32-bit signed int) and then its unscaled value (BigInteger).
     *
     * @param value The BigDecimal to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeBigDecimal(BigDecimal value) throws IOException {
        // 1. Write the scale
        writeInt(value.scale(), 32);

        // 2. Write the unscaled value
        writeBigInteger(value.unscaledValue());
    }

    /**
     * Writes any remaining bits in the buffer, padding the last byte with zeros,
     * and flushes the underlying stream.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void flush() throws IOException {
        if (bitsInBuffer > 0) {
            // Pad with zeros on the right and write the final byte
            // The buffer holds the bits aligned to the left side of the final byte.
            // We need to shift them left to fill the 8-bit byte space.
            buffer <<= (8 - bitsInBuffer);
            out.write(buffer);
            buffer = 0;
            bitsInBuffer = 0;
        }
        out.flush();
    }

    /**
     * Flushes the buffer and closes the underlying stream.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        try {
            flush();
        } finally {
            out.close();
        }
    }
}

