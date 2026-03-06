package dev.goldmensch.propane.spec.processor;

import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeSpec;
import dev.goldmensch.propane.spec.processor.syntax.SpecMeta;

import javax.annotation.processing.Filer;
import javax.lang.model.element.PackageElement;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

abstract class AbstractGenerator {

    protected final String packageName;
    private final Filer filer;

    protected AbstractGenerator(String packageName, Filer filer) {
        this.packageName = packageName;
        this.filer = filer;
    }

    protected AbstractGenerator(PackageElement element, Filer filer) {
        this.packageName = element.getQualifiedName().toString();
        this.filer = filer;
    }

    void generate(SpecMeta meta) {
        for (Function<SpecMeta, TypeSpec> generator : generators()) {
            TypeSpec spec = generator.apply(meta);
            JavaFile file = JavaFile.builder(packageName, spec).build();
            try {
                file.writeTo(filer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    abstract List<Function<SpecMeta, TypeSpec>> generators();
}
