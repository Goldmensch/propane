package dev.goldmensch.propane.internal;

import dev.goldmensch.propane.Property;
import dev.goldmensch.propane.PropertyProvider;

import java.util.*;

public class Properties {
    private final Map<Property<?>, List<PropertyProvider<?>>> providers = new HashMap<>();

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
