package dev.goldmensch.propane;

import java.util.function.Function;

public record PropertyProvider<T>(
        Property<T> property,
        int priority,
        Class<?> owner,
        Function<Introspection, T> supplier
) {
    public static final int FALLBACK_PRIORITY = 0;
    public static final int BUILDER_PRIORITY = Integer.MAX_VALUE;
}
