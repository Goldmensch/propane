package dev.goldmensch.propane;

import dev.goldmensch.propane.event.Event;
import dev.goldmensch.propane.property.Property;

import java.util.Map;

public class Registry<S extends Property.Scope> {
    private final Map<Class<? extends Event<S>>, S> eventScopes;

    public Registry(Map<Class<? extends Event<S>>, S> eventScopes) {
        this.eventScopes = eventScopes;
    }

    public S scopeForEvent(Class<? extends Event<S>> event) {
        return eventScopes.get(event);
    }
}
