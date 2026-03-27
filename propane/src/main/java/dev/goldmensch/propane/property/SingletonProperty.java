package dev.goldmensch.propane.property;

import dev.goldmensch.propane.Scope;

import java.util.Objects;

/// A [SingletonProperty] can only hold one instance. It is the most common type of property and
/// is used for services and most configuration options, like simple booleans or strings.
///
/// If multiple [PropertyProvider]s for this property are found, the one with the highest [PropertyProvider#priority()]
/// is used. If multiple providers with the same priority share the highest rank, the latest registered one is used.
///
/// @param <T> the java type of this property
public non-sealed abstract class SingletonProperty<T> implements Property.SingleValue<T> {
    private final String name;
    private final Source source;
    private final Scope scope;
    private final Class<T> type;

    /// @param name the [name][Property#name() ] of this property
    /// @param scope the [scope][Property#scope()] of this property
    /// @param source the [source][Property#source()] of this property
    /// @param type the [type][SingletonProperty#type()] of this property
    public SingletonProperty(String name, Source source, Scope scope,
                             Class<T> type) {
        this.name = name;
        this.source = source;
        this.scope = scope;
        this.type = type;
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

    /// The java type represented by this property
    ///
    /// @return the java type
    public Class<T> type() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SingletonProperty<?>) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.source, that.source) &&
                Objects.equals(this.scope, that.scope) &&
                Objects.equals(this.type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, source, scope, type);
    }

    @Override
    public String toString() {
        return "SingletonProperty[" +
                "name=" + name + ", " +
                "source=" + source + ", " +
                "scope=" + scope + ", " +
                "type=" + type + ']';
    }
}
