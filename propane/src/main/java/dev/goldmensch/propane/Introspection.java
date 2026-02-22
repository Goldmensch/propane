package dev.goldmensch.propane;

import dev.goldmensch.propane.internal.exposed.Properties;
import dev.goldmensch.propane.internal.Resolver;
import dev.goldmensch.propane.internal.ScopeStub;
import dev.goldmensch.propane.internal.Scopes;
import dev.goldmensch.propane.internal.annotation.Specialized;
import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.property.SpecificProperty;

public abstract class Introspection {
    private final Property.Scope scope;
    private final Resolver resolver;

    // called by Builder#build
    private Introspection(Property.Scope scope, Properties properties, Introspection parent) {
        this.scope = scope;
        this.resolver = parent.resolver.createChild(properties, this);
    }

    // called by create(Scope)
    private Introspection() {
        this.scope = ScopeStub.INSTANCE;
        this.resolver = Resolver.EMPTY;
    }

    // --- must be added by annotation processor
//    public static Builder create(Property.Scope scope) {
//        return EMPTY.createChild(scope);
//    }

    // overridden with real SpecificProperty implementation
    @SuppressWarnings("unchecked")
    public <T> T get(SpecificProperty<T> specific) {
        Property<T> property = (Property<T>) specific;
        Property.Scope propertyScope = property.scope();
        if (!Scopes.isChild(propertyScope, scope)) {
            throw new RuntimeException("scope (%s) of property (%s) isn't child of or equal to introspection scope %s".formatted(propertyScope, property.name(), scope));
        }

        return resolver.get(property).orElseThrow();
    }

    // body:
    // return this.new Builder(scope);
    // overridden with real Builder implementation
    public abstract Builder createChild(Property.Scope scope);

    public abstract class Builder {
        private final Properties properties;
        private final Property.Scope scope;

        private Builder(Property.Scope scope) {
            this.scope = scope;
            this.properties = new Properties(scope);
        }

        @Specialized
        public Builder add(PropertyProvider<?> provider) {
            properties.add(provider);
            return this;
        }

        private void validate() {
            if (!Scopes.isChild(scope, Introspection.this.scope)) {
                throw new RuntimeException("Child scope must be equal or subscope of parent scope");
            }
        }

        // body:
        // validate()
        // return new Introspection(scope, properties, Introspection.this);
        // overridden with real Introspection instance
        public abstract Introspection build();
    }
}
