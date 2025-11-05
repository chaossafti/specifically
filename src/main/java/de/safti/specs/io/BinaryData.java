package de.safti.specs.io;

import org.jetbrains.annotations.NotNull;

/**
 * @param array The byte[] array.
 * @param padding The amount of 0's that have been appended.
 */
public record BinaryData(byte[] array, int padding) {


    @Override
    public @NotNull String toString() {
        StringBuilder bits = new StringBuilder(array.length * 8);

        // Convert all bytes to bit strings
        for (byte b : array) {
            bits.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }

        // Remove trailing padding bits
        if (padding > 0 && padding <= bits.length()) {
            bits.setLength(bits.length() - padding);
        }

        // Insert spaces every 4 bits for readability
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < bits.length(); i++) {
            if (i > 0 && i % 4 == 0) formatted.append(' ');
            formatted.append(bits.charAt(i));
        }

        return formatted.toString();
    }


}
