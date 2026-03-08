package dev.goldmensch.propane.spec.processor;

import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.spec.annotation.GeneratedForSpec;
import dev.goldmensch.propane.spec.annotation.Propane;
import dev.goldmensch.propane.spec.annotation.Scopes;
import dev.goldmensch.propane.spec.processor.generator.DslGenerator;
import dev.goldmensch.propane.spec.processor.generator.PropertyGenerator;
import dev.goldmensch.propane.spec.processor.syntax.*;
import dev.goldmensch.propane.spec.processor.util.TriFunction;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.*;
import java.util.*;
import java.util.stream.Stream;

@SuppressWarnings("NotNullFieldNotInitialized")
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public class SpecProcessor extends AbstractProcessor {

    private static final SpecMeta FINISHED = new SpecMeta("", new String[]{}, "");

    private final Map<Name, SpecMeta> metadata = new HashMap<>();

    private Messager messager;
    private Types types;
    private Elements elements;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.types = processingEnv.getTypeUtils();
        this.elements = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element klass : roundEnv.getElementsAnnotatedWith(Propane.class)) {
            Name qualifiedName = ((TypeElement) klass).getQualifiedName();
            if (metadata.containsKey(qualifiedName)) {
                continue; // already ran in previous round
            }
            
            Scopes scopes = klass.getAnnotation(Scopes.class);
            Propane propane = klass.getAnnotation(Propane.class);
            if (scopes == null || propane == null) {
                messager.printError("@Scope and @Propane must be present on property spec class!", klass);
                continue;
            }

            PackageElement pkg = elements.getPackageOf(klass);
            DslGenerator dslGenerator = new DslGenerator(pkg, processingEnv.getFiler());

            String prefix = getPrefix(propane, klass.getSimpleName().toString());
            if (prefix == null) {
                continue;
            }

            SpecMeta meta = new SpecMeta(prefix, scopes.value(), klass.getSimpleName().toString());
            metadata.put(qualifiedName, meta);

            dslGenerator.generate(meta);
        }

        for (Element scopeKlass : roundEnv.getElementsAnnotatedWith(GeneratedForSpec.class)) {
            AnnotationMirror ann = getAnnotation(scopeKlass, GeneratedForSpec.class.getSimpleName()).orElseThrow();
            TypeElement spec = getValue(ann, "spec").accept(new TypeElementExtractor(), null);
            SpecMeta specMeta = metadata.get(spec.getQualifiedName());

            if (specMeta == FINISHED) {
                continue; // already ran in previous round
            }

            List<? extends SpecProperty> properties = readProperties(spec);
            PropertyGenerator generator = new PropertyGenerator(elements.getPackageOf(spec), processingEnv.getFiler());
            generator.generate(new PropertyGenerator.PropertyMeta(specMeta, properties, scopeKlass.asType()));

            metadata.put(spec.getQualifiedName(), FINISHED);
        }

        return true;
    }

    private @Nullable String getPrefix(Propane propane, String simpleClassName) {
        String annValue = propane.value();
        if (annValue.isEmpty()) {
            if (!simpleClassName.endsWith("PropertySpec")) {
                messager.printError("Classname of property specification must end with 'PropertySpec' if prefix should be extracted!");
                return null;
            }

            int last = simpleClassName.lastIndexOf("PropertySpec");
            return simpleClassName.substring(0, last);
        }

        return annValue;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(Propane.class.getName(), GeneratedForSpec.class.getName());
    }

    private Optional<? extends AnnotationMirror> getAnnotation(Element element, String name) {
        return element.getAnnotationMirrors()
                .stream()
                .filter(mirror -> mirror.getAnnotationType().asElement().getSimpleName().contentEquals(name))
                .findFirst();
    }

    private AnnotationValue getValue(AnnotationMirror mirror, String name) {
        return mirror.getElementValues().entrySet()
                .stream()
                .filter(entry -> entry.getKey().getSimpleName().contentEquals(name))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow();
    }

    private String getEnumConstant(AnnotationMirror mirror, String name) {
        return getValue(mirror, name).accept(new EnumConstantExtract(), null);
    }

    private class TypeElementExtractor extends SimpleAnnotationValueVisitor14<TypeElement, Void> {
        @Override
        public TypeElement visitType(TypeMirror t, Void unused) {
            return (TypeElement) types.asElement(t);
        }
    }

    private static class EnumConstantExtract extends SimpleAnnotationValueVisitor14<String, Void> {
        @Override
        public String visitEnumConstant(VariableElement c, Void unused) {
            return c.getSimpleName().toString();
        }
    }

    private class TypeArgumentExtractor extends SimpleTypeVisitor14<List<TypeElement>, Void> {
        @Override
        public List<TypeElement> visitDeclared(DeclaredType t, Void unused) {
            return t.getTypeArguments()
                    .stream()
                    .map(typeMirror -> (TypeElement) types.asElement(typeMirror))
                    .toList();
        }
    }

    private TypeMirror asType(Class<?> klass) {
        return elements.getTypeElement(klass.getName()).asType();
    }

    private final List<TriFunction<String, TypeMirror, Element, Optional<? extends SpecProperty>>> specExtractors = List.of(
            this::specSingleton,
            this::specCollection,
            this::specMapping
    );

    private List<? extends SpecProperty> readProperties(TypeElement klass) {
        return klass.getEnclosedElements()
                .stream()
                .filter(element -> element.getKind() == ElementKind.METHOD)
                .flatMap(element -> {
                    String name = element.getSimpleName().toString();
                    TypeMirror type = ((ExecutableElement) element).getReturnType();

                    for (var specExtractor : specExtractors) {
                        Optional<? extends SpecProperty> result = specExtractor.accept(name, type, element);
                        if (result.isPresent()) {
                            return result.stream();
                        }
                    }

                    return Stream.empty();
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private Optional<SpecSingleton> specSingleton(String name, TypeMirror type, Element element) {
        return getAnnotation(element, SpecSingleton.ANNOTATION)
                .map(mirror -> new SpecSingleton(
                        name,
                        Property.Source.valueOf(getEnumConstant(mirror, "source")),
                        getEnumConstant(mirror, "scope"),
                        (TypeElement) types.asElement(type)
                ));
    }

    private boolean isDifferentTypeErased(TypeMirror one, TypeMirror other) {
        return !types.isSameType(types.erasure(one), types.erasure(other));
    }

    private Optional<SpecEnumeration> specCollection(String name, TypeMirror type, Element element) {
        return getAnnotation(element, SpecEnumeration.ANNOTATION)
                .flatMap(mirror -> {
                    if (isDifferentTypeErased(type, asType(Collection.class))) {
                        messager.printError("@Collection property method must have return type Collection<TYPE>!");
                        return Optional.empty();
                    }

                    List<TypeElement> typeArgument = type.accept(new TypeArgumentExtractor(), null);
                    if (typeArgument.isEmpty()) {
                        messager.printError("Collection must have type parameter!");
                        return Optional.empty();
                    }


                    SpecEnumeration spec = new SpecEnumeration(
                            name,
                            Property.Source.valueOf(getEnumConstant(mirror, "source")),
                            getEnumConstant(mirror, "scope"),
                            typeArgument.getFirst(),
                            Property.FallbackBehaviour.valueOf(getEnumConstant(mirror, "fallback"))
                    );
                    return Optional.of(spec);
                });
    }

    private Optional<SpecMapping> specMapping(String name, TypeMirror type, Element element) {
        return getAnnotation(element, SpecMapping.ANNOTATION)
                .flatMap(mirror -> {
                    if (isDifferentTypeErased(type, asType(Map.class))) {
                        messager.printError("@Mapping property method must have return type Map<KEY_TYPE, VALUE_TYPE>!");
                        return Optional.empty();
                    }

                    List<TypeElement> typeArgument = type.accept(new TypeArgumentExtractor(), null);
                    if (typeArgument.size() < 2) {
                        messager.printError("Map must have type parameters!");
                        return Optional.empty();
                    }


                    SpecMapping spec = new SpecMapping(
                            name,
                            Property.Source.valueOf(getEnumConstant(mirror, "source")),
                            getEnumConstant(mirror, "scope"),
                            typeArgument.get(0),
                            typeArgument.get(1),
                            Property.FallbackBehaviour.valueOf(getEnumConstant(mirror, "fallback"))
                    );
                    return Optional.of(spec);
                });
    }



}
