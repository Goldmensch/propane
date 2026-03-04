import org.jspecify.annotations.NullMarked;

@NullMarked
module dev.goldmensch.propane {
    requires static com.palantir.javapoet;

    requires org.jspecify;
    requires java.compiler;

    exports dev.goldmensch.propane;
    exports dev.goldmensch.propane.property;
    exports dev.goldmensch.propane.internal.exposed;

    provides javax.annotation.processing.Processor with dev.goldmensch.propane.spec.processor.SpecProcessor;
}