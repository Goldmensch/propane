package dev.goldmensch.propane.internal;

import dev.goldmensch.propane.Property;
import dev.goldmensch.propane.PropertyProvider;

import java.util.*;

public class Properties {
    private final Property.Scope scope;
    private final Map<Property<?>, List<PropertyProvider<?>>> providers = new HashMap<>();

    public Properties(Property.Scope scope) {
        this.scope = scope;
    }

    /// validates property and provider invariants
    private void validate(PropertyProvider<?> provider) {
        PropertyProvider.Priority priority = provider.priority();
        Property<?> property = provider.property();
        Property.Source source = property.source();

        if (property.scope().priority() < 0) {
            throw new RuntimeException("priority of scope can't be negative");
        }

        if (source == Property.Source.PROVIDED) {
            if (priority != PropertyProvider.Priority.FALLBACK) {
                throw new RuntimeException("provided property provider must always be priority = fallback");
            }

            if (property instanceof Property.MultiValue<?> val && val.fallbackBehaviour() != Property.FallbackBehaviour.ACCUMULATE) {
                throw new RuntimeException("provided multi value property (collection/map) must have fallbackBehaviour set to accumulate");
            }
        }

        Property.Scope propertyScope = property.scope();
        if (!Scopes.isChild(scope, propertyScope)) {
            throw new RuntimeException("scope of property (%s) must be equal or parent of current scope (%s)".formatted(propertyScope, scope));
        }
    }

    public void add(PropertyProvider<?> provider) {
        validate(provider);

        list(provider.property()).addFirst(provider);
    }

    private List<PropertyProvider<?>> list(Property<?> property) {
        return providers.computeIfAbsent(property, _ -> new LinkedList<>());
    }

    // sorts and returns a copy of the providers
    Map<Property<?>, List<PropertyProvider<?>>> providers() {
        return deepImmutableCopySorted(providers);
    }

    private static Map<Property<?>, List<PropertyProvider<?>>> deepImmutableCopySorted(Map<Property<?>, List<PropertyProvider<?>>> oldMap) {
        Map<Property<?>, List<PropertyProvider<?>>> newMap = new HashMap<>();
        oldMap.forEach((property, providers) -> {
            LinkedList<PropertyProvider<?>> copy = new LinkedList<>(providers);
            copy.sort(Comparator.comparing(PropertyProvider::priority));

            newMap.put(property, Collections.unmodifiableList(copy));
        });

        return Collections.unmodifiableMap(newMap);
    }
}
