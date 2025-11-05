package de.safti.specs.layout.common;

import de.safti.specs.io.BinaryReader;
import de.safti.specs.io.BinaryWriter;
import de.safti.specs.layout.SpecContext;
import org.jetbrains.annotations.Nullable;

public interface TypeDef {

    Object read(BinaryReader reader, SpecContext context);

    void write(BinaryWriter writer, Object o);

    @Nullable
    default Object createDefault() {
        return null;
    }

}
