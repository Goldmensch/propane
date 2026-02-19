package dev.goldmensch.propane;

import java.util.Comparator;
import java.util.function.Function;

public record PropertyProvider<T>(
        Property<T> property,
        Priority priority,
        Class<?> owner,
        Function<Introspection, T> supplier
) {
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
