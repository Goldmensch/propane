package dev.goldmensch.propane.spec.processor.syntax;

import dev.goldmensch.propane.property.Property;

import javax.lang.model.element.TypeElement;

public record SpecEnumeration(
        String name,
        Property.Source source,
        String scope,
        TypeElement type,
        Property.FallbackBehaviour fallbackBehaviour,
        boolean internal
) implements SpecProperty {
    public static final String ANNOTATION = "Enumeration";
}
