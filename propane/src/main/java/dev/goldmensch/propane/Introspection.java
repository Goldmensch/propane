package dev.goldmensch.propane;

import dev.goldmensch.propane.internal.Properties;
import dev.goldmensch.propane.internal.Resolver;
import dev.goldmensch.propane.internal.ScopeStub;
import dev.goldmensch.propane.internal.Scopes;

public class Introspection {

    public static final Introspection EMPTY = new Introspection();

    private final Property.Scope scope;
    private final Resolver resolver;

    private Introspection(Property.Scope scope, Properties properties, Introspection parent) {
        this.scope = scope;
        this.resolver = parent.resolver.createChild(properties, this);
    }

    private Introspection() {
        this.scope = ScopeStub.INSTANCE;
        this.resolver = Resolver.EMPTY;
    }

    public static Builder create(Property.Scope scope) {
        return EMPTY.createChild(scope);
    }

    public <T> T get(Property<T> property) {
        Property.Scope propertyScope = property.scope();
        if (!Scopes.isChild(propertyScope, scope)) {
            throw new RuntimeException("scope (%s) of property (%s) isn't child of or equal to introspection scope %s".formatted(propertyScope, property.name(), scope));
        }

        return resolver.get(property).orElseThrow();
    }

    public Builder createChild(Property.Scope scope) {
        return this.new Builder(scope); // create non-static inner class
    }

    public class Builder {
        private final Properties properties;
        private final Property.Scope scope;

        private Builder(Property.Scope scope) {
            this.scope = scope;
            this.properties = new Properties(scope);
        }

        public Builder add(PropertyProvider<?> provider) {
            properties.add(provider);
            return this;
        }

        public Introspection build() {
            if (!Scopes.isChild(scope, Introspection.this.scope)) {
                throw new RuntimeException("Child scope must be equal or subscope of parent scope");
            }

            return new Introspection(scope, properties, Introspection.this);
        }
    }
}
