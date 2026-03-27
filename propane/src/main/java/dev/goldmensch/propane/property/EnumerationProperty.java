package dev.goldmensch.propane.property;

import dev.goldmensch.propane.Scope;

import java.util.Collection;
import java.util.Objects;

/// An [EnumerationProperty] represents an [Collection]. It can hold multiple values of the same type and makes no guarantees
/// about the order of its elements.
///
/// If multiple [PropertyProvider]s are found for this property, they are generally combined.
/// For information on how fallback values are trod, visit the documentation of [Property.FallbackStrategy].
///
/// @param <T> the java type of the elements held by this property
public non-sealed abstract class EnumerationProperty<T> implements Property.MultiValue<Collection<T>> {
    private final String name;
    private final Source source;
    private final Scope scope;
    private final Class<T> type;
    private final FallbackStrategy fallbackStrategy;

    /// @param name the [name][Property#name()] of this property
    /// @param source the [source][Property#source()] of this property
    /// @param scope the [scope][Property#scope()] of this property
    /// @param type the [type][EnumerationProperty#type()] of this property
    /// @param fallbackStrategy the [fallback strategy][Property.MultiValue#fallbackBehaviour()] of this property
    public EnumerationProperty(String name, Source source, Scope scope, Class<T> type,
                               FallbackStrategy fallbackStrategy) {
        this.name = name;
        this.source = source;
        this.scope = scope;
        this.type = type;
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

    /// the java type of the elements hold by this property
    ///
    /// @return the element's java class
    public Class<T> type() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (EnumerationProperty<?>) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.source, that.source) &&
                Objects.equals(this.scope, that.scope) &&
                Objects.equals(this.type, that.type) &&
                Objects.equals(this.fallbackStrategy, that.fallbackStrategy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, source, scope, type, fallbackStrategy);
    }

    @Override
    public String toString() {
        return "EnumerationProperty[" +
                "name=" + name + ", " +
                "source=" + source + ", " +
                "scope=" + scope + ", " +
                "type=" + type + ", " +
                "fallbackStrategy=" + fallbackStrategy + ']';
    }
}
