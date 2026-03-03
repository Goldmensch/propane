import org.jspecify.annotations.NullMarked;

@NullMarked
module dev.goldmensch.propane {
    requires org.jspecify;

    exports dev.goldmensch.propane;
    exports dev.goldmensch.propane.property;
    exports dev.goldmensch.propane.internal.exposed;
}