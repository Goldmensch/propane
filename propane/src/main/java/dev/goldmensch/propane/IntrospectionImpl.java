package dev.goldmensch.propane;

import dev.goldmensch.propane.event.Event;
import dev.goldmensch.propane.event.Listener;
import dev.goldmensch.propane.event.internal.EventBus;
import dev.goldmensch.propane.internal.exposed.Properties;
import dev.goldmensch.propane.internal.Resolver;
import dev.goldmensch.propane.internal.Scopes;
import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.property.PropertyProvider;
import dev.goldmensch.propane.property.SpecificProperty;
import dev.goldmensch.propane.spec.SkeletonMethod;
import dev.goldmensch.propane.spec.SkeletonMethodException;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

// I've got insane with that. But it had to be typesafe. It just had to be.
@Skeleton
public abstract class IntrospectionImpl<I_SELF extends IntrospectionImpl<I_SELF, I, B, S>, I extends Introspection<I, S>, B extends IntrospectionImpl<I_SELF, I, B, S>.Builder, S extends Scope>
implements Introspection<I, S> {
    final Registry<S> registry;
    final EventBus<I, S> eventBus;
    private final S scope;
    final Resolver<I> resolver;

    // called by Builder#newInstance
    protected IntrospectionImpl(S scope, Properties<I> properties, I_SELF parent) {
        this.registry = parent.registry;
        this.scope = scope;

        addIntrospectionProvider(properties);
        this.resolver = parent.resolver.createChild(properties, self());
        this.eventBus = new EventBus<>(this.registry, scope, parent.eventBus);
    }

    // called by create(Scope)
    protected IntrospectionImpl(Registry<S> registry, S scope) {
        this.registry = registry;
        this.scope = scope;
        this.resolver = Resolver.createEmpty();
        this.eventBus = new EventBus<>(registry, scope, null);
    }

    @SkeletonMethod
    protected abstract void addIntrospectionProvider(Properties<I> properties);

    @SkeletonMethod
    public static IntrospectionImpl<?, ?, ?, ?>.Builder create(Scope scope) {
        throw new SkeletonMethodException();
    }

    // overridden with real SpecificProperty implementation
    public <T> T get(SpecificProperty<T> specific) {
        Property<T> property = specific.generalized();
        Scope propertyScope = property.scope();
        if (!Scopes.isParent(propertyScope, scope)) {
            throw new RuntimeException("scope (%s) of property (%s) isn't child of or equal to introspection scope %s".formatted(propertyScope, property.name(), scope));
        }

        return resolver.get(property).orElseThrow();
    }

    @Override
    public S scope() {
        return scope;
    }

    // body:
    // return this.new Builder(scope);
    // overridden with real Builder implementation
    @SkeletonMethod
    public abstract B createChild(S scope);

    @Override
    public void subscribe(Listener<? extends Event<S>, S, I> listener) {
        eventBus.add(listener);
    }

    public void publish(Event<S> event) {
        eventBus.publish(event, self());
    }


    @SuppressWarnings("unchecked")
    private I self() {
        return (I) this;
    }

    public abstract class Builder {
        protected final Properties<I> properties;
        protected final S scope;

        protected Builder(S scope) {
            this.scope = scope;
            this.properties = new Properties<>(scope);
        }

        // I guarantee only compatible providers
        public B add(PropertyProvider<?, ?, I> provider) {
            properties.add(provider);
            return self();
        }

        @SkeletonMethod
        public <T> B addFallback(SpecificProperty<T> property, Function<I, @Nullable T> supplier) {
            throw new SkeletonMethodException();
        }

        @SkeletonMethod
        public <T> B addBuilder(SpecificProperty<T> property, Function<I, @Nullable T> supplier) {
            throw new SkeletonMethodException();
        }

        @SkeletonMethod
        public <T> B addFallback(SpecificProperty<T> property, Class<?> owner, Function<I, @Nullable T> supplier) {
            throw new SkeletonMethodException();
        }

        @SkeletonMethod
        public <T> B addBuilder(SpecificProperty<T> property, Class<?> owner, Function<I, @Nullable T> supplier) {
            throw new SkeletonMethodException();
        }

        public I_SELF build() {
            if (!Scopes.isSub(scope, IntrospectionImpl.this.scope)) {
                throw new RuntimeException("Child scope must be equal or subscope of parent scope");
            }

            return newInstance();
        }

        @SuppressWarnings("unchecked")
        private B self() {
            return (B) this;
        }

        protected Class<?> caller() {
            return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
        }

        // return new IntrospectionImpl(scope, properties, IntrospectionImpl.this);
        @SkeletonMethod
        protected abstract I_SELF newInstance();

    }
}
