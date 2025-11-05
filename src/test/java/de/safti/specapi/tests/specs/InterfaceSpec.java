package de.safti.specapi.tests.specs;

import de.safti.specs.annotations.Field;
import de.safti.specs.annotations.Spec;
import de.safti.specs.annotations.Type;

public interface InterfaceSpec extends Spec {

    @Type.StringTerminated
    String name();

    @Field.Setter("name")
    void setName(String name);

    @Type.Int(32)
    int number();

    @Field.Setter("number")
    void setNumber(int number);

}
