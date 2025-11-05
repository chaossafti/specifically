package de.safti.specapi.tests.specs;

import de.safti.specs.annotations.*;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface StructureTypeFreedomSpec extends Spec {

    @Structure.ListFixed(5)
    @Type.Int(16)
    ArrayList<Integer> arrayListInteger();


    @Structure.ListFixed(5)
    @Type.Int(16)
    List<Integer> listInteger();

    @Structure.ListFixed(5)
    @Type.Int(16)
    Collection<Integer> collectingInteger();


    @Structure.ListFixed(value = 5, listType = ListType.FU_INT)
    @Type.Int(16)
    IntArrayList intArrayList();

    @Structure.ListFixed(value = 5, listType = ListType.FU_INT)
    @Type.Int(16)
    IntList intList();


    @Structure.ListFixed(value = 5, listType = ListType.FU_UNMODIFIABLE_INT)
    @Type.Int(16)
    IntList intListUnmodifiable();

    /*
     * SETTERS
     */


    @Field.Setter("arrayListInteger")
    void setArrayListInteger(ArrayList<Integer> list);

    @Field.Setter("listInteger")
    void setListInteger(List<Integer> list);

    @Field.Setter("collectingInteger")
    void setCollectingInteger(Collection<Integer> list);

    @Field.Setter("intArrayList")
    void setIntArrayList(IntArrayList list);

    @Field.Setter("intList")
    void setIntList(IntList list);

    @Field.Setter("intListUnmodifiable")
    void setIntListUnmodifiable(IntList list);




}
