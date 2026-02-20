package dev.goldmensch.propane;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

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

    sealed interface SingleValue<T> extends Property<T> {}

    sealed interface MultiValue<T> extends Property<T> {
        FallbackBehaviour fallbackBehaviour();
    }

    enum FallbackBehaviour {
        OVERRIDE,
        ACCUMULATE
    }

    record SingleProperty<T>(String name, Source source, Scope scope, Class<T> type) implements SingleValue<T> {}

    record MapProperty<K, V>(String name, Source source, Scope scope, Class<K> keyType, Class<V> valueType, FallbackBehaviour fallbackBehaviour) implements MultiValue<Map<K, V>> {}

    record CollectionProperty<T>(String name, Source source, Scope scope, Class<T> type, FallbackBehaviour fallbackBehaviour) implements MultiValue<Collection<T>> {}

}
