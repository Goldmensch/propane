package logic.impl;

import dev.goldmensch.propane.IntrospectionSkeleton;
import dev.goldmensch.propane.Scope;

public interface TestIntrospection extends IntrospectionSkeleton<TestIntrospection, Scope> {

    static boolean accessible() {
        return TestIntrospectionImpl.INTROSPECTION.isBound();
    }

    static TestIntrospection accessScoped() {
        return TestIntrospectionImpl.INTROSPECTION.get();
    }

    static <T> T scopedGet(TestProperty<T> property) {
        return accessScoped().get(property);
    }

    <T> T get(TestProperty<T> specific);
}
