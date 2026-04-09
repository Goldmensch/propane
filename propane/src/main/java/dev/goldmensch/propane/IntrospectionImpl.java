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

/// Propane differentiates its API into two 2 sides:
///
/// - the one accessible by the user of the library that is using propane
/// - the one only accessible by the library using propane, so to speak the "internal" API
///
/// [IntrospectionImpl] is this second, internal side of [Introspection]. Here a library can
/// register [PropertyProvider] and create child instances of other [IntrospectionImpl].
/// Therefore, you should read the documentation of [Introspection] first.
///
/// The first step of creating [IntrospectionImpl] is by calling [IntrospectionImpl#create(Scope)]
/// with the "root" scope passed. In most cases, this is the [Scope] with the lowest [priority][Scope#priority()].
/// Looking at the example in [Scope], this would be `Scopes.CONFIGURATION`.
///
/// For any child scope, you create a child [Introspection] instance by calling [#createChild(Scope)] on the parent one
/// with the new scope passed. This child instance will have all property valus of the parent scope and its own property values.
/// For furhrter information, take a look at the documentation of [Introspection] and [Scope].
///
/// Beside properties, this class is also used to [publish][#publish(Event)] [Event]s. Such events, must have the _same_ [Scope]
/// as the [introspection instance][#scope()]. The published events are then passed to the parent of this class and so on.
///
///
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

    /// Creates an instance of this [IntrospectionImpl] with the given [Scope] set.
    ///
    /// @param scope the [Scope] of the to be created [IntrospectionImpl]
    /// @return the [IntrospectionImpl.Builder] of the new [IntrospectionImpl]
    @SkeletonMethod
    public static IntrospectionImpl<?, ?, ?, ?>.Builder create(Scope scope) {
        throw new SkeletonMethodException();
    }

    /// {@inheritDoc}
    // overridden with real SpecificProperty implementation
    public <T> T get(SpecificProperty<T> specific) {
        Property<T> property = specific.generalized();
        Scope propertyScope = property.scope();
        if (!Scopes.isParent(propertyScope, scope)) {
            throw new RuntimeException("scope (%s) of property (%s) isn't child of or equal to introspection scope %s".formatted(propertyScope, property.name(), scope));
        }

        return resolver.get(property).orElseThrow();
    }

    /// {@inheritDoc}
    @Override
    public S scope() {
        return scope;
    }

    /// Creates a child introspection instance with the given scope, that must be a child of scope that this introspection instance
    /// is bound to.
    ///
    /// @param scope the [Scope] of the child instance
    /// @return a builder to create the introspection instance
    // body:
    // return this.new Builder(scope);
    // overridden with real Builder implementation
    @SkeletonMethod
    public abstract B createChild(S scope);

    /// {@inheritDoc}
    @Override
    public void subscribe(Listener<? extends Event<S>, S, I> listener) {
        eventBus.add(listener);
    }

    /// Publishes the given event to this introspection and its parents.
    ///
    /// All listeners registered on this introspection instance or any parent instance, that are listening
    /// for the type of this event will be called.
    ///
    /// @param event the [Event] to be published
    public void publish(Event<S> event) {
        eventBus.publish(event, self());
    }


    @SuppressWarnings("unchecked")
    private I self() {
        return (I) this;
    }

    /// The builder used to create instances of [Introspection].
    @Skeleton
    public abstract class Builder {
        protected final Properties<I> properties;
        protected final S scope;

        protected Builder(S scope) {
            this.scope = scope;
            this.properties = new Properties<>(scope);
        }

        /// Adds the given [PropertyProvider] to this introspection instance.
        ///
        /// @param provider the [PropertyProvider] to add
        /// @return this builder instance
        public B add(PropertyProvider<?, ?, I> provider) {
            properties.add(provider);
            return self();
        }

        /// Adds an [PropertyProvider] with given [Property] and [supplier][PropertyProvider#supplier()].
        ///
        /// [PropertyProvider#priority()] will be set to [PropertyProvider.Priority#FALLBACK]
        /// [PropertyProvider#owner()] will be the caller of this method, see [StackWalker#getCallerClass()].
        ///
        /// @param property the property the values are provided for
        /// @param supplier the supplier providing the values
        /// @return this builder instance
        @SkeletonMethod
        public <T> B addFallback(SpecificProperty<T> property, Function<I, @Nullable T> supplier) {
            throw new SkeletonMethodException();
        }

        /// Adds an [PropertyProvider] with given [Property] and [supplier][PropertyProvider#supplier()].
        ///
        /// [PropertyProvider#priority()] will be set to [PropertyProvider.Priority#BUILDER]
        /// [PropertyProvider#owner()] will be the caller of this method, see [StackWalker#getCallerClass()].
        ///
        /// @param property the property the values are provided for
        /// @param supplier the supplier providing the values
        /// @return this builder instance
        @SkeletonMethod
        public <T> B addBuilder(SpecificProperty<T> property, Function<I, @Nullable T> supplier) {
            throw new SkeletonMethodException();
        }

        /// Adds an [PropertyProvider] with given [Property], [supplier][PropertyProvider#supplier()]
        /// and [owner][PropertyProvider#owner()]
        ///
        /// [PropertyProvider#priority()] will be set to [PropertyProvider.Priority#FALLBACK]
        ///
        /// @param property the property the values are provided for
        /// @param supplier the supplier providing the values
        /// @return this builder instance
        @SkeletonMethod
        public <T> B addFallback(SpecificProperty<T> property, Class<?> owner, Function<I, @Nullable T> supplier) {
            throw new SkeletonMethodException();
        }

        /// Adds an [PropertyProvider] with given [Property], [supplier][PropertyProvider#supplier()]
        /// and [owner][PropertyProvider#owner()]
        ///
        /// [PropertyProvider#priority()] will be set to [PropertyProvider.Priority#BUILDER]
        ///
        /// @param property the property the values are provided for
        /// @param supplier the supplier providing the values
        /// @return this builder instance
        @SkeletonMethod
        public <T> B addBuilder(SpecificProperty<T> property, Class<?> owner, Function<I, @Nullable T> supplier) {
            throw new SkeletonMethodException();
        }

        /// Creates a new [Introspection] instance with the scope of this builder and
        /// the registered providers.
        ///
        /// @return the newly created [Introspection] instance
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
