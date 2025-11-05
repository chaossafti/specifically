package de.safti.specapi.tests.specs;

import de.safti.specs.annotations.Field;
import de.safti.specs.annotations.Spec;
import de.safti.specs.annotations.Type;

import java.math.BigInteger;

public interface BigIntSpec extends Spec {

    @Type.Int(128)
    BigInteger bigInt();

    @Field.Setter("bigInt")
    void setBigInt(BigInteger bigInt);

}
