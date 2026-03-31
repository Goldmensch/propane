package dev.goldmensch.propane;

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
/// It's important to note, that only classes having methods throwing [SkeletonMethodException] will
/// be marked as [@Skeleton][Skeleton]. That doesn't mean, that there aren't specific implementations of classes
/// not annotated with [@Skeleton][Skeleton], which should be used instead.
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Skeleton {
}
