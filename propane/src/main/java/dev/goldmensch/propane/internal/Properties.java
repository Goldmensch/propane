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

    // properties should be added in the same order as they were in the to be added "properties"
    // because we use addFirst in add, we have to reverse the order here, so that the "last" provider is added first, and the first added last -> order is same as original
    // 1. [A: A1, A2], [B: B1, B2]  (as passed, original)
    // 2. B2, B1, A2, A1            (reversed)
    // 3. [A: A1, A2], [B: B1, B2]  (A2 then A1, but addFirst, so that order is as original)
    void addAll(Properties properties) {
        List<PropertyProvider<?>> reversed = properties.providers
                .values()
                .stream()
                .flatMap(List::stream)
                .toList().reversed();
        reversed.forEach(this::add);
    }


    private List<PropertyProvider<?>> list(Property<?> property) {
        return providers.computeIfAbsent(property, _ -> new LinkedList<>());
    }

    Map<Property<?>, List<PropertyProvider<?>>> providers() {
        providers.forEach((_, list) -> list.sort(Comparator.comparing(PropertyProvider::priority)));
        return providers;
    }
}
