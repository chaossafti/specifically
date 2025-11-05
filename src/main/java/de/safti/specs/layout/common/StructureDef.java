package de.safti.specs.layout.common;

import org.jetbrains.annotations.NotNull;

/**
 * A structure is a TypeDef that holds multiple types.
 * An example of a structure is an array.
 */
public interface StructureDef extends TypeDef {

    @NotNull
    TypeDef inner();

}
