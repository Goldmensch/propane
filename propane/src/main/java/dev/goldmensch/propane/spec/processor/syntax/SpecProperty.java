package dev.goldmensch.propane.spec.processor.syntax;

import dev.goldmensch.propane.property.Property;

public sealed interface SpecProperty permits SpecEnumeration, SpecMapping, SpecSingleton {
    String name();
    Property.Source source();
    String scope();
    boolean internal();
}
