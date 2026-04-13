package dev.goldmensch.propane;

import dev.goldmensch.propane.event.Event;
import dev.goldmensch.propane.event.Listener;
import dev.goldmensch.propane.event.Subscription;
import dev.goldmensch.propane.property.*;
import dev.goldmensch.propane.property.PropertyProviderSkeleton;
import dev.goldmensch.propane.spec.SkeletonMethod;
import dev.goldmensch.propane.spec.SkeletonMethodException;

import java.util.NoSuchElementException;


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
/// ### Scopes access
/// Beside using [#get(SpecificProperty)] directly on an introspection instance, you can sometimes utilize
/// Java's [ScopedValue] API to get a properties value. For that to work, the [`Introspection`][IntrospectionSkeleton] class
/// features multiple static methods like [#scopedGet(SpecificProperty)]. Methods using this scopes system, are always
/// prefixed with "scoped".
///
/// For further understanding, it is helpful to know how the [ScopedValue] API works.
/// In short: The [ScopedValue] API allows data to be available in a "context" whether only by field.
/// A context is for example the callchain in which you are, take following example:
///
/// ```java
/// static ScopedValue<MyIntrospection> VAL = ScopedValue.newInstance();
///
/// void one() {
///     inner();
/// }
///
/// void two() {
///     MyIntrospection introspection = VAL.get();
///     // the call above will fail
/// }
///
/// void inner() {
///     MyIntrospection introspection = VAL.get();
///     // use the introspection.instance()
/// }
///
/// void main() {
///     MyIntrospection introspection = MyIntrospectionImpl.create(...)
///                                         ...
///                                         .build();
///
///     // calls "one" with the ScopedValue "VAL" set to the introspection variable
///     ScopedValue.where(VAL, introspection).run(() -> one());
///
///     two();
/// }
/// ```
///
/// The value for "VAL" is set for all method calls inside the lambda of `run(() -> one())`. This means
/// that it's accessible inside "one", "inner" and any subsequent method call, but not inside `two` because
/// it is called outside of `run()`.
///
///
/// The [`Introspection`][IntrospectionSkeleton] and [`IntrospectionImpl`][IntrospectionImplSkeleton] classes
/// implement the above logic natively, covering up the underlying [ScopedValue]. Instead of [ScopedValue#get()]
/// you can use [`MyIntrospection#getScoped(MY_PROPERTY)`][IntrospectionSkeleton#scopedGet(SpecificProperty)].
///
/// Please note, that the availability of scoped access to introspection instances vary widely and is different for
/// places inside the callchain. Each library decide whether and how to support them, so please take a look at the libraries
/// documentation on where you can use it and where not.
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

    /// Whether scoped access to the introspection instance (by calling `scopedGet`)
    /// is possible.
    ///
    /// @return whether scoped access is possible
    @SkeletonMethod
    static boolean accessible() {
        // return IntrospectionImplSkeleton.INTROSPECTION.isBound();
        throw new SkeletonMethodException();
    }

    /// Returns the introspection instance set via [ScopedValue] if set, else throws
    /// [NoSuchElementException].
    ///
    /// @return the introspection instance of this scope
    /// @see ScopedValue#get()
    @SkeletonMethod
    static IntrospectionSkeleton<?, ?> accessScoped() {
        // return IntrospectionImplSkeleton.INTROSPECTION.get();
        throw new SkeletonMethodException();
    }


    /// Shorthand for `accessScoped().get(property)`. Throws [NoSuchElementException]
    /// if [#accessible()] returns `false`.
    ///
    /// @param property the property to get
    /// @return the value for the property
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
    /// @return the [Subscription] identifying this registered listener
    /// @see IntrospectionSkeleton Introspections' class documentation
    Subscription<SELF, S> subscribe(Listener<? extends Event<S>, S, SELF> listener);

    /// Returns the [Scope] this introspection instance is bound to.
    ///
    /// @return the [Scope] of this introspection instance
    S scope();
}
