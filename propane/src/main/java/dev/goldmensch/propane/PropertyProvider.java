package dev.goldmensch.propane;

import dev.goldmensch.propane.property.SpecificProperty;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

public class PropertyProvider<T, PROPERTY extends SpecificProperty<T>, INTROSPECTION extends Introspection> {
    private final PROPERTY property;
    private final Priority priority;
    private final Class<?> owner;
    private final Function<INTROSPECTION, @Nullable T> supplier;

    public PropertyProvider(
            PROPERTY property,
            Priority priority,
            Class<?> owner,
            Function<INTROSPECTION, @Nullable T> supplier
    ) {
        this.property = property;
        this.priority = priority;
        this.owner = owner;
        this.supplier = supplier;
    }

    public PROPERTY property() {
        return property;
    }

    public Priority priority() {
        return priority;
    }

    public Class<?> owner() {
        return owner;
    }

    public Function<INTROSPECTION, @Nullable T> supplier() {
        return supplier;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PropertyProvider<?, ?, ?>) obj;
        return Objects.equals(this.property, that.property) &&
                Objects.equals(this.priority, that.priority) &&
                Objects.equals(this.owner, that.owner) &&
                Objects.equals(this.supplier, that.supplier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(property, priority, owner, supplier);
    }

    @Override
    public String toString() {
        return "PropertyProvider[" +
                "property=" + property + ", " +
                "priority=" + priority + ", " +
                "owner=" + owner + ", " +
                "supplier=" + supplier + ']';
    }


    public static class Priority implements Comparable<Priority> {
        public static final Priority FALLBACK = new Priority(0);
        public static final Priority BUILDER = new Priority(Integer.MAX_VALUE);

        private final int ordinal;

        Priority(int ordinal) {
            this.ordinal = ordinal;
        }

        public static Priority of(int ordinal) {
            if (ordinal < 0) {
                throw new RuntimeException("priority ordinal can't be negative");
            }

            if (ordinal == FALLBACK.ordinal || ordinal == BUILDER.ordinal) {
                throw new RuntimeException("priority ordinal can't be 0 or Integer.MAX_VALUE");
            }

            return new Priority(ordinal);
        }

        @Override
        public int compareTo(Priority o) {
            return Comparator.<Integer>reverseOrder().compare(ordinal, o.ordinal);
        }
    }
}
