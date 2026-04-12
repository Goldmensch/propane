package dev.goldmensch.propane;

import dev.goldmensch.propane.event.Event;
import dev.goldmensch.propane.event.Listener;
import dev.goldmensch.propane.property.*;
import dev.goldmensch.propane.property.PropertyProviderSkeleton;
import dev.goldmensch.propane.spec.SkeletonMethod;
import dev.goldmensch.propane.spec.SkeletonMethodException;


/// The [`Introspection`][IntrospectionSkeleton] type is the central element of Propane.
/// Its purpose is to expose the property and event system to the user.
///
/// Each [`Introspection`][IntrospectionSkeleton] instance is bound to a [Scope], allowing accessing the scopes' and its parents'
/// [Properties][Property]. For more information visit the documentation of [Scope]. Unless
/// the root scopes' [`Introspection`][IntrospectionSkeleton] instance, each [`Introspection`][IntrospectionSkeleton] instance is child of another, inheriting its values
/// that are combines with its own. For more information, visit the section [below](#properties)
///
/// ## Properties
/// An instance of [`Introspection`][IntrospectionSkeleton] can hold [`PropertyProvider`][PropertyProviderSkeleton]s for all [properties][Property]
/// accessible by its [Scope]. An introspection instance
/// will first compute its own value using the providers registered at it and possibly combine it with the
/// values from the introspection instances' parent(s). For more information on how this is done, visit the documentation of
/// [`SingletonProperty`][SingletonPropertySkeleton], [`MappingProperty`][MappingPropertySkeleton] and [`EnumerationProperty`][EnumerationPropertySkeleton].
///
/// After [computing][PropertyProviderSkeleton#supplier()] a value, it will be cached for the lifetime of that introspection instance.
/// Accessing such an instance is threadsafe, and it is guaranteed to always return the same instance.
///
/// TODO: docs (scoped access)
///
/// ## Listeners
/// A [Listener] registered on an [`Introspection`][IntrospectionSkeleton] instance is stored for the lifetime of this
/// instance. It will be called if the event it is registered for, is either fired in this introspection instance itself
/// or any children of it.
///
/// ```java
/// IntrospectionSkeleton A = ...;
/// IntrospectionSkeleton B with B is children of A
///
/// A.subscribe(FooEvent.class, _ -> System.out.println("Foo fired"));
///
/// publish FooEvent in A -> "Foo fired" printed
/// publish FooEvent in B -> "Foo fired" printed
///
/// // ---------------------------------------
/// B.subsribe(BarEvent.class, _ -> System.out.println("Bar fired"));
///
/// publish BarEvent in A -> nothing printed
/// publish BarEvent in B -> "Bar fired" printed
///
/// ```
/// @see SpecificProperty why you have to use the "specific" version of this class
@Skeleton
public interface IntrospectionSkeleton<SELF extends IntrospectionSkeleton<SELF, S>, S extends Scope> {

    /// TODO: docs (scoped access)
    @SkeletonMethod
    static boolean accessible() {
        // return IntrospectionImplSkeleton.INTROSPECTION.isBound();
        throw new SkeletonMethodException();
    }

    /// TODO: docs (scoped access)
    @SkeletonMethod
    static IntrospectionSkeleton<?, ?> accessScoped() {
        // return IntrospectionImplSkeleton.INTROSPECTION.get();
        throw new SkeletonMethodException();
    }


    /// TODO: docs (scoped access)
    @SkeletonMethod
    static <T> T scopedGet(SpecificProperty<T> property) {
        //  return accessScoped().get(property);
        throw new SkeletonMethodException();
    }

    /// Returns the value for the requested property by either retrieving it from the cache
    /// or computing it according to the [class' documentation][IntrospectionSkeleton].
    ///
    /// @param specific the requested property
    /// @return the value of the requested property
    /// @see IntrospectionSkeleton Introspections' class documentation
    @SkeletonMethod
    <T> T get(SpecificProperty<T> specific);

    /// Subscribes to an [event][Event] with the given [Listener].
    /// The provided listener will be stored in this instance, thus be available for the lifetime
    /// of this introspection instance.
    ///
    /// @param listener the [Listener] to be registered
    /// @see IntrospectionSkeleton Introspections' class documentation
    void subscribe(Listener<? extends Event<S>, S, SELF> listener);

    /// Returns the [Scope] this introspection instance is bound to.
    ///
    /// @return the [Scope] of this introspection instance
    S scope();
}
