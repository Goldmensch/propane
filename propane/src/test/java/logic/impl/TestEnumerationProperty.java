package logic.impl;

import dev.goldmensch.propane.Scope;
import dev.goldmensch.propane.property.EnumerationProperty;

import java.util.Collection;

public class TestEnumerationProperty<T> extends EnumerationProperty<T> implements TestProperty<Collection<T>> {
    public TestEnumerationProperty(String name, Source source, Scope scope, Class<T> type, FallbackBehaviour fallbackBehaviour) {
        super(name, source, scope, type, fallbackBehaviour);
    }

    @Override
    public Collection<T> getScoped() {
        return TestIntrospection.scopedGet(this);
    }
}
