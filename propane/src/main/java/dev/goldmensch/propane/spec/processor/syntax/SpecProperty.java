package dev.goldmensch.propane.spec.processor.syntax;

import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.spec.processor.javadoc.JavaDocReader;

public sealed interface SpecProperty permits SpecEnumeration, SpecMapping, SpecSingleton {
    String name();
    Property.Source source();
    String scope();
    Meta meta();

    record Meta(boolean internal, JavaDocReader.Comment comment) {}
}
