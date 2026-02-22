package dev.goldmensch.propane.internal.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// If the method returns it eclosing class and is annotated with this annotation, the generated wrapping class
/// will override this method with the correct wrapped class as the return type. The method just calls its super one.
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Specialized {
}
