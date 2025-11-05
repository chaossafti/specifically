package de.safti.specs.annotations;

import de.safti.specs.layout.InterfaceLayout;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes should not be manually annotated with this annotation.
 * Runtime generated methods are marked with this annotation.
 * @see InterfaceLayout
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface InterfaceSpecImpl {

    Class<? extends Spec> specClass();

}
