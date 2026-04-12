package logic.impl;

import dev.goldmensch.propane.IntrospectionSkeleton;
import dev.goldmensch.propane.Scope;
import dev.goldmensch.propane.event.Event;
import dev.goldmensch.propane.event.Listener;

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
