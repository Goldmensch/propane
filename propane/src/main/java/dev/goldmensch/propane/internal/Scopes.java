package dev.goldmensch.propane.internal;

import dev.goldmensch.propane.Property;

public class Scopes {
    private static int compare(Property.Scope one, Property.Scope other) {
        return Integer.compare(one.priority(), other.priority());
    }

    // child or equal
    public static boolean isChild(Property.Scope one, Property.Scope other) {
        return compare(one, other) >= 0;
    }
}
