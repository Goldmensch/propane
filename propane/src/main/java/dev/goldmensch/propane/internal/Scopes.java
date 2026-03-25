package dev.goldmensch.propane.internal;

import dev.goldmensch.propane.Scope;

public class Scopes {
    private static int compare(Scope one, Scope other) {
        return Integer.compare(one.priority(), other.priority());
    }

    // child or equal
    public static boolean isSub(Scope child, Scope parent) {
        return compare(child, parent) >= 0;
    }

    public static boolean isParent(Scope parent, Scope child) {
        return compare(parent, child) <= 0;
    }

    public static boolean isSame(Scope one, Scope other) {
        return compare(one, other) == 0;
    }
}
