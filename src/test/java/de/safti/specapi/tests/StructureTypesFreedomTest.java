package de.safti.specapi.tests;

import de.safti.specapi.tests.specs.StructureTypeFreedomSpec;
import de.safti.specs.SpecIO;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import org.junit.jupiter.api.Test;

public class StructureTypesFreedomTest {

    private void populate(StructureTypeFreedomSpec spec) {
        Setter[] setters = new Setter[] {
                spec::setListInteger,
                spec::setListInteger,
                spec::setCollectingInteger,
                spec::setIntList,
        };

        for (Setter setter : setters) {
            IntList list = new IntArrayList();
            setter.set(list);
        }

        spec.setIntListUnmodifiable(IntLists.unmodifiable(new IntArrayList()));
        spec.setIntArrayList(new IntArrayList());
    }

    @Test
    public void test() {
        StructureTypeFreedomSpec spec = SpecIO.generate(StructureTypeFreedomSpec.class);
        populate(spec);


    }


    interface Setter {
        void set(IntList list);
    }

}
