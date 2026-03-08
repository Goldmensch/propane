package dev.goldmensch.propane.spec.processor.generator;

import com.palantir.javapoet.*;
import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.spec.annotation.GeneratedForSpec;
import dev.goldmensch.propane.spec.processor.syntax.SpecEnumeration;
import dev.goldmensch.propane.spec.processor.syntax.SpecMapping;
import dev.goldmensch.propane.spec.processor.syntax.SpecMeta;
import dev.goldmensch.propane.spec.processor.syntax.SpecSingleton;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.function.Function;

public class DslGenerator extends AbstractGenerator<SpecMeta> {

    private @Nullable ClassName scopeClass;

    public DslGenerator(PackageElement pkg, Filer filer) {
        super(pkg, filer);
    }

    @Override
    List<Function<SpecMeta, TypeSpec>> generators(SpecMeta meta) {
        return List.of(
                this::generateScope,
                this::generateSingleton,
                _ -> generateMulti(SpecEnumeration.ANNOTATION),
                _ -> generateMulti(SpecMapping.ANNOTATION)
        );
    }

    private TypeSpec generateScope(SpecMeta meta) {
        var specClass = ClassName.get(packageName, meta.specClass());
        this.scopeClass = ClassName.get(packageName, meta.prefix() + "Scope");

        TypeSpec.Builder builder = TypeSpec.enumBuilder(scopeClass)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(GeneratedForSpec.class)
                        .addMember("spec", "$T.class", specClass)
                        .build())
                .addSuperinterface(Property.Scope.class);

        for (String name : meta.scopes()) {
            builder.addEnumConstant(name);
        }

        builder.addMethod(MethodSpec.methodBuilder("priority")
                .returns(int.class)
                .addStatement("return this.ordinal()")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .build());

        return builder.build();
    }

    private TypeSpec.Builder annotationBuilder(String name) {
        return TypeSpec.annotationBuilder(name)
                .addAnnotation(AnnotationSpec.builder(Retention.class)
                        .addMember("value", "$T.SOURCE", RetentionPolicy.class)
                        .build())
                .addAnnotation(AnnotationSpec.builder(Target.class)
                        .addMember("value", "$T.METHOD", ElementType.class)
                        .build())
                .addMethod(MethodSpec.methodBuilder("source")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(Property.Source.class)
                        .build())
                .addMethod(MethodSpec.methodBuilder("scope")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(scopeClass)
                        .build());
    }

    private TypeSpec generateSingleton(SpecMeta meta) {
        return annotationBuilder(SpecSingleton.ANNOTATION).build();
    }

    private TypeSpec generateMulti(String name) {
        return annotationBuilder(name)
                .addMethod(MethodSpec.methodBuilder("fallback")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(Property.FallbackBehaviour.class)
                        .build()
                )
                .build();
    }
}
