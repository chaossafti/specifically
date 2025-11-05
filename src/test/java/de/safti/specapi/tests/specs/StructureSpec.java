package de.safti.specapi.tests.specs;

import de.safti.specs.annotations.*;

import java.util.List;
import java.util.Set;

public interface StructureSpec extends Spec {

    /*
     * GETTERS
     */

    @Structure.ArrayFixed(10)
    @Type.Int(16)
    int[] ints();

    @Structure.ArrayFixed({10, 9})
    @Type.Int(16)
    int[][] ints2d();

    @Structure.ArrayFixed({10, 9, 8})
    @Type.Int(16)
    int[][][] ints3d();

    // lists

    @Structure.ListFixed(10)
    @Type.Int(16)
    List<Integer> intListFixed();

    @Structure.ListDynamic("@auto")
    @Type.Int(16)
    List<Integer> intListDynamic();

    // sets

    @Structure.SetFixed(10)
    @Type.Int(16)
    Set<Integer> intSetFixed();

    @Structure.SetDynamic("@auto")
    @Type.Int(16)
    Set<Integer> intSetDynamic();



    /*
     * SETTERS
     */


    @Field.Setter("ints")
    void setInts(int[] ints);

    @Field.Setter("intListFixed")
    void setIntListFixed(List<Integer> list);

    @Field.Setter("intListDynamic")
    void setIntListDynamic(List<Integer> list);

    @Field.Setter("intSetFixed")
    void setIntSetFixed(Set<Integer> set);

    @Field.Setter("intSetDynamic")
    void setIntSetDynamic(Set<Integer> set);

    @Field.Setter("ints2d")
    void setInts2d(int[][] ints);

    @Field.Setter("ints3d")
    void setInts3d(int[][][] ints);



}
