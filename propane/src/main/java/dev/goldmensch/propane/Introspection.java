package dev.goldmensch.propane;

import dev.goldmensch.propane.internal.exposed.Properties;
import dev.goldmensch.propane.internal.Resolver;
import dev.goldmensch.propane.internal.ScopeStub;
import dev.goldmensch.propane.internal.Scopes;
import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.property.SpecificProperty;

public abstract class Introspection<I_SELF extends Introspection<I_SELF>> {
    private final Property.Scope scope;
    final Resolver<I_SELF> resolver;

    // called by Builder#newInstance
    @SuppressWarnings("unchecked")
    protected Introspection(Property.Scope scope, Properties<I_SELF> properties, I_SELF parent) {
        this.scope = scope;

        this.resolver = parent.resolver.createChild(properties, (I_SELF) this);
    }

    // called by create(Scope)
    protected Introspection() {
        this.scope = ScopeStub.INSTANCE;
        this.resolver = Resolver.createEmpty();
    }

    // --- must be added by annotation processor
//    public static Builder create(Property.Scope scope) {
//        return EMPTY.createChild(scope);
//    }

    // overridden with real SpecificProperty implementation
    public <T> T get(SpecificProperty<T> specific) {
        Property<T> property = specific.generalized();
        Property.Scope propertyScope = property.scope();
        if (!Scopes.isChild(propertyScope, scope)) {
            throw new RuntimeException("scope (%s) of property (%s) isn't child of or equal to introspection scope %s".formatted(propertyScope, property.name(), scope));
        }

        return resolver.get(property).orElseThrow();
    }

    // body:
    // return this.new Builder(scope);
    // overridden with real Builder implementation and real Builder implementation
    public abstract <B extends Builder<B, ?>> B createChild(Property.Scope scope);

    public abstract class Builder<SELF extends Builder<SELF, PROVIDER>, PROVIDER extends PropertyProvider<?, ?, I_SELF>> {
        protected final Properties<I_SELF> properties;
        protected final Property.Scope scope;

        protected Builder(Property.Scope scope) {
            this.scope = scope;
            this.properties = new Properties<>(scope);
        }

        public SELF add(PROVIDER provider) {
            properties.add(provider);
            return self();
        }

        public I_SELF build() {
            if (!Scopes.isChild(scope, Introspection.this.scope)) {
                throw new RuntimeException("Child scope must be equal or subscope of parent scope");
            }

            return newInstance();
        }

        @SuppressWarnings("unchecked")
        private SELF self() {
            return (SELF) this;
        }

        // return new Introspection(scope, properties, Introspection.this);
        protected abstract I_SELF newInstance();


    }
}
