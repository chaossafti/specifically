package de.safti.specs.layout;

import de.safti.specs.annotations.Spec;
import de.safti.specs.io.BinaryData;
import de.safti.specs.io.BinaryWriter;
import de.safti.specs.layout.common.SpecField;
import org.jetbrains.annotations.Nullable;

public interface SpecLayout {

    Class<? extends Spec> getSpecClass();

    Spec create(BinaryData data);

    Spec createInstance();

    void write(Spec spec, BinaryWriter writer);

    @Nullable
    SpecField getField(String name);

    SpecField[] getFields();

}
