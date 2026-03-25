package logic.impl;

import dev.goldmensch.propane.Scope;
import dev.goldmensch.propane.property.SingleProperty;

public class TestSingleProperty<T> extends SingleProperty<T> implements TestProperty<T>{
    public TestSingleProperty(String name, Source source, Scope scope, Class<T> type) {
        super(name, source, scope, type);
    }

    @Override
    public T getScoped() {
        return TestIntrospection.scopedGet(this);
    }
}
