package dev.goldmensch.propane;

import dev.goldmensch.propane.event.Event;
import dev.goldmensch.propane.event.Listener;
import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.property.SpecificProperty;
import dev.goldmensch.propane.spec.SkeletonMethod;
import dev.goldmensch.propane.spec.SkeletonMethodException;

public interface Introspection<SELF extends Introspection<SELF, S>, S extends Property.Scope> {

    @SkeletonMethod
    static boolean accessible() {
        // return IntrospectionImpl.INTROSPECTION.isBound();
        throw new SkeletonMethodException();
    }

    @SkeletonMethod
    static Introspection<?, ?> accessScoped() {
        // return IntrospectionImpl.INTROSPECTION.get();
        throw new SkeletonMethodException();
    }


    @SkeletonMethod
    static <T> T scopedGet(SpecificProperty<T> property) {
        //  return accessScoped().get(property);
        throw new SkeletonMethodException();
    }

    // overridden with real SpecificProperty implementation
    @SkeletonMethod
    <T> T get(SpecificProperty<T> specific);

    void subscribe(Listener<? extends Event<S>, S, SELF> listener);

    S scope();
}
