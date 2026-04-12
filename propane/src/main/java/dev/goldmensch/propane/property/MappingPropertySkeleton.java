package dev.goldmensch.propane.property;

import dev.goldmensch.propane.Scope;
import dev.goldmensch.propane.Skeleton;

import java.util.Map;
import java.util.Objects;

/// A [`MappingProperty`][MappingPropertySkeleton] represents a [Map]. It has one key type and one value type.
///
/// For example, think of validators that should validate request parameters based on their type.
/// These validators can be registered via a `MappingPropertySkeleton<Class, Validator>`, basically representing
/// an `Map<Class, Validator>`.
///
/// If multiple [`PropertyProvider`][PropertyProviderSkeleton]s are registered for this property at the same introspection instance, all values are combined and the ones
/// from a provider with a higher [priority][PropertyProviderSkeleton#priority()]
/// override the values of the ones from provider with a lower priority. (think of [Map#put(Object, Object)]).
/// For information on how fallback values are trod, visit the documentation of [Property.FallbackStrategy].
///
/// If you have multiple [`PropertyProvider`][PropertyProviderSkeleton]s registered at different introspection instances, that are related to each other
/// (the one is child of the other), the above applies for each introspection instance itself. The final value is then computed
/// by combining all values under the rule that values from a child override the parents' ones.
///
/// For example, take a look here (simplified API):
/// ```java
/// IntrospectionSkeleton A = IntrospectionImplSkeleton.create(Scopes.ROOT)
///     .addBuilder(Property.MAPPING, _ -> Map.of("foo", "Value A Builder")) // builder has higher priority than fallback
///     .addFallback(Property.MAPPING, _ -> Map.of("foo", "Value A Fallback"))
///     .build();
///
/// A.get(Property.MAPPING).get("foo") // returns "Value A Builder"
///
/// IntrospectionSkeleton B = A.createChild(Scopes.ROOT)
///     .addFallback(Property.MAPPING, _ -> Map.of("foo", "Value B"))
///     .build();
///
/// B.get(Property.MAPPING).get("foo") // returns "Value B"
/// ```
///
/// @param <K> the java type of the key
/// @param <V> the java type of the value
/// @see SpecificProperty why you have to use the "specific" version of this class
@Skeleton
public non-sealed abstract class MappingPropertySkeleton<K, V> implements Property.MultiValue<Map<K, V>> {
    private final String name;
    private final Source source;
    private final Scope scope;
    private final Class<K> keyType;
    private final Class<V> valueType;
    private final FallbackStrategy fallbackStrategy;


    /// @param name the [name][Property#name()] of this property
    /// @param source the [source][Property#source()] of this property
    /// @param scope the [scope][Property#scope()] of this property
    /// @param keyType the [key's java type][MappingPropertySkeleton#keyType()] of this property
    /// @param valueType the [value's java type][MappingPropertySkeleton#valueType()] of this property
    /// @param fallbackStrategy the [fallback strategy][Property.MultiValue#fallbackBehaviour()] of this property
    public MappingPropertySkeleton(String name, Source source, Scope scope, Class<K> keyType,
                                   Class<V> valueType,
                                   FallbackStrategy fallbackStrategy) {
        this.name = name;
        this.source = source;
        this.scope = scope;
        this.keyType = keyType;
        this.valueType = valueType;
        this.fallbackStrategy = fallbackStrategy;
    }

    /// {@inheritDoc}
    @Override
    public String name() {
        return name;
    }

    /// {@inheritDoc}
    @Override
    public Source source() {
        return source;
    }

    /// {@inheritDoc}
    @Override
    public Scope scope() {
        return scope;
    }

    /// {@inheritDoc}
    @Override
    public FallbackStrategy fallbackBehaviour() {
        return fallbackStrategy;
    }

    /// the java type of the key
    ///
    /// @return the key's class
    public Class<K> keyType() {
        return keyType;
    }

    /// the java type of the value
    ///
    /// @return the value's class
    public Class<V> valueType() {
        return valueType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MappingPropertySkeleton<?, ?>) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.source, that.source) &&
                Objects.equals(this.scope, that.scope) &&
                Objects.equals(this.keyType, that.keyType) &&
                Objects.equals(this.valueType, that.valueType) &&
                Objects.equals(this.fallbackStrategy, that.fallbackStrategy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, source, scope, keyType, valueType, fallbackStrategy);
    }

    @Override
    public String toString() {
        return "MappingPropertySkeleton[" +
                "name=" + name + ", " +
                "source=" + source + ", " +
                "scope=" + scope + ", " +
                "keyType=" + keyType + ", " +
                "valueType=" + valueType + ", " +
                "fallbackStrategy=" + fallbackStrategy + ']';
    }
}
