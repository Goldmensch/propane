package dev.goldmensch.propane.property;

/// Propane generates a custom API for each library using it. Part of this custom API layer
/// is an own "property type" called the libraries specific property type.
///
/// This interface is the foundation of this specific property type, which is nothing more than a
/// wrapper around this, like:
///
/// ```java
/// public interface JDACProperty<T> extends SpecificProperty<T> {
///     ...
/// }
/// ```
///
/// If you're working with propane or a library using propane, you will mostly see and use the libraries specific property type (like `JDACProperty`).
///
/// ## Getting the "real" property
/// This interface doesn't extend [Property], thus enabling [Property] to be sealed and allows the java compiler to check
/// pattern exhaustiveness, which makes working with it easy.
///
/// A libraries specific property type must only be implemented by its specific [MappingProperty], [SingletonProperty] and
/// [EnumerationProperty], which themselves implement [Property]. Therefore, a libraries specific property type can always be safely
/// cast to [Property].
// must be implemented from "user" specific property interface
// the "user specific" interface must be implemented by the "user specific" Single/Map/Collection Property and the IntrospectionImpl
public interface SpecificProperty<T> {

    /// Casts this specific property type to [Property].
    /// This cast method is safe as guaranteed by the spec.
    ///
    /// @return this as a [Property]
    @SuppressWarnings("unchecked")
    default Property<T> generalized() {
        return (Property<T>) this;
    }

    default T scopedGet() {
        return generalized().getScoped();
    }
}
