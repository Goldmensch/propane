package dev.goldmensch.propane;

import dev.goldmensch.propane.property.SpecificProperty;
import dev.goldmensch.propane.spec.SkeletonMethodException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Classes annotated with this annotation are called "skeleton classes".
/// Such classes shouldn't be used by users of Propane directly in most cases,
/// as static and some instance methods aren't implemented and will throw [SkeletonMethodException].
///
/// Functional implementations of such methods are provided by the specific implementation
/// of such a skeleton class.
///
/// Fore more information take a look at the Javadocs of [SpecificProperty].
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Skeleton {
}
