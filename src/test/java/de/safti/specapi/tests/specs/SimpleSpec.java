package de.safti.specapi.tests.specs;

import de.safti.specs.annotations.Spec;
import de.safti.specs.annotations.Type;

import java.util.Objects;

public class SimpleSpec implements Spec {

    public SimpleSpec() {
    }

    @Type.StringTerminated()
    public String name;

    @Type.Int(32)
    public int number;

    @Override
    public String toString() {
        return "SimpleSpec{" +
                "name='" + name + '\'' +
                ", number=" + number +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || getClass() != o.getClass()) return false;
        SimpleSpec that = (SimpleSpec) o;
        return number == that.number && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, number);
    }
}
