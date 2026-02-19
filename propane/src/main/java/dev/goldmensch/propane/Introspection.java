package dev.goldmensch.propane;

import dev.goldmensch.propane.internal.Properties;
import dev.goldmensch.propane.internal.Resolver;

public class Introspection {

    private final Resolver resolver;

    private Introspection(Resolver resolver) {
        this.resolver = resolver;
    }

    public static Builder create() {
        return new Builder(new Properties());
    }

    public <T> T get(Property<T> property) {
        return resolver.get(property, this);
    }

    public Builder createChild() {
        return new Builder(resolver.properties());
    }

    public static class Builder {
        private final Properties properties;

        private Builder(Properties properties) {
            this.properties = properties;
        }

        public Builder add(PropertyProvider<?> provider) {
            properties.add(provider);
            return this;
        }

        public Introspection build() {
            Resolver resolver = properties.createResolver();

            return new Introspection(resolver);
        }
    }
}
