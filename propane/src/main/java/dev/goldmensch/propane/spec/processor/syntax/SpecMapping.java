package dev.goldmensch.propane.spec.processor.syntax;

import dev.goldmensch.propane.property.Property;

import javax.lang.model.element.TypeElement;

public record SpecMapping(
        String name,
        Property.Source source,
        String scope,
        TypeElement keyTpe,
        TypeElement valueType,
        Property.FallbackStrategy fallbackStrategy,
        boolean internal
) implements SpecProperty {
    public static final String ANNOTATION = "Mapping";
}
