package dev.goldmensch.propane.internal;

public class Helpers {
    @SuppressWarnings("unchecked")
    public static <T> T castUnsafe(Object some) {
        return (T) some;
    }
}
