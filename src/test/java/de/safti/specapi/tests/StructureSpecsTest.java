package de.safti.specapi.tests;

import de.safti.specapi.tests.specs.StructureSpec;
import de.safti.specs.SpecIO;
import de.safti.specs.annotations.Spec;
import de.safti.specs.io.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;


import org.junit.platform.commons.annotation.Testable;

public class StructureSpecsTest {

    public StructureSpec populatedSpec() {
        StructureSpec spec = SpecIO.generate(StructureSpec.class);

        // arrays
        int[] ints = spec.ints();
        Assertions.assertEquals(10, ints.length, "1D array length must be 10");
        for (int i = 0; i < ints.length; i++) {
            ints[i] = i;
        }
        for (int i = 0; i < 10; i++) {
            Assertions.assertEquals(i, ints[i], "ints[" + i + "] mismatch");
        }

        // 2D array: 10 × 9
        int[][] ints2d = spec.ints2d();
        Assertions.assertEquals(10, ints2d.length, "2D outer dimension must be 10");
        for (int i = 0; i < ints2d.length; i++) {
            Assertions.assertNotNull(ints2d[i], "ints2d[" + i + "] must not be null");
            Assertions.assertEquals(9, ints2d[i].length, "ints2d[" + i + "] inner dimension must be 9");
            for (int j = 0; j < ints2d[i].length; j++) {
                ints2d[i][j] = i + j;
            }
        }
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 9; j++) {
                Assertions.assertEquals(i + j, ints2d[i][j], "ints2d[" + i + "][" + j + "] mismatch");
            }
        }

        // 3D array: 10 × 9 × 8
        int[][][] ints3d = spec.ints3d();
        Assertions.assertEquals(10, ints3d.length, "3D outer dimension must be 10");
        for (int i = 0; i < ints3d.length; i++) {
            Assertions.assertNotNull(ints3d[i], "ints3d[" + i + "] must not be null");
            Assertions.assertEquals(9, ints3d[i].length, "ints3d[" + i + "] second dimension must be 9");
            for (int j = 0; j < ints3d[i].length; j++) {
                Assertions.assertNotNull(ints3d[i][j], "ints3d[" + i + "][" + j + "] must not be null");
                Assertions.assertEquals(8, ints3d[i][j].length, "ints3d[" + i + "][" + j + "] third dimension must be 8");
                for (int k = 0; k < ints3d[i][j].length; k++) {
                    ints3d[i][j][k] = i + j + k;
                }
            }
        }
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 9; j++) {
                for (int k = 0; k < 8; k++) {
                    Assertions.assertEquals(i + j + k, ints3d[i][j][k],
                            "ints3d[" + i + "][" + j + "][" + k + "] mismatch");
                }
            }
        }

        // fixed list
        Assertions.assertNull(spec.intListFixed());
        List<Integer> intListFixed = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            intListFixed.add(i * 3);
        }
        spec.setIntListFixed(intListFixed);

        // dynamic list
        Assertions.assertNull(spec.intListDynamic());
        List<Integer> intListDynamic = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            intListDynamic.add(i * 3);
        }
        spec.setIntListDynamic(intListDynamic);

        // fixed set
        Assertions.assertNull(spec.intSetFixed());
        java.util.Set<Integer> intSetFixed = new java.util.HashSet<>();
        for (int i = 0; i < 10; i++) {
            intSetFixed.add(i * 5);
        }
        spec.setIntSetFixed(intSetFixed);

        // dynamic set
        Assertions.assertNull(spec.intSetDynamic());
        java.util.Set<Integer> intSetDynamic = new java.util.HashSet<>();
        for (int i = 0; i < 10; i++) {
            intSetDynamic.add(i * 5);
        }
        spec.setIntSetDynamic(intSetDynamic);


        return spec;
    }

    @Testable
    public static void testEquality(Spec first, Spec second) throws AssertionError {
        SpecTypesTest.testEquality(first, second);
    }

    public StructureSpec writeReadCopy(StructureSpec spec) {
        BinaryData data = SpecIO.write(spec);
        return (StructureSpec) SpecIO.read(data, StructureSpec.class);
    }

    @Test
    public void testStructureSpecIO() {
        // create and populate a single StructureSpec instance
        StructureSpec spec = populatedSpec();

        // write → read → compare
        StructureSpec readSpec = writeReadCopy(spec);
        Assertions.assertDoesNotThrow(() -> testEquality(spec, readSpec));
    }

}
