package dev.goldmensch.propane.spec.generator;

import com.palantir.javapoet.*;
import dev.goldmensch.propane.Scope;
import dev.goldmensch.propane.event.Event;
import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.spec.annotation.GeneratedForSpec;
import dev.goldmensch.propane.spec.processor.syntax.SpecEnumeration;
import dev.goldmensch.propane.spec.processor.syntax.SpecMapping;
import dev.goldmensch.propane.spec.processor.syntax.SpecMeta;
import dev.goldmensch.propane.spec.processor.syntax.SpecSingleton;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class DslGenerator extends AbstractGenerator<SpecMeta> {

    private ClassName scopeName;

    public DslGenerator(PackageElement pkg, Filer filer) {
        super(pkg, filer);
    }

    @Override
    Map<String, List<Supplier<TypeSpec>>> generators(SpecMeta meta) {
        this.scopeName = ClassName.get(packageName, meta.prefix() + "Scope");
        return Map.of("", List.of(
                    () -> generateScope(meta),
                    () -> generateSingleton(meta),
                    () -> generateMulti(SpecEnumeration.ANNOTATION),
                    () -> generateMulti(SpecMapping.ANNOTATION)
                ),
                "internal", List.of(
                        this::generateEventAnnotation,
                        () -> eventInterface(meta)
                ));
    }

    private TypeSpec generateScope(SpecMeta meta) {
        var specClass = ClassName.get(packageName, meta.specClass());

        TypeSpec.Builder builder = TypeSpec.enumBuilder(scopeName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(GeneratedForSpec.class)
                        .addMember("spec", "$T.class", specClass)
                        .build())
                .addSuperinterface(Scope.class);

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
                        .returns(scopeName)
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

    private TypeSpec generateEventAnnotation() {
        return TypeSpec.annotationBuilder("Event")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Retention.class)
                        .addMember("value", "$T.SOURCE", RetentionPolicy.class)
                        .build())
                .addAnnotation(AnnotationSpec.builder(Target.class)
                        .addMember("value", "$T.TYPE", ElementType.class)
                        .build())
                .addMethod(MethodSpec.methodBuilder("value")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(scopeName)
                        .build())
                .build();
    }

    private TypeSpec eventInterface(SpecMeta meta) {
        ClassName registry = ClassName.get(packageName + ".internal", "Registry");

        return TypeSpec.interfaceBuilder(meta.prefix() + "Event")
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(Event.class), scopeName))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(MethodSpec.methodBuilder("scope")
                        .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                        .returns(scopeName)
                        .addStatement("return $T.INSTANCE.scopeForEvent(this.getClass())", registry)
                        .build())
                .build();
    }
}
