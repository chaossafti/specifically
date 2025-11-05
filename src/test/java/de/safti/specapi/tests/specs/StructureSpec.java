package de.safti.specapi.tests.specs;

import de.safti.specs.annotations.Spec;
import de.safti.specs.annotations.Structure;
import de.safti.specs.annotations.Type;

public interface ArraySpec extends Spec {

    @Structure.ArrayFixed(10)
    @Type.Int(16)
    int[] ints();


}
