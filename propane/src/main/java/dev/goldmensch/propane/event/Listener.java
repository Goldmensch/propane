package dev.goldmensch.propane.event;

import dev.goldmensch.propane.IntrospectionSkeleton;
import dev.goldmensch.propane.Scope;
import dev.goldmensch.propane.property.SpecificProperty;

import java.util.function.BiConsumer;

/// A [Listener] listens to the occurrence of a [certain][#event()] [Event].
///
/// Listeners are registered on [`Introspection`][IntrospectionSkeleton] instances and life as long as they do.
/// They are called when an event is fired, that matches the [one this listener is registered for][#event()]
/// via the [`Introspection`][IntrospectionSkeleton] instance this listener is registered on or any direct child instance of it.
/// Fore more information see the docs of [`Introspection`][IntrospectionSkeleton].
///
/// @see IntrospectionSkeleton
/// @see Event
public interface Listener<E extends Event<S>, S extends Scope, I extends IntrospectionSkeleton<I, S>> {

    /// Will be called if a [matching][#event()] event occurs.
    ///
    /// The provided instance of [`Introspection`][IntrospectionSkeleton] is the one used to publish the event. This means
    /// that all properties accessible by the [scope][Event#scope()] of the event, can be retrieved.
    ///
    /// Furthermore, in the context of this method (inside this method), scoped access to the given
    /// [`Introspection`][IntrospectionSkeleton] is available, that means that you can use a specific
    /// version of [IntrospectionSkeleton#scopedGet(SpecificProperty)] to get the values of properties.
    /// To learn more about scoped access, take a look [here][IntrospectionSkeleton].
    ///
    /// Additionally, implementors of this method must consider that the values returned from that
    /// [`Introspection`][IntrospectionSkeleton] instance may vary depending on where the [Event] is fired.
    ///
    /// @param event the fired [Event]
    /// @param introspection the [`Introspection`][IntrospectionSkeleton] instance used to fire the event.
    void accept(E event, I introspection);

    /// Specifies the event, this Listener should be called for.
    ///
    /// @return the [type][Class] of the event
    Class<E> event();

    /// Creates an [Listener] based on the passed [Event] and [BiConsumer] that will be called as [Listener#accept(Event, IntrospectionSkeleton)].
    ///
    /// This method is intended to be used inline with [IntrospectionSkeleton#subscribe(Listener)]:
    /// ```java
    /// IntrospectionSkeleton intro = ...
    /// intro.subscribe(Listener.create(FooEvent.class, (e, _) -> ...));
    /// ```
    ///
    /// @param event the [Event] that should trigger the [Listener] (see [#event()])
    /// @param acceptor body of [Listener#accept(Event, IntrospectionSkeleton)]
    ///
    /// @return the created [Listener] instance
    static <T extends Event<S>, S extends Scope, I extends IntrospectionSkeleton<I, S>> Listener<T, S, I> create(Class<T> event, BiConsumer<T, I> acceptor) {
        return new Listener<>() {
            @Override
            public void accept(T event, I introspection) {
                acceptor.accept(event, introspection);
            }

            @Override
            public Class<T> event() {
                return event;
            }
        };
    }
}
