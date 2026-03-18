package dev.goldmensch.propane.internal;

import dev.goldmensch.propane.property.Property;

public class Scopes {
    private static int compare(Property.Scope one, Property.Scope other) {
        return Integer.compare(one.priority(), other.priority());
    }

    // child or equal
    public static boolean isSub(Property.Scope child, Property.Scope parent) {
        return compare(child, parent) >= 0;
    }

    public static boolean isParent(Property.Scope parent, Property.Scope child) {
        return compare(parent, child) <= 0;
    }

    public static boolean isSame(Property.Scope one, Property.Scope other) {
        return compare(one, other) == 0;
    }
}
