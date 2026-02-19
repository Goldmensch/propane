package dev.goldmensch.propane.internal;

import dev.goldmensch.propane.Property;
import dev.goldmensch.propane.PropertyProvider;

import java.util.*;

public class Properties {
    private final Map<Property<?>, SortedSet<PropertyProvider<?>>> providers = new HashMap<>();

    public void add(PropertyProvider<?> provider) {
        list(provider.property()).add(provider);
    }

    private SortedSet<PropertyProvider<?>> list(Property<?> property) {
        return providers.computeIfAbsent(property, _ -> newSet());
    }

    private SortedSet<PropertyProvider<?>> newSet() {
        return new TreeSet<>(Comparator.<PropertyProvider<?>>comparingInt(PropertyProvider::priority).reversed());
    }

    public Resolver createResolver() {
        return new Resolver(providers);
    }

}
