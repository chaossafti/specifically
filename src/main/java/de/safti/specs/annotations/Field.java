package de.safti.specs.annotations;

import de.safti.specs.layout.common.SpecField;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class Field {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Setter {
        /**
         * @return The spec field name.
         */
        String value();

    }

    /**
     * Annotated methods will expose the internal {@link SpecField} instance.
     * You may provide {@code @all} as value and request a {@code SpecField[]} as setType to leak all fields.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    public @interface Leaker {

        /**
         * You may provide {@code @all} to this value and set the spec field setType to {@code SpecField[]} to leak all fields.
         *
         * @return The spec field name.
         */
        String value();
    }



}
