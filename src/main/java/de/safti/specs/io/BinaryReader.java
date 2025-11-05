package de.safti.specs.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A bit-level input reader that allows reading primitive types and arbitrary
 * bit amounts from an underlying byte array.
 */
class BitReader implements Closeable {

    private final byte[] data;
    private int bytePosition; // Tracks the current byte index in the array
    private int buffer;       // Internal buffer (stores the current byte, 0-255)
    private int bitsInBuffer; // Number of bits currently available in the buffer (0-8)

    /**
     * Creates a BitReader wrapping the given byte array.
     * @param data The byte array containing the bit stream data.
     */
    public BitReader(byte[] data) {
        this.data = data;
        this.bytePosition = 0;
        this.buffer = 0;
        this.bitsInBuffer = 0;
    }

    /**
     * Fills the internal buffer with the next byte from the underlying array.
     *
     * @throws ArrayIndexOutOfBoundsException If the end of the array is reached.
     */
    private void fillBuffer() throws ArrayIndexOutOfBoundsException {
        if (bytePosition >= data.length) {
            // Check for reading too long and throw the requested exception type
            throw new ArrayIndexOutOfBoundsException("Attempted to read beyond the data array bounds.");
        }
        // Read the next byte (masked to treat it as unsigned 0-255)
        buffer = data[bytePosition] & 0xFF;
        bytePosition++;
        bitsInBuffer = 8;
    }

    /**
     * Reads an unsigned value composed of 'numBits' from the stream.
     * This is the core method for all reading operations.
     *
     * @param numBits The number of bits to read (1 to 63).
     * @return The unsigned long value read from the stream.
     * @throws ArrayIndexOutOfBoundsException If the end of the data array is reached.
     */
    public long readBits(int numBits) throws ArrayIndexOutOfBoundsException {
        if (numBits < 1 || numBits > 63) {
            throw new IllegalArgumentException("Number of bits must be between 1 and 63. Requested: " + numBits);
        }

        long result = 0;
        int remainingBitsToRead = numBits;

        while (remainingBitsToRead > 0) {
            if (bitsInBuffer == 0) {
                fillBuffer();
            }

            // How many bits can we take from the current buffer?
            int bitsToTake = Math.min(remainingBitsToRead, bitsInBuffer);

            // Calculate the mask to extract the highest 'bitsToTake' bits from the buffer
            int mask = (1 << bitsToTake) - 1;

            // 1. Shift buffer to the right to align the desired bits to the LSB position
            // 2. Mask the result to extract only those bits
            int extractedBits = (buffer >>> (bitsInBuffer - bitsToTake)) & mask;

            // Append the extracted bits to the result
            result = (result << bitsToTake) | extractedBits;

            // Update counters
            bitsInBuffer -= bitsToTake;
            remainingBitsToRead -= bitsToTake;
        }

        return result;
    }

    /**
     * Reads a signed integer value composed of 'numBits' from the stream,
     * performing sign extension if the most significant bit is set.
     *
     * @param numBits The number of bits to read (1 to 63).
     * @return The signed long value read from the stream.
     * @throws ArrayIndexOutOfBoundsException If the end of the data array is reached.
     */
    public long readInt(int numBits) throws ArrayIndexOutOfBoundsException {
        if (numBits < 1 || numBits > 63) {
            throw new IllegalArgumentException("Number of bits must be between 1 and 63. Requested: " + numBits);
        }

        long value = readBits(numBits);

        // Sign extension logic
        long signBit = 1L << (numBits - 1);

        if ((value & signBit) != 0) {
            // The sign bit is set (value is negative).
            // Fill all higher bits with 1s to perform two's complement extension.
            // Create a mask for bits from 'numBits' up to 63.
            long extensionMask = ~((1L << numBits) - 1);
            value |= extensionMask;
        }

        return value;
    }

    /**
     * Reads an unsigned integer value composed of 'numBits' from the stream.
     *
     * @param numBits The number of bits to read (1 to 63).
     * @return The unsigned long value read from the stream.
     * @throws ArrayIndexOutOfBoundsException If the end of the data array is reached.
     */
    public long readUInt(int numBits) throws ArrayIndexOutOfBoundsException {
        return readBits(numBits);
    }

    // --- Primitive Type Aliases (Standard Bit Lengths) ---

    public boolean readBoolean() throws ArrayIndexOutOfBoundsException { return readBits(1) != 0; }
    public byte readByte() throws ArrayIndexOutOfBoundsException { return (byte) readInt(8); }
    public int readUByte() throws ArrayIndexOutOfBoundsException { return (int) readUInt(8); }
    public short readShort() throws ArrayIndexOutOfBoundsException { return (short) readInt(16); }
    public int readUShort() throws ArrayIndexOutOfBoundsException { return (int) readUInt(16); }
    public int readInt() throws ArrayIndexOutOfBoundsException { return (int) readInt(32); }
    public long readUInt() throws ArrayIndexOutOfBoundsException { return readUInt(32); }
    public long readLong() throws ArrayIndexOutOfBoundsException { return readInt(64); }

    // --- Big Data Types ---

    /**
     * Reads a BigInteger by first reading its length (in bytes) and then the byte array.
     *
     * @return The BigInteger read from the stream.
     * @throws ArrayIndexOutOfBoundsException If the end of the data array is reached while reading the length or the bytes.
     */
    public BigInteger readBigInteger() throws ArrayIndexOutOfBoundsException {
        // The BigInteger must be read on a byte boundary. Align first.
        if (bitsInBuffer != 0) {
            readBits(bitsInBuffer); // Consume remaining bits
        }

        // 1. Read the length of the byte array (32-bit signed int)
        // This relies on readInt(32) to throw ArrayIndexOutOfBoundsException if needed.
        int length = Math.toIntExact(readInt(32));
        if (length < 0) {
            throw new CorruptDataException("Invalid length read for BigInteger: " + length);
        }

        // 2. Read the bytes themselves (using the 8-bit aligned readByte() method)
        byte[] bigIntData = new byte[length];

        for (int i = 0; i < length; i++) {
            bigIntData[i] = readByte(); // readByte relies on fillBuffer which checks bounds
        }

        return new BigInteger(bigIntData);
    }

    /**
     * Reads a BigDecimal by first reading its scale and then its unscaled value (BigInteger).
     *
     * @return The BigDecimal read from the stream.
     * @throws ArrayIndexOutOfBoundsException If the end of the data array is reached.
     */
    public BigDecimal readBigDecimal() throws ArrayIndexOutOfBoundsException {
        // 1. Read the scale (32-bit signed int)
        int scale = Math.toIntExact(readInt(32));

        // 2. Read the unscaled value
        BigInteger unscaledValue = readBigInteger();

        return new BigDecimal(unscaledValue, scale);
    }

    /**
     * This implementation does not wrap an InputStream, so there is nothing to close.
     */
    @Override
    public void close() {
        // No underlying stream to close, but implements Closeable for interface consistency.
    }
}
