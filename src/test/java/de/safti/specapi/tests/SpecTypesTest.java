package de.safti.specapi.tests;

import de.safti.specapi.tests.specs.BigIntSpec;
import de.safti.specapi.tests.specs.SpecWithAllTypes;
import de.safti.specs.SpecIO;
import de.safti.specs.annotations.Spec;
import de.safti.specs.io.BinaryData;
import de.safti.specs.layout.SpecLayout;
import de.safti.specs.layout.common.SpecField;
import de.safti.specs.utils.Checkers;
import de.safti.specs.utils.SpecPrinter;
import it.unimi.dsi.fastutil.bytes.ByteLists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import java.math.BigInteger;

import static de.safti.specs.utils.Debugs.*;

public class SpecTypesTest {
    // Long.MAX_VALUE * 4
    public static final BigInteger LARGE_BIG_INT = BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(4));


    public SpecWithAllTypes populatedSpec() {
        SpecWithAllTypes spec = SpecIO.generateEmpty(SpecWithAllTypes.class);

        // integers
        spec.setByteVal((byte) 123);
        spec.setShortVal((short) 12345);
        spec.setIntVal(123456789);
        spec.setLongVal(123456789012345L);


        // decimals
        spec.setFloatVal(123.45f);
        spec.setDoubleVal(12345.6789);


        // big numbers
        spec.setBigIntVal(LARGE_BIG_INT);

        // varints
        spec.setVarIntVal(123);
        spec.setUVarIntVal(12345);


        // strings
        spec.setStringTerminatedVal("Hello World!");
        spec.setStringFixedVal("1234567890");

        // dynamic strings
        spec.setStringDynamicVal("Dynamic String");
        spec.setExternalLengthField(3);
        spec.setStringDynamicExternalVal("abc");

        // enums
        spec.setEnumVal(SpecWithAllTypes.TestEnum.EIGHT);

        return spec;
    }

    @Testable
    public static void testEquality(Spec first, Spec second) throws AssertionError {
        SpecLayout layout = SpecIO.getLayout(first);
        SpecLayout layout2 = SpecIO.getLayout(second);
        Assertions.assertSame(layout2, layout);


        SpecField[] fields = layout.getFields();
        for (SpecField field : fields) {
            try {
                // run both getters and assert equal
                Object firstValue = field.getter().invoke(first);
                Object secondValue = field.getter().invoke(second);

                if(Checkers.unequal(firstValue, secondValue)) throw new AssertionError("Field %s do not have equal values! %s != %s".formatted(field.name(), toStringWClass(firstValue), toStringWClass(secondValue)));

            } catch (Throwable e) {
                if(e instanceof AssertionError) throw (AssertionError) e;

                throw new RuntimeException("Failed to get field " + field.name(), e);
            }
        }

    }

    public SpecWithAllTypes writeReadCopy(SpecWithAllTypes spec) {
        BinaryData data = SpecIO.write(spec);
        return (SpecWithAllTypes) SpecIO.read(data, SpecWithAllTypes.class);
    }

    @Test
    public void testAllTypesIO() {
        // make a spec, serialize it, read it back in, compare it and assert it's equal
        SpecWithAllTypes spec = populatedSpec();
        SpecWithAllTypes readSpec = writeReadCopy(spec);

        Assertions.assertDoesNotThrow(() -> testEquality(spec, readSpec));
    }


    @Test
    public void testTestEquality() {
        // tests if the testEquality method works by supplying a wrong value and asserting that it throws

        SpecWithAllTypes spec = populatedSpec();
        SpecWithAllTypes readSpec = writeReadCopy(spec);
        readSpec.setIntVal(123);

        Assertions.assertThrows(AssertionError.class, () -> testEquality(spec, readSpec), "Field intVal do not have equal values!");
    }

    @Test
    public void testBigIntSpec() {
        // separate test for BigInteger because it seems to cause issues often
        BigIntSpec spec = SpecIO.generateEmpty(BigIntSpec.class);
        spec.setBigInt(LARGE_BIG_INT);

        BinaryData data = SpecIO.write(spec);

        Assertions.assertEquals(16, data.array().length);

        BigIntSpec read = (BigIntSpec) SpecIO.read(data, BigIntSpec.class);
        Assertions.assertEquals(spec.bigInt(), read.bigInt());


    }
}
