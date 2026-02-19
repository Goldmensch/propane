package dev.goldmensch.propane.internal;

import dev.goldmensch.propane.Property;
import dev.goldmensch.propane.PropertyProvider;

import java.util.*;

public class Properties {
    private final Map<Property<?>, SortedSet<PropertyProvider<?>>> providers = new HashMap<>();

    /// validates property and provider invariants
    private void validate(PropertyProvider<?> provider) {
        PropertyProvider.Priority priority = provider.priority();
        Property<?> property = provider.property();
        Property.Source source = property.source();

        if (source == Property.Source.PROVIDED) {
            if (priority != PropertyProvider.Priority.FALLBACK) {
                throw new RuntimeException("provided property provider must always be priority = fallback");
            }

            if (property instanceof Property.MultiValue<?> val && val.fallbackBehaviour() != Property.FallbackBehaviour.ACCUMULATE) {
                throw new RuntimeException("provided multi value property (collection/map) must have fallbackBehaviour set to accumulate");
            }
        }
    }

    public void add(PropertyProvider<?> provider) {
        validate(provider);

        list(provider.property()).add(provider);
    }

    private SortedSet<PropertyProvider<?>> list(Property<?> property) {
        return providers.computeIfAbsent(property, _ -> new TreeSet<>(Comparator.comparing((PropertyProvider<?> o) -> o.priority())));
    }

    Map<Property<?>, SortedSet<PropertyProvider<?>>> providers() {
        return providers;
    }
}
