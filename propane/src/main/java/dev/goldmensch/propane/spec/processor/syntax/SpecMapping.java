package dev.goldmensch.propane.spec.processor.syntax;

import dev.goldmensch.propane.property.Property;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public record SpecMapping(
        String name,
        Property.Source source,
        String scope,
        TypeElement keyTpe,
        TypeElement valueType,
        Property.FallbackBehaviour fallbackBehaviour,
        boolean internal
) implements SpecProperty {
    public static final String ANNOTATION = "Mapping";
}
