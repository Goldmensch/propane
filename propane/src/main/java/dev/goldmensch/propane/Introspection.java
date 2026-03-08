package dev.goldmensch.propane;

import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.property.SpecificProperty;
import dev.goldmensch.propane.spec.SkeletonMethod;
import dev.goldmensch.propane.spec.SkeletonMethodException;

public interface Introspection {

    @SkeletonMethod
    static boolean accessible() {
        // return IntrospectionImpl.INTROSPECTION.isBound();
        throw new SkeletonMethodException();
    }

    @SkeletonMethod
    static Introspection accessScoped() {
        // return IntrospectionImpl.INTROSPECTION.get();
        throw new SkeletonMethodException();
    }


    @SkeletonMethod
    static <T> T scopedGet(SpecificProperty<T> property) {
        //  return accessScoped().get(property);
        throw new SkeletonMethodException();
    }

    // overridden with real SpecificProperty implementation
    <T> T get(SpecificProperty<T> specific);
}
