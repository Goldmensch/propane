package dev.goldmensch.propane.event.internal;

import dev.goldmensch.propane.IntrospectionSkeleton;
import dev.goldmensch.propane.Registry;
import dev.goldmensch.propane.Scope;
import dev.goldmensch.propane.event.Event;
import dev.goldmensch.propane.event.Listener;
import dev.goldmensch.propane.internal.Scopes;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EventBus<I extends IntrospectionSkeleton<I, S>, S extends Scope> {
    private final Registry<S> registry;
    private final S scope;
    private final @Nullable EventBus<I, S> parent;
    private final Map<Class<Event<S>>, Collection<Listener<Event<S>, S, I>>> listeners = new ConcurrentHashMap<>();

    public EventBus(Registry<S> registry, S scope, @Nullable EventBus<I, S> parent) {
        this.registry = registry;
        this.scope = scope;
        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
    public void add(Listener<? extends Event<S>, S, I> listener) {
        Class<? extends Event<S>> event = listener.event();
        S eventScope = registry.scopeForEvent(event);

        if (!Scopes.isSub(eventScope, scope)) {
            throw new RuntimeException("scope of event listener must be child of current scope");
        }

        var list = listeners.computeIfAbsent((Class<Event<S>>) event, _ -> new LinkedList<>());
        list.add((Listener<Event<S>, S, I>) listener);
    }

    public void publish(Event<S> event, I introspection) {
        if (!Scopes.isSame(event.scope(), scope)) {
            throw new RuntimeException("event scope must be current scope");
        }

        call(event, introspection);
    }

    private void call(Event<S> event, I introspection) {
        for (Listener<Event<S>, S, I> listener : listeners.getOrDefault(event.getClass(), List.of())) {
            listener.accept(event, introspection);
        }

        if (parent != null) {
            parent.call(event, introspection);
        }
    }
}
