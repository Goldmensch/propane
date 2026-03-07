package dev.goldmensch.propane.spec.processor.generator;

import com.palantir.javapoet.*;
import dev.goldmensch.propane.property.*;
import dev.goldmensch.propane.spec.processor.syntax.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PropertyGenerator extends AbstractGenerator<PropertyGenerator.PropertyMeta> {

    private static final String SINGLETON = "SingletonProperty";
    private static final String COLLECTION = "CollectionProperty";
    private static final String MAPPING = "MappingProperty";



    public PropertyGenerator(PackageElement packageName, Filer filer) {
        super(packageName, filer);
    }

    @Override
    List<Function<PropertyGenerator.PropertyMeta, TypeSpec>> generators(PropertyMeta meta) {
        ClassName name = ClassName.get(packageName, meta.name("Property"));
        return List.of(
                _ -> specificProperty(meta, name),
                _ -> singletonProperty(meta, name),
                _ -> collectionProperty(meta, name),
                _ -> mappingProperty(meta, name)

        );
    }

    public record PropertyMeta(SpecMeta spec, List<? extends SpecProperty> properties, TypeMirror scopeClass) {
        String name(String name) {
            return spec().prefix() + name;
        }
    }

    private TypeSpec specificProperty(PropertyMeta meta, ClassName className) {
        ClassName singletonClassName = ClassName.get(packageName, meta.name(SINGLETON));
        ClassName collectionClassName = ClassName.get(packageName, meta.name(COLLECTION));
        ClassName mappingClassName = ClassName.get(packageName, meta.name(MAPPING));

        TypeSpec.Builder builder = TypeSpec.interfaceBuilder(className)
                .addTypeVariable(TypeVariableName.get("T"))
                .addSuperinterface(withTGeneric(ClassName.get(SpecificProperty.class)))
                .addModifiers(Modifier.PUBLIC, Modifier.SEALED)
                .addPermittedSubclass(singletonClassName)
                .addPermittedSubclass(collectionClassName)
                .addPermittedSubclass(mappingClassName);

        for (SpecProperty property : meta.properties()) {
            FieldSpec field = switch (property) {
                case SpecSingleton(String name, Property.Source source, String scope, TypeElement type) -> {
                    ParameterizedTypeName fieldType = ParameterizedTypeName.get(className, ClassName.get(type));

                    yield FieldSpec.builder(fieldType, name, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .initializer("new $T($S, $T.$L, $T.$L, $T.class)",
                                    singletonClassName, name,
                                    Property.Source.class, source,
                                    meta.scopeClass, scope,
                                    type)
                            .build();
                }

                case SpecEnumeration(String name, Property.Source source, String scope, TypeElement type,
                                     Property.FallbackBehaviour fallback) -> {
                    ParameterizedTypeName fieldType = ParameterizedTypeName.get(className, ParameterizedTypeName.get(ClassName.get(Collection.class), ClassName.get(type)));

                    yield FieldSpec.builder(fieldType, name, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .initializer("new $T($S, $T.$L, $T.$L, $T.class, $T.$L)",
                                    collectionClassName, name,
                                    Property.Source.class, source,
                                    meta.scopeClass, scope,
                                    type,
                                    Property.FallbackBehaviour.class, fallback)
                            .build();
                }

                case SpecMapping(String name, Property.Source source, String scope,
                                 TypeElement keyType,
                                 TypeElement valueType,
                                 Property.FallbackBehaviour fallback) -> {
                    ParameterizedTypeName fieldType = ParameterizedTypeName.get(className, ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(keyType), ClassName.get(valueType)));

                    yield FieldSpec.builder(fieldType, name, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .initializer("new $T($S, $T.$L, $T.$L, $T.class, $T.class, $T.$L)",
                                    mappingClassName, name,
                                    Property.Source.class, source,
                                    meta.scopeClass, scope,
                                    keyType,
                                    valueType,
                                    Property.FallbackBehaviour.class, fallback)
                            .build();
                }
            };

            builder.addField(field);
        }

        return builder.build();
    }

    private TypeSpec singletonProperty(PropertyGenerator.PropertyMeta meta, ClassName specificProperty) {
        return TypeSpec.classBuilder(meta.name(SINGLETON))
                .addModifiers(Modifier.FINAL)
                .addTypeVariable(TypeVariableName.get("T"))
                .superclass(withTGeneric(ClassName.get(SingleProperty.class)))
                .addSuperinterface(withTGeneric(specificProperty))
                .addMethod(propertySuperConstructor()
                        .addParameter(withTGeneric(ClassName.get(Class.class)), "type")
                        .addStatement("super(name, source, scope, type)")
                        .build())
                .build();
    }

    private TypeSpec collectionProperty(PropertyGenerator.PropertyMeta meta, ClassName specificProperty) {
        return TypeSpec.classBuilder(meta.name(COLLECTION))
                .addModifiers(Modifier.FINAL)
                .addTypeVariable(TypeVariableName.get("T"))
                .superclass(withTGeneric(ClassName.get(CollectionProperty.class)))
                .addSuperinterface(ParameterizedTypeName.get(specificProperty, withTGeneric(ClassName.get(Collection.class))))
                .addMethod(propertySuperConstructor()
                        .addParameter(withTGeneric(ClassName.get(Class.class)), "type")
                        .addParameter(Property.FallbackBehaviour.class, "fallback")
                        .addStatement("super(name, source, scope, type, fallback)")
                        .build())
                .build();
    }

    private TypeSpec mappingProperty(PropertyGenerator.PropertyMeta meta, ClassName specificProperty) {
        TypeVariableName[] typeVariables = {TypeVariableName.get("K"), TypeVariableName.get("V")};
        ParameterizedTypeName mapTypeName = ParameterizedTypeName.get(ClassName.get(Map.class), typeVariables);

        return TypeSpec.classBuilder(meta.name(MAPPING))
                .addModifiers(Modifier.FINAL)
                .addTypeVariables(List.of(typeVariables))
                .superclass(ParameterizedTypeName.get(ClassName.get(MapProperty.class), typeVariables))
                .addSuperinterface(ParameterizedTypeName.get(specificProperty, mapTypeName))
                .addMethod(propertySuperConstructor()
                        .addParameter(withGeneric(ClassName.get(Class.class), "K"), "keyType")
                        .addParameter(withGeneric(ClassName.get(Class.class), "V"), "valueType")
                        .addParameter(Property.FallbackBehaviour.class, "fallback")
                        .addStatement("super(name, source, scope, keyType, valueType, fallback)")
                        .build())
                .build();
    }

    private TypeName withGeneric(ClassName typeName, String... variables) {
        TypeVariableName[] names = Arrays.stream(variables)
                .map(TypeVariableName::get)
                .toArray(TypeVariableName[]::new);

        return ParameterizedTypeName.get(typeName, names);
    }

    private TypeName withTGeneric(ClassName typeName) {
        return withGeneric(typeName, "T");
    }

    private MethodSpec.Builder propertySuperConstructor() {
        return MethodSpec.constructorBuilder()
                .addParameter(String.class, "name")
                .addParameter(Property.Source.class, "source")
                .addParameter(Property.Scope.class, "scope");
    }
}
