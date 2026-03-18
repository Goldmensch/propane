package dev.goldmensch.propane.event;

import dev.goldmensch.propane.Introspection;
import dev.goldmensch.propane.property.Property;

import java.util.function.BiConsumer;

public interface Listener<E extends Event<S>, S extends Property.Scope, I extends Introspection<I, S>> {
    void accept(E event, I introspection);

    Class<E> event();

    static <T extends Event<S>, S extends Property.Scope, I extends Introspection<I, S>> Listener<T, S, I> create(Class<T> event, BiConsumer<T, I> acceptor) {
        return new Listener<>() {
            @Override
            public void accept(T event, I introspection) {
                acceptor.accept(event, introspection);
            }

            @Override
            public Class<T> event() {
                return event;
            }
        };
    }
}
