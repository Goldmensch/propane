package dev.goldmensch.propane;

import dev.goldmensch.propane.internal.exposed.Properties;
import dev.goldmensch.propane.internal.Resolver;
import dev.goldmensch.propane.internal.ScopeStub;
import dev.goldmensch.propane.internal.Scopes;
import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.property.SpecificProperty;

// I've got insane with that. But it had to be typesafe. It just had to be.
public abstract class IntrospectionImpl<I_SELF extends IntrospectionImpl<I_SELF, I, B>, I extends Introspection, B extends IntrospectionImpl<I_SELF, I, B>.Builder>
implements Introspection {
    private final Property.Scope scope;
    final Resolver<I> resolver;

    // called by Builder#newInstance
    @SuppressWarnings("unchecked")
    protected IntrospectionImpl(Property.Scope scope, Properties<I> properties, I_SELF parent) {
        this.scope = scope;

        this.resolver = parent.resolver.createChild(properties, (I) this);
    }

    // called by create(Scope)
    protected IntrospectionImpl() {
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
    public abstract B createChild(Property.Scope scope);

    public abstract class Builder {
        protected final Properties<I> properties;
        protected final Property.Scope scope;

        protected Builder(Property.Scope scope) {
            this.scope = scope;
            this.properties = new Properties<>(scope);
        }

        // I guarantee only compatible providers
        public B add(PropertyProvider<?, ?, I> provider) {
            properties.add(provider);
            return self();
        }

        public I_SELF build() {
            if (!Scopes.isChild(scope, IntrospectionImpl.this.scope)) {
                throw new RuntimeException("Child scope must be equal or subscope of parent scope");
            }

            return newInstance();
        }

        @SuppressWarnings("unchecked")
        private B self() {
            return (B) this;
        }

        // validate()
        // return new IntrospectionImpl(scope, properties, IntrospectionImpl.this);
        protected abstract I_SELF newInstance();


    }
}
