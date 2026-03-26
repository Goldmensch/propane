package logic.impl;

import dev.goldmensch.propane.Scope;
import dev.goldmensch.propane.property.SingletonProperty;

public class TestSingletonProperty<T> extends SingletonProperty<T> implements TestProperty<T>{
    public TestSingletonProperty(String name, Source source, Scope scope, Class<T> type) {
        super(name, source, scope, type);
    }

    @Override
    public T getScoped() {
        return TestIntrospection.scopedGet(this);
    }
}
