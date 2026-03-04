package dev.goldmensch.propane.spec.processor;

import com.palantir.javapoet.*;
import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.spec.processor.syntax.SpecMeta;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.function.Function;

public class DSLGenerator {

    private final List<Function<SpecMeta, JavaFile>> generators = List.of(
            this::generateScope,
            this::generateSingleton,
            meta -> generateMulti(meta, "Collection"),
            meta -> generateMulti(meta, "Map")
    );


    private final String packageName;
    private final Filer filer;
    private @Nullable ClassName scopeClass;

    public DSLGenerator(PackageElement pkg, Filer filer) {
        this.packageName = pkg.getQualifiedName().toString();
        this.filer = filer;
    }

    public void generate(SpecMeta meta) {
        for (Function<SpecMeta, JavaFile> generator : generators) {
            JavaFile file = generator.apply(meta);
            try {
                file.writeTo(filer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private JavaFile generateScope(SpecMeta meta) {
        this.scopeClass = ClassName.get(packageName, meta.prefix() + "Scope");

        TypeSpec.Builder builder = TypeSpec.enumBuilder(scopeClass)
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



        return JavaFile.builder(packageName, builder.build()).build();
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

    private JavaFile generateSingleton(SpecMeta meta) {
        TypeSpec typeSpec = annotationBuilder("Singleton").build();

        return JavaFile.builder(packageName, typeSpec).build();
    }

    private JavaFile generateMulti(SpecMeta meta, String name) {
        TypeSpec typeSpec = annotationBuilder(name)
                .addMethod(MethodSpec.methodBuilder("fallbackBehaviour")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(Property.FallbackBehaviour.class)
                        .build()
                )
                .build();

        return JavaFile.builder(packageName, typeSpec).build();
    }
}
