package dev.goldmensch.propane.property;

import dev.goldmensch.propane.Scope;

import java.util.Collection;
import java.util.Objects;

public non-sealed abstract class EnumerationProperty<T> implements Property.MultiValue<Collection<T>> {
    private final String name;
    private final Source source;
    private final Scope scope;
    private final Class<T> type;
    private final FallbackBehaviour fallbackBehaviour;

    public EnumerationProperty(String name, Source source, Scope scope, Class<T> type,
                               FallbackBehaviour fallbackBehaviour) {
        this.name = name;
        this.source = source;
        this.scope = scope;
        this.type = type;
        this.fallbackBehaviour = fallbackBehaviour;
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

    public Class<T> type() {
        return type;
    }

    @Override
    public FallbackBehaviour fallbackBehaviour() {
        return fallbackBehaviour;
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
                Objects.equals(this.fallbackBehaviour, that.fallbackBehaviour);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, source, scope, type, fallbackBehaviour);
    }

    @Override
    public String toString() {
        return "EnumerationProperty[" +
                "name=" + name + ", " +
                "source=" + source + ", " +
                "scope=" + scope + ", " +
                "type=" + type + ", " +
                "fallbackBehaviour=" + fallbackBehaviour + ']';
    }
}
