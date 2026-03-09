package dev.goldmensch.propane.spec.generator;

import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeSpec;
import dev.goldmensch.propane.spec.processor.syntax.SpecMeta;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.Filer;
import javax.lang.model.element.PackageElement;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

abstract class AbstractGenerator<T> {

    protected final String packageName;
    private final Filer filer;

    protected AbstractGenerator(PackageElement element, Filer filer) {
        this.packageName = element.getQualifiedName().toString();
        this.filer = filer;
    }

    public void generate(T meta) {
        for (Map.Entry<String, List<Supplier<@Nullable TypeSpec>>> generator : generators(meta).entrySet()) {
            for (Supplier<@Nullable TypeSpec> supplier : generator.getValue()) {
                TypeSpec spec = supplier.get();
                if (spec == null) continue;

                String pName = generator.getKey().isEmpty()
                        ? packageName
                        : packageName + "." + generator.getKey();
                JavaFile file = JavaFile.builder(pName, spec).build();
                try {
                    file.writeTo(filer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    abstract Map<String, List<Supplier<@Nullable TypeSpec>>> generators(T meta);
}
