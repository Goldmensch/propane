package dev.goldmensch.propane;

import dev.goldmensch.propane.internal.Resolver;

public class Introspection {

    private final Resolver resolver;

    private Introspection(Resolver resolver) {
        this.resolver = resolver;
    }

    public static Builder create() {
        return new Builder(new Resolver.State());
    }

    public <T> T get(Property<T> property) {
        return resolver.get(property, this);
    }

    public Builder createChild() {
        return new Builder(resolver.state());
    }

    public static class Builder {
        private final Resolver.State state;

        private Builder(Resolver.State state) {
            this.state = state;
        }

        public Builder add(PropertyProvider<?> provider) {
            state.properties().add(provider);
            return this;
        }

        public Introspection build() {
            Resolver resolver = state.create();

            return new Introspection(resolver);
        }
    }
}
