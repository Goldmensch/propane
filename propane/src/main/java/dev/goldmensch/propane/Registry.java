package dev.goldmensch.propane;

import dev.goldmensch.propane.event.Event;

import java.util.Map;

/// The [Registry] is used to store metadata gathered during compile-time.
///
/// For example, it maps event [Class]es to their [Scope]s.
///
/// The instance of [Registry] must always be accessible by a `static final` singleton
/// and its data must be constant during runtime.
public class Registry<S extends Scope> {
    private final Map<Class<? extends Event<S>>, S> eventScopes;

    public Registry(Map<Class<? extends Event<S>>, S> eventScopes) {
        this.eventScopes = eventScopes;
    }

    public S scopeForEvent(Class<? extends Event<S>> event) {
        S scope = eventScopes.get(event);

        if (scope == null) {
            throw new RuntimeException("No scope found for event %s. Most likely it's falsely missing from the registry implementation.");
        }

        return scope;
    }
}
