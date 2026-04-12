package logic.impl;

import dev.goldmensch.propane.Scope;
import dev.goldmensch.propane.property.EnumerationPropertySkeleton;

import java.util.Collection;

public class TestEnumerationProperty<T> extends EnumerationPropertySkeleton<T> implements TestProperty<Collection<T>> {
    public TestEnumerationProperty(String name, Source source, Scope scope, Class<T> type, FallbackStrategy fallbackStrategy) {
        super(name, source, scope, type, fallbackStrategy);
    }

    @Override
    public Collection<T> getScoped() {
        return TestIntrospection.scopedGet(this);
    }
}
