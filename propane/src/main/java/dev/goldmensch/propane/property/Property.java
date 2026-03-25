package dev.goldmensch.propane.property;

import dev.goldmensch.propane.Scope;

@SuppressWarnings("unused")
public sealed interface Property<T> {

    Scope scope();

    String name();

    Source source();

    enum Source {
        PROVIDED,

        BUILDER,

        EXTENSION
    }

    sealed interface SingleValue<T> extends Property<T> permits SingleProperty {}

    sealed interface MultiValue<T> extends Property<T> permits CollectionProperty, MapProperty {
        FallbackBehaviour fallbackBehaviour();
    }

    enum FallbackBehaviour {
        OVERRIDE,
        ACCUMULATE
    }

    T getScoped();

}
