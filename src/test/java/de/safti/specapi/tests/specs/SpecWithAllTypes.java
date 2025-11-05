package de.safti.specapi.tests.specs;

import de.safti.specs.annotations.Field;
import de.safti.specs.annotations.Spec;
import de.safti.specs.annotations.Type;
import de.safti.specs.layout.common.SpecField;

import java.math.BigInteger;

public interface SpecWithAllTypes extends Spec {

    /*
     * GETTERS / DEFINITION
     */


    // --- numbers (integers) ---

    @Type.Int(8)
    byte byteVal();

    @Type.Int(16)
    short shortVal();

    @Type.Int(32)
    int intVal();

    // TODO: check comment below, no idea if that's true
    @Type.Int(63) // must be 63 because 64th bit is the sign
    long longVal();

    // --- numbers (decimals) ---

    @Type.Float
    float floatVal();

    @Type.Double
    double doubleVal();

    // --- numbers (large) ---

    @Type.Int(128)
    BigInteger bigIntVal();

    // TODO: BigDecimal

    // --- numbers (varints) ---

    @Type.VarInt
    long varIntVal();

    @Type.UVarInt
    long uVarIntVal();


    // --- strings ---

    @Type.StringTerminated()
    String stringTerminatedVal();

    @Type.Int(32)
    int externalLengthField();

    @Type.StringDynamic("externalLengthField")
    String stringDynamicExternalVal();

    @Type.StringDynamic("@auto")
    String stringDynamicVal();

    @Type.StringFixed(10)
    String stringFixedVal();

    // --- enums ---
    @Type.Enum(TestEnum.class)
    TestEnum enumVal();



    enum TestEnum {
        ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN
    }


    /*
     * SETTERS
     */

    @Field.Setter("byteVal")
    void setByteVal(byte val);

    @Field.Setter("shortVal")
    void setShortVal(short val);

    @Field.Setter("intVal")
    void setIntVal(int val);

    @Field.Setter("longVal")
    void setLongVal(long val);

    @Field.Setter("floatVal")
    void setFloatVal(float val);

    @Field.Setter("doubleVal")
    void setDoubleVal(double val);

    @Field.Setter("bigIntVal")
    void setBigIntVal(BigInteger l);

    @Field.Setter("stringTerminatedVal")
    void setStringTerminatedVal(String val);

    @Field.Setter("stringDynamicVal")
    void setStringDynamicVal(String val);

    @Field.Setter("stringFixedVal")
    void setStringFixedVal(String val);

    @Field.Setter("externalLengthField")
    void setExternalLengthField(int val);

    @Field.Setter("stringDynamicExternalVal")
    void setStringDynamicExternalVal(String val);

    @Field.Setter("varIntVal")
    void setVarIntVal(long val);

    @Field.Setter("uVarIntVal")
    void setUVarIntVal(long val);

    @Field.Setter("enumVal")
    void setEnumVal(TestEnum val);


    /*
     * OTHER
     */


    @Field.Leaker("@all")
    SpecField[] fields();

}
