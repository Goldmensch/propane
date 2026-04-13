package dev.goldmensch.propane.event;

import dev.goldmensch.propane.IntrospectionSkeleton;
import dev.goldmensch.propane.Scope;
import dev.goldmensch.propane.event.internal.EventBus;

/// A [Subscription] is the 'identifier' of a registered [Listener]. It allows
/// to `unsubscribe` this particular [Listener] from the introspection instance it is registered on.
///
/// @see IntrospectionSkeleton#subscribe(Listener)
public class Subscription<I extends IntrospectionSkeleton<I, S>, S extends Scope> {
    private final Listener<?, S, I> listener;
    private final EventBus<I, S> eventBus;

    public Subscription(Listener<?, S, I> listener, EventBus<I, S> eventBus) {
        this.listener = listener;
        this.eventBus = eventBus;
    }


    /// Unregisters this particular [Listener] from the [`Introspection`][IntrospectionSkeleton] instance
    /// it is registered on.
    public void unsubscribe() {
        eventBus.remove(listener);
    }
}
