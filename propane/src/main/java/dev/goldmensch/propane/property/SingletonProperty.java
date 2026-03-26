package dev.goldmensch.propane.property;

import dev.goldmensch.propane.Scope;

import java.util.Objects;

public non-sealed abstract class SingletonProperty<T> implements Property.SingleValue<T> {
    private final String name;
    private final Source source;
    private final Scope scope;
    private final Class<T> type;

    public SingletonProperty(String name, Source source, Scope scope,
                             Class<T> type) {
        this.name = name;
        this.source = source;
        this.scope = scope;
        this.type = type;
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
