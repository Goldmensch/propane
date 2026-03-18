package dev.goldmensch.propane.spec.generator;

import com.palantir.javapoet.*;
import dev.goldmensch.propane.Introspection;
import dev.goldmensch.propane.IntrospectionImpl;
import dev.goldmensch.propane.PropertyProvider;
import dev.goldmensch.propane.Registry;
import dev.goldmensch.propane.internal.exposed.Properties;
import dev.goldmensch.propane.property.*;
import dev.goldmensch.propane.spec.processor.syntax.*;
import org.jspecify.annotations.Nullable;

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
import java.util.function.Supplier;

public class PropertyGenerator extends AbstractGenerator<PropertyGenerator.PropertyMeta> {

    private static final TypeVariableName T = TypeVariableName.get("T");

    private static final String SINGLETON = "SingletonProperty";
    private static final String COLLECTION = "CollectionProperty";
    private static final String MAPPING = "MappingProperty";

    public PropertyGenerator(PackageElement packageName, Filer filer) {
        super(packageName, filer);
    }

    private PropertyMeta meta;
    private ClassName providerName;
    private ClassName introspectionName;
    private ClassName introspectionImplName;
    private ClassName specificName;
    private ClassName internalPropertiesName;
    private ClassName builderName;
    private ClassName scopeName;

    private ClassName singletonClassName;
    private ClassName collectionClassName;
    private ClassName mappingClassName;


    @Override
    Map<String, List<Supplier<@Nullable TypeSpec>>> generators(PropertyMeta meta) {
        String internalPackage = packageName + ".internal";

        this.meta = meta;
        scopeName = (ClassName) ClassName.get(meta.scopeClass);

        // public api
        providerName = ClassName.get(packageName, meta.name("PropertyProvider"));
        introspectionName = ClassName.get(packageName, meta.name("Introspection"));
        specificName = ClassName.get(packageName, meta.name("Property"));

        // internal
        introspectionImplName = ClassName.get(internalPackage, meta.name("IntrospectionImpl"));
        builderName = ClassName.get(introspectionImplName.packageName(), introspectionImplName.simpleName(), "Builder");

        internalPropertiesName = ClassName.get(internalPackage, meta.name("Internal"));
        singletonClassName = ClassName.get(internalPackage, meta.name(SINGLETON));
        collectionClassName = ClassName.get(internalPackage, meta.name(COLLECTION));
        mappingClassName = ClassName.get(internalPackage, meta.name(MAPPING));

        List<Supplier<TypeSpec>> root = List.of(
                this::specificProperty,
                this::introspection,
                this::provider
        );

        return Map.of(
                "", root,
                "internal", List.of(
                        this::introspectionImpl,
                        this::internalProperties,
                        this::singletonProperty,
                        this::collectionProperty,
                        this::mappingProperty
                )
        );
    }

    public record PropertyMeta(SpecMeta spec, List<? extends SpecProperty> properties, TypeMirror scopeClass) {
        String name(String name) {
            return spec().prefix() + name;
        }
    }

    private TypeSpec provider() {
        ParameterizedTypeName superClass = ParameterizedTypeName.get(ClassName.get(PropertyProvider.class),
                T,
                withTGeneric(specificName),
                introspectionName);

        ParameterizedTypeName function = ParameterizedTypeName.get(ClassName.get(Function.class),
                introspectionName, T);

        return TypeSpec.classBuilder(providerName)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(T)
                .superclass(superClass)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(withTGeneric(specificName), "property")
                        .addParameter(PropertyProvider.Priority.class, "priority")
                        .addParameter(withGeneric(ClassName.get(Class.class), "?"), "owner")
                        .addParameter(function, "supplier")
                        .addStatement("super(property, priority, owner, supplier)")
                        .build())
                .build();
    }

    private TypeSpec introspection() {
        return TypeSpec.interfaceBuilder(introspectionName)
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(Introspection.class), introspectionName, scopeName))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(MethodSpec.methodBuilder("get")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addParameter(withTGeneric(specificName), "specific")
                        .returns(T)
                        .addTypeVariable(T)
                        .build()
                )
                .addMethod(MethodSpec.methodBuilder("accessible")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(boolean.class)
                        .addStatement("return $T.INTROSPECTION.isBound()", introspectionImplName)
                        .build())
                .addMethod(MethodSpec.methodBuilder("accessScoped")
                        .returns(introspectionName)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addStatement("return $T.INTROSPECTION.get()", introspectionImplName)
                        .build())
                .addMethod(MethodSpec.methodBuilder("scopedGet")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(T)
                        .addTypeVariable(T)
                        .addParameter(withTGeneric(specificName), "property")
                        .addStatement("return accessScoped().get(property)")
                        .build())
                .build();
    }

    private TypeSpec introspectionImpl() {
        ParameterizedTypeName parentIntrospection = ParameterizedTypeName.get(ClassName.get(IntrospectionImpl.class),
                introspectionImplName, introspectionName, builderName, scopeName);

        return TypeSpec.classBuilder(introspectionImplName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(parentIntrospection)
                .addSuperinterface(introspectionName)
                .addField(FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(ScopedValue.class), introspectionImplName), "INTROSPECTION", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$T.newInstance()", ScopedValue.class)
                        .build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PRIVATE)
                        .addParameter(scopeName, "scope")
                        .addParameter(ParameterizedTypeName.get(ClassName.get(Properties.class), introspectionName), "properties")
                        .addParameter(introspectionImplName, "parent")
                        .addStatement("super(scope, properties, parent)")
                        .build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PRIVATE)
                        .addParameter(scopeName, "scope")
                        .addStatement("super(new $T<>($T.of()), scope)", Registry.class, Map.class)
                        .build())
                .addMethod(MethodSpec.methodBuilder("create")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(builderName)
                        .addParameter(scopeName, "scope")
                        .addStatement("return new $T(scope).createChild(scope)", introspectionImplName)
                        .build())
                .addMethod(MethodSpec.methodBuilder("createChild")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(builderName)
                        .addParameter(scopeName, "scope")
                        .addStatement("return this.new $T(scope)", builderName)
                        .build())
                .addMethod(MethodSpec.methodBuilder("get")
                        .addTypeVariable(T)
                        .returns(T)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(withTGeneric(specificName), "specific")
                        .addStatement("return super.get(specific)")
                        .build())
                .addMethod(MethodSpec.methodBuilder("addIntrospectionProvider")
                        .addModifiers(Modifier.PROTECTED)
                        .addParameter(ParameterizedTypeName.get(ClassName.get(Properties.class), introspectionName), "properties")
                        .addStatement("properties.add(new $T<>($T.INTROSPECTION, $T.FALLBACK, $T.class, _ -> this))",
                                providerName, specificName, PropertyProvider.Priority.class, introspectionName)
                        .build())
                .addType(TypeSpec.classBuilder(builderName)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .superclass(parentIntrospection.nestedClass("Builder"))
                        .addMethod(MethodSpec.constructorBuilder()
                                .addParameter(scopeName, "scope")
                                .addStatement("super(scope)")
                                .build())
                        .addMethod(MethodSpec.methodBuilder("newInstance")
                                .returns(introspectionImplName)
                                .addModifiers(Modifier.PROTECTED)
                                .addAnnotation(Override.class)
                                .addStatement("return new $T(scope, properties, $T.this)", introspectionImplName, introspectionImplName)
                                .build())
                        .addMethod(addX("FALLBACK", false))
                        .addMethod(addX("BUILDER",false))
                        .addMethod(addX("FALLBACK", true))
                        .addMethod(addX("BUILDER",true))
                        .build())
                .build();
    }

    private MethodSpec addX(String name, boolean ownerExplicit) {
        StringBuilder nameBuilder = new StringBuilder(name.toLowerCase());
        nameBuilder.setCharAt(0, Character.toUpperCase(nameBuilder.charAt(0)));
        nameBuilder.insert(0, "add");

        ParameterizedTypeName function = ParameterizedTypeName.get(ClassName.get(Function.class),
                introspectionName, T);

        String owner = ownerExplicit
                ? "owner"
                : "caller()";

        MethodSpec.Builder builder = MethodSpec.methodBuilder(nameBuilder.toString())
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(T)
                .returns(builderName)
                .addParameter(withTGeneric(specificName), "property");

        if (ownerExplicit) {
            builder.addParameter(withGeneric(ClassName.get(Class.class), "?"), "owner");
        }

        return builder
                .addParameter(function, "supplier")
                .addStatement("return add(new $T<>(property, $T.$L, $L, supplier))", providerName, PropertyProvider.Priority.class, name, owner)
                .build();
    }

    private @Nullable TypeSpec internalProperties() {
        TypeSpec.Builder builder = TypeSpec.classBuilder(internalPropertiesName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PRIVATE)
                        .build());

        meta.properties()
                .stream()
                .filter(SpecProperty::internal)
                .map(this::toField)
                .forEach(builder::addField);

        return meta.properties().stream().anyMatch(SpecProperty::internal)
                ? builder.build()
                : null;
    }

    private TypeSpec specificProperty() {
         TypeSpec.Builder builder = TypeSpec.interfaceBuilder(specificName)
                .addTypeVariable(TypeVariableName.get("T"))
                .addSuperinterface(withTGeneric(ClassName.get(SpecificProperty.class)))
                .addModifiers(Modifier.PUBLIC);

        FieldSpec introspectionProperty = FieldSpec
                .builder(
                        ParameterizedTypeName.get(specificName, introspectionName),
                        "INTROSPECTION",
                        Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T<>($S, $T.PROVIDED, $T.$L, $T.class)",
                        singletonClassName, "INTROSPECTION",
                        Property.Source.class,
                        scopeName, meta.spec.scopes()[0], // first scope is always highest priority -> Enum.ordinal
                        introspectionName)
                .build();
        builder.addField(introspectionProperty);

        meta.properties()
                .stream()
                .filter(property -> !property.internal())
                .map(this::toField)
                .forEach(builder::addField);

        return builder.build();
    }

    private FieldSpec toField(SpecProperty property) {
        return switch (property) {
            case SpecSingleton(String name, Property.Source source, String scope, TypeElement type, var _) -> {
                ParameterizedTypeName fieldType = ParameterizedTypeName.get(specificName, ClassName.get(type));

                yield FieldSpec.builder(fieldType, name, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("new $T<>($S, $T.$L, $T.$L, $T.class)",
                                singletonClassName, name,
                                Property.Source.class, source,
                                scopeName, scope,
                                type)
                        .build();
            }

            case SpecEnumeration(String name,
                                 Property.Source source,
                                 String scope,
                                 TypeElement type,
                                 Property.FallbackBehaviour fallback,
                                 var _) -> {
                ParameterizedTypeName fieldType = ParameterizedTypeName.get(specificName, ParameterizedTypeName.get(ClassName.get(Collection.class), ClassName.get(type)));

                yield FieldSpec.builder(fieldType, name, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("new $T<>($S, $T.$L, $T.$L, $T.class, $T.$L)",
                                collectionClassName, name,
                                Property.Source.class, source,
                                scopeName, scope,
                                type,
                                Property.FallbackBehaviour.class, fallback)
                        .build();
            }

            case SpecMapping(String name, Property.Source source, String scope,
                             TypeElement keyType,
                             TypeElement valueType,
                             Property.FallbackBehaviour fallback,
                             var _) -> {
                ParameterizedTypeName fieldType = ParameterizedTypeName.get(specificName, ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(keyType), ClassName.get(valueType)));

                yield FieldSpec.builder(fieldType, name, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("new $T<>($S, $T.$L, $T.$L, $T.class, $T.class, $T.$L)",
                                mappingClassName, name,
                                Property.Source.class, source,
                                scopeName, scope,
                                keyType,
                                valueType,
                                Property.FallbackBehaviour.class, fallback)
                        .build();
            }
        };
    }

    private TypeSpec singletonProperty() {
        return TypeSpec.classBuilder(meta.name(SINGLETON))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addTypeVariable(TypeVariableName.get("T"))
                .superclass(withTGeneric(ClassName.get(SingleProperty.class)))
                .addSuperinterface(withTGeneric(specificName))
                .addMethod(propertySuperConstructor()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(withTGeneric(ClassName.get(Class.class)), "type")
                        .addStatement("super(name, source, scope, type)")
                        .build())
                .addMethod(getScoped(T))
                .build();
    }

    private TypeSpec collectionProperty() {
        TypeName collectionType = withTGeneric(ClassName.get(Collection.class));
        return TypeSpec.classBuilder(meta.name(COLLECTION))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addTypeVariable(TypeVariableName.get("T"))
                .superclass(withTGeneric(ClassName.get(CollectionProperty.class)))
                .addSuperinterface(ParameterizedTypeName.get(specificName, collectionType))
                .addMethod(propertySuperConstructor()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(withTGeneric(ClassName.get(Class.class)), "type")
                        .addParameter(Property.FallbackBehaviour.class, "fallback")
                        .addStatement("super(name, source, scope, type, fallback)")
                        .build())
                .addMethod(getScoped(collectionType))
                .build();
    }



    private TypeSpec mappingProperty() {
        TypeVariableName[] typeVariables = {TypeVariableName.get("K"), TypeVariableName.get("V")};
        ParameterizedTypeName mapTypeName = ParameterizedTypeName.get(ClassName.get(Map.class), typeVariables);

        return TypeSpec.classBuilder(meta.name(MAPPING))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addTypeVariables(List.of(typeVariables))
                .superclass(ParameterizedTypeName.get(ClassName.get(MapProperty.class), typeVariables))
                .addSuperinterface(ParameterizedTypeName.get(specificName, mapTypeName))
                .addMethod(propertySuperConstructor()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(withGeneric(ClassName.get(Class.class), "K"), "keyType")
                        .addParameter(withGeneric(ClassName.get(Class.class), "V"), "valueType")
                        .addParameter(Property.FallbackBehaviour.class, "fallback")
                        .addStatement("super(name, source, scope, keyType, valueType, fallback)")
                        .build())
                .addMethod(getScoped(mapTypeName))
                .build();
    }

    private MethodSpec getScoped(TypeName returnType) {
        return MethodSpec.methodBuilder("getScoped")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(returnType)
                .addStatement("return $T.scopedGet(this)", introspectionName)
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
                .addParameter(scopeName, "scope");
    }
}
