package de.safti.specapi.tests;

import de.safti.specapi.tests.specs.OptionalSpec;
import de.safti.specs.SpecIO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.OptionalInt;

public class OptSpecTest {

    @Test
    public void test() {
        OptionalSpec spec = SpecIO.generate(OptionalSpec.class);

        // all null/empty by default
        Assertions.assertNull(spec.nullableString());
        Assertions.assertTrue(spec.optString().isEmpty());
        Assertions.assertTrue(spec.optInt().isEmpty());


        // populate spec
        spec.setNullableString("Hello World!");
        spec.setOptString(Optional.of("Hello World!"));
        spec.setOptInt(OptionalInt.of(42));

        // check
        Assertions.assertEquals("Hello World!", spec.nullableString());
        Assertions.assertEquals("Hello World!", spec.optString().get());
        Assertions.assertEquals(42, spec.optInt().getAsInt());
    }

}
