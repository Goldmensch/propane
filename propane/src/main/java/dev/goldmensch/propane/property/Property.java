package dev.goldmensch.propane.property;

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

    interface Scope {
        /// sorted lower to higher - best to be enum, could be backed by [Enum#ordinal()]
        int priority();
    }

    sealed interface SingleValue<T> extends Property<T> permits SingleProperty {}

    sealed interface MultiValue<T> extends Property<T> permits CollectionProperty, MapProperty {
        FallbackBehaviour fallbackBehaviour();
    }

    enum FallbackBehaviour {
        OVERRIDE,
        ACCUMULATE
    }

}
