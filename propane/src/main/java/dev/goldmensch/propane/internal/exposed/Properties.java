package dev.goldmensch.propane.internal.exposed;

import dev.goldmensch.propane.IntrospectionSkeleton;
import dev.goldmensch.propane.Scope;
import dev.goldmensch.propane.internal.Scopes;
import dev.goldmensch.propane.property.Priority;
import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.property.PropertyProviderSkeleton;

import java.util.*;

public class Properties<INTROSPECTION extends IntrospectionSkeleton<INTROSPECTION, ?>> {
    private final Scope scope;
    private final Map<Property<?>, List<PropertyProviderSkeleton<?, ?, INTROSPECTION>>> providers = new HashMap<>();

    public Properties(Scope scope) {
        this.scope = scope;
    }

    /// validates property and provider invariants
    private void validate(PropertyProviderSkeleton<?, ?, ?> provider) {
        Priority priority = provider.priority();
        Property<?> property = provider.property().generalized();
        Property.Source source = property.source();

        if (property.scope().priority() < 0) {
            throw new RuntimeException("priority of scope can't be negative");
        }

        if (source == Property.Source.PROVIDED) {
            if (priority != Priority.FALLBACK) {
                throw new RuntimeException("provided property provider must always be priority = fallback");
            }

            if (property instanceof Property.MultiValue<?> val && val.fallbackStrategy() != Property.FallbackStrategy.COMBINE) {
                throw new RuntimeException("provided multi value property (collection/map) must have fallbackStrategy set to accumulate");
            }
        }

        if (source == Property.Source.BUILDER) {
            if (priority != Priority.BUILDER && priority != Priority.FALLBACK) {
                throw new RuntimeException("builder property provider must always be priority = builder or fallback");
            }
        }

        Scope propertyScope = property.scope();
        if (!Scopes.isSub(scope, propertyScope)) {
            throw new RuntimeException("scope of property (%s) must be equal or parent of current scope (%s)".formatted(propertyScope, scope));
        }
    }

    public void add(PropertyProviderSkeleton<?, ?, INTROSPECTION> provider) {
        validate(provider);

        list(provider.property().generalized()).addFirst(provider);
    }

    private List<PropertyProviderSkeleton<?, ?, INTROSPECTION>> list(Property<?> property) {
        return providers.computeIfAbsent(property, _ -> new LinkedList<>());
    }

    // exposes the providers
    public Map<Property<?>, List<PropertyProviderSkeleton<?, ?, INTROSPECTION>>> providers() {
        return providers;
    }
}
