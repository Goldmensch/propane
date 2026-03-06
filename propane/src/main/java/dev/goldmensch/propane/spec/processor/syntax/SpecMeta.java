package dev.goldmensch.propane.spec.processor.syntax;

import com.palantir.javapoet.ClassName;

public record SpecMeta(
        String prefix,
        String[] scopes,
        String specClass
) {

}
