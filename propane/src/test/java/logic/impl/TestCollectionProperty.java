package logic.impl;

import dev.goldmensch.propane.Introspection;
import dev.goldmensch.propane.property.CollectionProperty;

import java.util.Collection;
import java.util.List;

public class TestCollectionProperty<T> extends CollectionProperty<T> implements TestProperty<Collection<T>> {
    public TestCollectionProperty(String name, Source source, Scope scope, Class<T> type, FallbackBehaviour fallbackBehaviour) {
        super(name, source, scope, type, fallbackBehaviour);
    }

    @Override
    public Collection<T> getScoped() {
        return TestIntrospection.scopedGet(this);
    }
}
