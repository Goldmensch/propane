package dev.goldmensch.propane.spec.processor;

import dev.goldmensch.propane.spec.annotation.Propane;
import dev.goldmensch.propane.spec.annotation.Scopes;
import dev.goldmensch.propane.spec.processor.syntax.SpecMeta;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_25)
public class SpecProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element klasses : roundEnv.getElementsAnnotatedWith(Propane.class)) {
            generateDSL(klasses);
        }

        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(Propane.class.getName());
    }

    private void generateDSL(Element klass) {
        PackageElement klassPackage = processingEnv.getElementUtils().getPackageOf(klass);

        Scopes scopes = klass.getAnnotation(Scopes.class);
        Propane propane = klass.getAnnotation(Propane.class);
        if (scopes == null || propane == null) {
            processingEnv.getMessager().printError("@Scope and @Propane must be present on property spec class!", klass);
            return;
        }

        SpecMeta meta = new SpecMeta(propane.value(), scopes.value());

        new DSLGenerator(klassPackage, processingEnv.getFiler()).generate(meta);
    }
}
