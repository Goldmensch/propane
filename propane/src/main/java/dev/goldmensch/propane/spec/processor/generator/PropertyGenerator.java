package dev.goldmensch.propane.spec.processor.generator;

import com.palantir.javapoet.*;
import dev.goldmensch.propane.Introspection;
import dev.goldmensch.propane.PropertyProvider;
import dev.goldmensch.propane.internal.exposed.Properties;
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
        ClassName providerClassName = ClassName.get(packageName, meta.name("PropertyProvider"));
        ClassName introspectionClassName = ClassName.get(packageName, meta.name("Introspection"));
        ClassName specificClassName = ClassName.get(packageName, meta.name("Property"));
        return List.of(
                _ -> introspection(introspectionClassName, providerClassName),
                _ -> specificProperty(meta, specificClassName),
                _ -> singletonProperty(meta, specificClassName),
                _ -> collectionProperty(meta, specificClassName),
                _ -> mappingProperty(meta, specificClassName),
                _ -> provider(introspectionClassName, providerClassName, specificClassName)

        );
    }

    public record PropertyMeta(SpecMeta spec, List<? extends SpecProperty> properties, TypeMirror scopeClass) {
        String name(String name) {
            return spec().prefix() + name;
        }
    }

    private TypeSpec provider(ClassName introspectionClassName, ClassName providerClassName, ClassName specificProperty) {
        TypeVariableName t = TypeVariableName.get("T");
        ParameterizedTypeName superClass = ParameterizedTypeName.get(ClassName.get(PropertyProvider.class),
                t,
                withTGeneric(specificProperty),
                introspectionClassName);

        ParameterizedTypeName function = ParameterizedTypeName.get(ClassName.get(Function.class),
                introspectionClassName, t);

        return TypeSpec.classBuilder(providerClassName)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .superclass(superClass)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(withTGeneric(specificProperty), "property")
                        .addParameter(PropertyProvider.Priority.class, "priority")
                        .addParameter(WildcardTypeName.get(Class.class), "owner")
                        .addParameter(function, "supplier")
                        .addStatement("super(property, priority, owner, supplier)")
                        .build())
                .build();
    }

    private TypeSpec introspection(ClassName introspectionClassName, ClassName providerClassName) {
        TypeName providerWildcard = withGeneric(providerClassName, "?");
        ClassName builderClassName = ClassName.get(packageName, introspectionClassName.simpleName(), "Builder");
        ParameterizedTypeName parentIntrospection = ParameterizedTypeName.get(ClassName.get(Introspection.class),
                introspectionClassName, builderClassName, providerWildcard);

        return TypeSpec.classBuilder(introspectionClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(parentIntrospection)
                .addField(FieldSpec.builder(introspectionClassName, "EMPTY", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("new $T()", introspectionClassName)
                        .build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(Property.Scope.class, "scope")
                        .addParameter(ParameterizedTypeName.get(ClassName.get(Properties.class), introspectionClassName), "properties")
                        .addParameter(introspectionClassName, "parent")
                        .addStatement("super(scope, properties, parent)")
                        .build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addStatement("super()")
                        .build())
                .addMethod(MethodSpec.methodBuilder("create")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(builderClassName)
                        .addParameter(Property.Scope.class, "scope")
                        .addStatement("return EMPTY.createChild(scope)")
                        .build())
                .addMethod(MethodSpec.methodBuilder("createChild")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(builderClassName)
                        .addParameter(Property.Scope.class, "scope")
                        .addStatement("return this.new $T(scope)", builderClassName)
                        .build())
                .addType(TypeSpec.classBuilder(builderClassName)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .superclass(parentIntrospection.nestedClass("Builder"))
                        .addMethod(MethodSpec.constructorBuilder()
                                .addParameter(Property.Scope.class, "scope")
                                .addStatement("super(scope)")
                                .build())
                        .addMethod(MethodSpec.methodBuilder("newInstance")
                                .returns(introspectionClassName)
                                .addModifiers(Modifier.PROTECTED)
                                .addAnnotation(Override.class)
                                .addStatement("return new $T(scope, properties, $T.this)", introspectionClassName, introspectionClassName)
                                .build())
                        .build())
                .build();
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
