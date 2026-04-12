package logic.impl;

import dev.goldmensch.propane.property.PropertyProviderSkeleton;

import java.util.function.Function;

public class TestPropertyProvider<T> extends PropertyProviderSkeleton<T, TestProperty<T>, TestIntrospection> {
    public TestPropertyProvider(TestProperty<T> property, Priority priority, Class<?> owner, Function<TestIntrospection, T> supplier) {
        super(property, priority, owner, supplier);
    }
}
