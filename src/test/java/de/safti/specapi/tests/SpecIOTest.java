package de.safti.specapi.tests;

import de.safti.specapi.tests.specs.InterfaceSpec;
import de.safti.specapi.tests.specs.SimpleSpec;
import de.safti.specs.SpecIO;
import de.safti.specs.annotations.Spec;
import de.safti.specs.io.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

public class SpecIOTest {

    @Testable
    private void testReadWrite(Spec spec, Class<? extends Spec> specClass) {
        BinaryData data = SpecIO.write(spec);

        Spec read = SpecIO.read(data, specClass);
        Assertions.assertEquals(read, spec);
    }

    @Test
    public void readWriteClassSpecTest() {
        SimpleSpec simpleSpec = new SimpleSpec();
        simpleSpec.name = "myname";
        simpleSpec.number = 123;

        testReadWrite(simpleSpec, SimpleSpec.class);
    }

    @Test
    public void readWriteInterfaceSpecTest() {
        InterfaceSpec spec = SpecIO.generateEmpty(InterfaceSpec.class);

        // when using generate every field is initialized with null (or primitive default)
        Assertions.assertNull(spec.name());
        Assertions.assertEquals(0, spec.number());

        // setterMethod methods work
        spec.setName("safti");
        spec.setNumber(67);

        // getter methods work
        Assertions.assertEquals("safti", spec.name());
        Assertions.assertEquals(67, spec.number());

        testReadWrite(spec, InterfaceSpec.class);
    }

}
