package dev.goldmensch.propane.internal.exposed;

import dev.goldmensch.propane.Introspection;
import dev.goldmensch.propane.internal.Scopes;
import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.PropertyProvider;
import dev.goldmensch.propane.property.SpecificProperty;

import java.util.*;

public class Properties<INTROSPECTION extends Introspection<?>> {
    private final Property.Scope scope;
    private final Map<Property<?>, List<PropertyProvider<?, ?, INTROSPECTION>>> providers = new HashMap<>();

    public Properties(Property.Scope scope) {
        this.scope = scope;
    }

    /// validates property and provider invariants
    private void validate(PropertyProvider<?, ?, ?> provider) {
        PropertyProvider.Priority priority = provider.priority();
        Property<?> property = provider.property().generalized();
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

    public void add(PropertyProvider<?, ?, INTROSPECTION> provider) {
        validate(provider);

        list(provider.property().generalized()).addFirst(provider);
    }

    private List<PropertyProvider<?, ?, INTROSPECTION>> list(Property<?> property) {
        return providers.computeIfAbsent(property, _ -> new LinkedList<>());
    }

    // exposes the providers
    public Map<Property<?>, List<PropertyProvider<?, ?, INTROSPECTION>>> providers() {
        return providers;
    }
}
