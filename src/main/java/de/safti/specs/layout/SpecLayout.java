package de.safti.specs.spec;

import de.safti.specs.annotations.Spec;

import java.io.ByteArrayOutputStream;

public interface SpecLayout {

    Class<? extends Spec> getSpecClass();

    Spec create(byte[] data);

    void write(Spec spec, ByteArrayOutputStream baos);

}
