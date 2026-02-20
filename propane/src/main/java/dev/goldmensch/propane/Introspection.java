package dev.goldmensch.propane;

import dev.goldmensch.propane.internal.Properties;
import dev.goldmensch.propane.internal.Resolver;
import dev.goldmensch.propane.internal.ScopeStub;
import dev.goldmensch.propane.internal.Scopes;

public class Introspection {

    private final Property.Scope scope;
    private final Resolver resolver;

    private Introspection(Property.Scope scope, Resolver resolver) {
        this.scope = scope;
        this.resolver = resolver;
    }

    public static Builder create(Property.Scope scope) {
        return new Introspection(ScopeStub.INSTANCE, Resolver.EMPTY).createChild(scope);
    }

    public <T> T get(Property<T> property) {
        Property.Scope propertyScope = property.scope();
        if (!Scopes.isChild(propertyScope, scope)) {
            throw new RuntimeException("scope (%s) of property (%s) isn't child of or equal to introspection scope %s".formatted(propertyScope, property.name(), scope));
        }

        return resolver.get(property, this);
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
            Resolver old = Introspection.this.resolver;
            Resolver resolver = old.createChild(properties, Introspection.this);

            if (!Scopes.isChild(scope, Introspection.this.scope)) {
                throw new RuntimeException("Child scope must be equal or subscope of parent scope");
            }

            return new Introspection(scope, resolver);
        }
    }
}
