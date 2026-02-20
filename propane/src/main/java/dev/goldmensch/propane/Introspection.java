package dev.goldmensch.propane;

import dev.goldmensch.propane.internal.Properties;
import dev.goldmensch.propane.internal.Resolver;

public class Introspection {

    private final Resolver resolver;

    private Introspection(Resolver resolver) {
        this.resolver = resolver;
    }

    public static Builder create() {
        return new Builder(Resolver.EMPTY);
    }

    public <T> T get(Property<T> property) {
        return resolver.get(property, this);
    }

    public Builder createChild() {
        // ensure all values are inside cache, so that child don't 'compute' parent properties.
        // if we wouldn't do that, child and parent may get different instances from the same provider
        // we load it here instead of in Builder#build() so that the majority of values is still computed lazy
        resolver.ensureAllComputed(this);

        return new Builder(resolver);
    }

    public static class Builder {
        private final Resolver oldResolver;
        private final Properties properties = new Properties();

        public Builder(Resolver oldResolver) {
            this.oldResolver = oldResolver;
        }

        public Builder add(PropertyProvider<?> provider) {
            properties.add(provider);
            return this;
        }

        public Introspection build() {
            Resolver resolver = oldResolver.createChild(properties);

            return new Introspection(resolver);
        }
    }
}
