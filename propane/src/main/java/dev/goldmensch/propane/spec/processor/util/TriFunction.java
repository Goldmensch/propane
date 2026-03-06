package dev.goldmensch.propane.spec.processor.util;

@FunctionalInterface
public interface TriFunction<A, B, C, R> {
    R accept(A a, B b, C c);
}
