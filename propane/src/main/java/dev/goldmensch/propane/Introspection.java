package dev.goldmensch.propane;

import dev.goldmensch.propane.property.SpecificProperty;

public interface Introspection {

    // overridden with real SpecificProperty implementation
    <T> T get(SpecificProperty<T> specific);
}
