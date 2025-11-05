package de.safti.specs.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BinaryWriterTest {

    @Test
    void writeULong() {
        BinaryWriter writer = new BinaryWriter();
        writer.writeUInt(-123, 64);

        BinaryReader reader = new BinaryReader(writer.toByteArray());

        Assertions.assertEquals(-123, reader.readULong(64));

    }
}