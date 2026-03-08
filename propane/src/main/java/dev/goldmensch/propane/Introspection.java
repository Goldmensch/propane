package dev.goldmensch.propane;

import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.property.SpecificProperty;

public interface Introspection {

    //    -- written by generator
    //    static boolean accessible() {
    //        return IntrospectionImpl.INTROSPECTION.isBound();
    //    }

    //    -- written by generator
    //    static Introspection accessScoped() {
    //        return IntrospectionImpl.INTROSPECTION.get();
    //    }

    //   -- written by generator with correct property type
    //   static <T> T scopedGet(SpecificProperty<T> property) {
    //    return accessScoped().get(property);
    // }

    // overridden with real SpecificProperty implementation
    <T> T get(SpecificProperty<T> specific);
}
