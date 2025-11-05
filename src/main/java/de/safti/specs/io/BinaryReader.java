package de.safti.specs.io;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A bit-level input reader that allows reading primitive types and arbitrary
 * bit amounts from an underlying byte array.
 */
public class BinaryReader {

    private final BinaryData data;
    private int bytePosition; // Tracks the current byte index in the array
    private int buffer;       // Internal buffer (stores the current byte, 0-255)
    private int bitsInBuffer; // Number of bits currently available in the buffer (0-8)

    /**
     * Creates a BitReader wrapping the given byte array.
     * @param data The byte array containing the bit stream array.
     */
    public BinaryReader(BinaryData data) {
        this.data = data;
        this.bytePosition = 0;
        this.buffer = 0;
        this.bitsInBuffer = 0;
    }

    /**
     * Fills the internal buffer with the next byte from the underlying array,
     * accounting for padding bits in the final byte.
     *
     * @throws ArrayIndexOutOfBoundsException If the end of the array is reached.
     */
    private void fillBuffer() throws ArrayIndexOutOfBoundsException {
        byte[] arr = data.array();
        int padding = data.padding();

        if (bytePosition >= arr.length) {
            throw new ArrayIndexOutOfBoundsException("Attempted to read beyond the array bounds.");
        }

        // Read next byte (unsigned)
        buffer = arr[bytePosition] & 0xFF;
        bytePosition++;

        // If this is the last byte, remove the padding bits from the count
        if (bytePosition == arr.length && padding > 0) {
            bitsInBuffer = 8 - padding;
            buffer = buffer >>> padding; // shift out padding bits
        } else {
            bitsInBuffer = 8;
        }
    }


    /**
     * Reads an unsigned value composed of 'numBits' from the stream.
     * This is the core method for all reading operations.
     *
     * @param numBits The number of bits to read (1 to 63).
     * @return The unsigned long value read from the stream.
     * @throws ArrayIndexOutOfBoundsException If the end of the array array is reached.
     */
    public long readBits(int numBits) throws ArrayIndexOutOfBoundsException {
        if (numBits < 1 || numBits > 64) {
            throw new IllegalArgumentException("Number of bits must be between 1 and 64. Requested: " + numBits);
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
     * @throws ArrayIndexOutOfBoundsException If the end of the array array is reached.
     */
    public long readSignedBits(int numBits) throws ArrayIndexOutOfBoundsException {
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
            long extensionMask = -(1L << numBits);
            value |= extensionMask;
        }

        return value;
    }

    /**
     * Reads an unsigned integer value composed of 'numBits' from the stream.
     *
     * @param numBits The number of bits to read (1 to 32).
     * @return The unsigned long value read from the stream.
     * @throws ArrayIndexOutOfBoundsException If the end of the array array is reached.
     */
    public long readUInt(int numBits) throws ArrayIndexOutOfBoundsException {
        return readBits(numBits);
    }

    // --- Primitive Type Aliases (Standard Bit Lengths) ---

    public boolean readBoolean() throws ArrayIndexOutOfBoundsException { return readBits(1) != 0; }
    public byte readByte() throws ArrayIndexOutOfBoundsException { return readByte(8); }
    public int readUByte() throws ArrayIndexOutOfBoundsException { return readUByte(8); }
    public short readShort() throws ArrayIndexOutOfBoundsException { return readShort(16); }
    public int readUShort() throws ArrayIndexOutOfBoundsException { return readUShort(16); }
    public int readInt() throws ArrayIndexOutOfBoundsException { return readInt(32); }
    public long readUInt() throws ArrayIndexOutOfBoundsException { return readUInteger(32); }
    public long readLong() throws ArrayIndexOutOfBoundsException { return readLong(64); }

    // --- Arbitrary Bit Length Primitives (Signed) ---

    /**
     * Reads a signed byte value composed of 'numBits' from the stream.
     * The value is sign-extended and then truncated to a byte.
     *
     * @param numBits The number of bits to read (1 to 8).
     * @return The signed byte value read from the stream.
     * @throws IllegalArgumentException If numBits is outside the range 1-8.
     * @throws ArrayIndexOutOfBoundsException If the end of the array array is reached.
     */
    public byte readByte(int numBits) throws ArrayIndexOutOfBoundsException {
        if (numBits < 1 || numBits > 8) {
            throw new IllegalArgumentException("Number of bits for byte must be between 1 and 8. Requested: " + numBits);
        }
        return (byte) readSignedBits(numBits);
    }

    /**
     * Reads a signed short value composed of 'numBits' from the stream.
     * The value is sign-extended and then truncated to a short.
     *
     * @param numBits The number of bits to read (1 to 16).
     * @return The signed short value read from the stream.
     * @throws IllegalArgumentException If numBits is outside the range 1-16.
     * @throws ArrayIndexOutOfBoundsException If the end of the array array is reached.
     */
    public short readShort(int numBits) throws ArrayIndexOutOfBoundsException {
        if (numBits < 1 || numBits > 16) {
            throw new IllegalArgumentException("Number of bits for short must be between 1 and 16. Requested: " + numBits);
        }
        return (short) readSignedBits(numBits);
    }

    /**
     * Reads a signed integer value composed of 'numBits' from the stream.
     * The value is sign-extended and then truncated to an int.
     *
     * @param numBits The number of bits to read (1 to 32).
     * @return The signed integer value read from the stream.
     * @throws IllegalArgumentException If numBits is outside the range 1-32.
     * @throws ArrayIndexOutOfBoundsException If the end of the array array is reached.
     */
    public int readInt(int numBits) throws ArrayIndexOutOfBoundsException {
        if (numBits < 1 || numBits > 32) {
            throw new IllegalArgumentException("Number of bits for int must be between 1 and 32. Requested: " + numBits);
        }
        return (int) readSignedBits(numBits);
    }

    /**
     * Reads a signed long value composed of 'numBits' from the stream.
     * The value is correctly sign-extended.
     *
     * @param numBits The number of bits to read (1 to 63).
     * @return The signed long value read from the stream.
     * @throws IllegalArgumentException If numBits is outside the range 1-63.
     * @throws ArrayIndexOutOfBoundsException If the end of the array array is reached.
     */
    public long readLong(int numBits) throws ArrayIndexOutOfBoundsException {
        // readInt(numBits) already returns a sign-extended long for 1-63 bits.
        return readSignedBits(numBits);
    }

    // --- Arbitrary Bit Length Primitives (Unsigned) ---

    /**
     * Reads an unsigned byte value composed of 'numBits' from the stream.
     *
     * @param numBits The number of bits to read (1 to 8).
     * @return The unsigned byte value read from the stream (as an int).
     * @throws IllegalArgumentException If numBits is outside the range 1-8.
     * @throws ArrayIndexOutOfBoundsException If the end of the array array is reached.
     */
    public int readUByte(int numBits) throws ArrayIndexOutOfBoundsException {
        if (numBits < 1 || numBits > 8) {
            throw new IllegalArgumentException("Number of bits for unsigned byte must be between 1 and 8. Requested: " + numBits);
        }
        return (int) readUInt(numBits);
    }

    /**
     * Reads an unsigned short value composed of 'numBits' from the stream.
     *
     * @param numBits The number of bits to read (1 to 16).
     * @return The unsigned short value read from the stream (as an int).
     * @throws IllegalArgumentException If numBits is outside the range 1-16.
     * @throws ArrayIndexOutOfBoundsException If the end of the array array is reached.
     */
    public int readUShort(int numBits) throws ArrayIndexOutOfBoundsException {
        if (numBits < 1 || numBits > 16) {
            throw new IllegalArgumentException("Number of bits for unsigned short must be between 1 and 16. Requested: " + numBits);
        }
        return (int) readUInt(numBits);
    }

    /**
     * Reads an unsigned integer value composed of 'numBits' from the stream.
     *
     * @param numBits The number of bits to read (1 to 32).
     * @return The unsigned integer value read from the stream (as a long).
     * @throws IllegalArgumentException If numBits is outside the range 1-32.
     * @throws ArrayIndexOutOfBoundsException If the end of the array array is reached.
     */
    public long readUInteger(int numBits) throws ArrayIndexOutOfBoundsException {
        if (numBits < 1 || numBits > 32) {
            throw new IllegalArgumentException("Number of bits for unsigned int must be between 1 and 32. Requested: " + numBits);
        }
        return readUInt(numBits);
    }

    /**
     * Reads an unsigned long value composed of 'numBits' from the stream.
     *
     * @param numBits The number of bits to read (1 to 63).
     * @return The unsigned long value read from the stream.
     * @throws IllegalArgumentException If numBits is outside the range 1-63.
     * @throws ArrayIndexOutOfBoundsException If the end of the array array is reached.
     */
    public long readULong(int numBits) throws ArrayIndexOutOfBoundsException {
        // readUInt(numBits) already returns the unsigned long for 1-63 bits.
        return readUInt(numBits);
    }

    // --- Floating Point Types (Arbitrary Bit Length) ---

    /**
     * Reads the IEEE 754 bit pattern for a float using a custom number of bits.
     *
     * @return The float value reconstructed from the raw bit pattern.
     * @throws IllegalArgumentException       If numBits is outside the range 1-32.
     * @throws ArrayIndexOutOfBoundsException If the end of the array array is reached.
     */
    public float readFloat() throws ArrayIndexOutOfBoundsException {
        return readFloat(32);
    }

    /**
     * Reads the IEEE 754 bit pattern for a float using a custom number of bits.
     *
     * @param numBits The number of bits to read (1 to 32).
     * @return The float value reconstructed from the raw bit pattern.
     * @throws IllegalArgumentException If numBits is outside the range 1-32.
     * @throws ArrayIndexOutOfBoundsException If the end of the array array is reached.
     */
    public float readFloat(int numBits) throws ArrayIndexOutOfBoundsException {
        if (numBits < 1 || numBits > 32) {
            throw new IllegalArgumentException("Number of bits for float must be between 1 and 32.");
        }
        // Reads the bits as a raw, unsigned pattern, padded to the left with zeros.
        long bits = readUInt(numBits);
        return Float.intBitsToFloat((int) bits);
    }

    /**
     * Reads the IEEE 754 bit pattern for a double using a custom number of bits.
     *
     * @return The double value reconstructed from the raw bit pattern.
     * @throws IllegalArgumentException       If numBits is outside the range 1-64.
     * @throws ArrayIndexOutOfBoundsException If the end of the array array is reached.
     */
    public double readDouble() throws ArrayIndexOutOfBoundsException {
        return readDouble(64);
    }

    /**
     * Reads the IEEE 754 bit pattern for a double using a custom number of bits.
     *
     * @param numBits The number of bits to read (1 to 64).
     * @return The double value reconstructed from the raw bit pattern.
     * @throws IllegalArgumentException If numBits is outside the range 1-64.
     * @throws ArrayIndexOutOfBoundsException If the end of the array array is reached.
     */
    public double readDouble(int numBits) throws ArrayIndexOutOfBoundsException {
        if (numBits < 1 || numBits > 64) {
            throw new IllegalArgumentException("Number of bits for double must be between 1 and 64.");
        }

        if (numBits <= 63) {
            // Use readUInt for up to 63 bits (it returns a long).
            long bits = readUInt(numBits);
            // This is safe because Double.longBitsToDouble interprets the full 64 bits,
            // and the bits not read are implicitly 0 (since it's a new long value).
            return Double.longBitsToDouble(bits);
        } else { // numBits == 64
            return Double.longBitsToDouble(readBits(64));
        }
    }

    // --- Big Data Types ---

    /**
     * Reads a BigInteger by first reading its dimensions (in bytes) and then the byte array.
     *
     * @return The BigInteger read from the stream.
     * @throws ArrayIndexOutOfBoundsException If the end of the array array is reached while reading the dimensions or the bytes.
     */
    public BigInteger readBigInteger(int bits) throws ArrayIndexOutOfBoundsException {
        // align bits to the next byte boundary
        if(bits % 8 != 0) {
            bits += 8 - (bits % 8); // align to next byte
        }

        int byteCount = bits / 8;
        int bitsToRead = bits;

        // create and fill the big integer array
        byte[] bigIntData = new byte[byteCount];
        for (int i = 0; i < byteCount; i++) {
            int readThisIteration = Math.min(bitsToRead, 8);
            bitsToRead -= readThisIteration;
            bigIntData[i] = readByte(readThisIteration);
        }

        return new BigInteger(bigIntData);
    }

    /**
     * Reads a BigDecimal by first reading its scale and then its unscaled value (BigInteger).
     *
     * @return The BigDecimal read from the stream.
     * @throws ArrayIndexOutOfBoundsException If the end of the array array is reached.
     */
    public BigDecimal readBigDecimal(int bits) throws ArrayIndexOutOfBoundsException {
        int scale = readInt(32);
        BigInteger unscaledValue = readBigInteger(bits);

        return new BigDecimal(unscaledValue, scale);
    }


    // --- VAR INTS ---
    /**
     * Reads an unsigned LEB128 (ULEB128) value from the stream.
     *
     * @return The decoded unsigned long value.
     */
    public long readUVarInt() {
        long result = 0;
        int shift = 0;
        long b;
        do {
            b = readByte() & 0xFF;
            result |= (b & 0x7F) << shift;
            shift += 7;
        } while ((b & 0x80) != 0);
        return result;
    }

    /**
     * Reads a signed LEB128 (SLEB128) value from the stream.
     *
     * @return The decoded signed long value.
     */
    public long readVarInt() {
        long result = 0;
        int shift = 0;
        long b;
        do {
            b = readByte() & 0xFF;
            result |= (b & 0x7F) << shift;
            shift += 7;
        } while ((b & 0x80) != 0);

        // sign extend if the sign bit of the last byte is set
        if ((shift < 64) && ((b & 0x40) != 0)) {
            result |= -1L << shift;
        }

        return result;
    }


    /**
     * Checks if there are any remaining bits to read in the stream.
     * Considers both buffered bits and valid bits remaining in the data array.
     *
     * @return true if there are more bits available, false otherwise.
     */
    public boolean hasMore() {
        int totalBits = (data.array().length * 8) - data.padding();
        int bitsRead = (bytePosition * 8) - bitsInBuffer;
        return bitsRead < totalBits;
    }

    /**
     * Checks if the given number of bits can be read from the stream
     * without exceeding available (non-padded) data.
     *
     * @param bits the number of bits to check for availability.
     * @return true if that many bits can be read, false otherwise.
     */
    public boolean canRead(int bits) {
        int totalBits = (data.array().length * 8) - data.padding();
        int bitsRead = (bytePosition * 8) - bitsInBuffer;
        return (bitsRead + bits) <= totalBits;
    }

}
