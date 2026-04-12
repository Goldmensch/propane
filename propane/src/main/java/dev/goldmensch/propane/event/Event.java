package dev.goldmensch.propane.event;

import dev.goldmensch.propane.IntrospectionImplSkeleton;
import dev.goldmensch.propane.IntrospectionSkeleton;
import dev.goldmensch.propane.Scope;

/// An Event in general, is an occurrence of something during a libraries runtime.
/// For example, this can be a request or a certain action taking place.
///
/// In Propane each [Event] is bound to the [Scope] in which it occurs.
/// An event can only be fired from a [`Introspection`][IntrospectionSkeleton] instance with exactly the [scope][#scope]
/// of the that event.
///
/// For example, if we have an [Event] of scope `Scopes.CONFIGURATION`,
/// we can only use [IntrospectionImplSkeleton#publish(Event)] if [IntrospectionImplSkeleton#scope()] returns
/// `Scopes.CONFIGURATION` too.
///
/// @see Listener
@SuppressWarnings("unused")
public interface Event<S extends Scope> {
    S scope();

}
