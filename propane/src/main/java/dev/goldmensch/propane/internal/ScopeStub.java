package dev.goldmensch.propane.internal;

import dev.goldmensch.propane.property.Property;

// stub used to have some value for scopes in "empty" introspections
public class ScopeStub implements Property.Scope {

    public static final ScopeStub INSTANCE = new ScopeStub();

    private ScopeStub() {}

    @Override
    public int priority() {
        return Integer.MIN_VALUE; // illegal value for property registration, but so scope will always be parent
    }
}
