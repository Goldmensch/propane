package logic.impl;

import dev.goldmensch.propane.Introspection;
import dev.goldmensch.propane.property.Property;

public interface TestIntrospection extends Introspection<Property.Scope> {

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
