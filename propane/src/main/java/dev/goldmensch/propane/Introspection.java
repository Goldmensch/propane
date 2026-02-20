package dev.goldmensch.propane;

import dev.goldmensch.propane.internal.Properties;
import dev.goldmensch.propane.internal.Resolver;

public class Introspection {

    private final Resolver resolver;

    private Introspection(Resolver resolver) {
        this.resolver = resolver;
    }

    public static Builder create() {
        return new Introspection(Resolver.EMPTY).new Builder();
    }

    public <T> T get(Property<T> property) {
        return resolver.get(property, this);
    }

    public Builder createChild() {
        return this.new Builder(); // create non-static inner class
    }

    public class Builder {
        private final Properties properties = new Properties();

        public Builder add(PropertyProvider<?> provider) {
            properties.add(provider);
            return this;
        }

        public Introspection build() {
            Resolver old = Introspection.this.resolver;
            Resolver resolver = old.createChild(properties, Introspection.this);

            return new Introspection(resolver);
        }
    }
}
