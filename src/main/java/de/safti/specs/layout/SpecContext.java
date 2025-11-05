package de.safti.specs.layout;

import de.safti.specs.annotations.Spec;
import de.safti.specs.io.BinaryReader;
import de.safti.specs.layout.common.SpecField;
import de.safti.specs.layout.common.TypeDef;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * Class used for providing context about the fields around the spec.
 * Used in {@link TypeDef#read(BinaryReader, SpecContext)}
 */
public class SpecContext {
    private final Spec spec;
    private final SpecLayout layout;

    public SpecContext(Spec spec, SpecLayout layout) {
        this.spec = spec;
        this.layout = layout;
    }


    @Nullable
    @Contract(pure = true)
    public Object getFieldValue(String fieldName) {
        SpecField field = layout.getField(fieldName);
        if(field == null) return null;
        try {
            return field.getter().invoke(spec);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to read from field " + field.name(), e);
        }


    }

}
