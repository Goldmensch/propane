package dev.goldmensch.propane.spec.processor.syntax;

import javax.lang.model.element.TypeElement;

public record SpecEvent(
        TypeElement event,
        String scope
) {
}
