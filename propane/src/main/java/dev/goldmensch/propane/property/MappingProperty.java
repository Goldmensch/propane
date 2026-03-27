package dev.goldmensch.propane.property;

import dev.goldmensch.propane.Scope;

import java.util.Map;
import java.util.Objects;

public non-sealed abstract class MappingProperty<K, V> implements Property.MultiValue<Map<K, V>> {
    private final String name;
    private final Source source;
    private final Scope scope;
    private final Class<K> keyType;
    private final Class<V> valueType;
    private final FallbackStrategy fallbackStrategy;

    public MappingProperty(String name, Source source, Scope scope, Class<K> keyType,
                           Class<V> valueType,
                           FallbackStrategy fallbackStrategy) {
        this.name = name;
        this.source = source;
        this.scope = scope;
        this.keyType = keyType;
        this.valueType = valueType;
        this.fallbackStrategy = fallbackStrategy;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Source source() {
        return source;
    }

    @Override
    public Scope scope() {
        return scope;
    }

    public Class<K> keyType() {
        return keyType;
    }

    public Class<V> valueType() {
        return valueType;
    }

    @Override
    public FallbackStrategy fallbackBehaviour() {
        return fallbackStrategy;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MappingProperty<?, ?>) obj;
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
        return "MappingProperty[" +
                "name=" + name + ", " +
                "source=" + source + ", " +
                "scope=" + scope + ", " +
                "keyType=" + keyType + ", " +
                "valueType=" + valueType + ", " +
                "fallbackStrategy=" + fallbackStrategy + ']';
    }
}
