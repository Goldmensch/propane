package dev.goldmensch.propane.property;

// must be implemented from "user" specific property interface
// the "user specific" interface must be implemented by the "user specific" Single/Map/Collection Property and the IntrospectionImpl
public interface SpecificProperty<T> {

    @SuppressWarnings("unchecked")
    default Property<T> generalized() {
        return (Property<T>) this;
    }

    default T scopedGet() {
        return generalized().getScoped();
    }
}
