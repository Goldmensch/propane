package dev.goldmensch.propane.property;

import dev.goldmensch.propane.IntrospectionImpl;
import dev.goldmensch.propane.Scope;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/// A [Property] is an identifier for a certain component of a library.
///
/// A library component is just some sort of arbitrary information. That could be some service,
/// some setting configured by the user via a builder, a custom implementation of an interface or something else.
///
/// It's recommended that the library also uses properties for its internal services. They can be specified in an
/// extra class that is kept internal for differentiation.
///
/// There are basically 3 types of properties:
///
/// - [SingletonProperty] for values consisting of one instance
/// - [EnumerationProperty] for values consisting of multiple instances (reference to [Collection])
/// - [MappingProperty] for values representing a mapping between keys and values (reference to [Map])
///
/// Beside their purpose, each type hold a slightly different set of information.
@SuppressWarnings("unused")
public sealed interface Property<T> {

    /// The name of this property.
    ///
    /// Its only purpose is to allow the differentiation of properties in logs or debug sessions.
    ///
    /// @return the properties name
    String name();

    /// The [Scope] that this property is bound to.
    ///
    /// @return the properties scope
    /// @see Scope how scopes restrict access to properties
    Scope scope();


    /// The [Source] of this property.
    ///
    /// @return the properties source
    Source source();

    /// The source of a property defines "where" an [PropertyProvider] for this property
    /// can be "registered". Each source imposes certain restrictions on the priority of its [PropertyProvider]s.
    enum Source {
        /// For properties with source [#PROVIDED] [PropertyProvider]s should only be registered
        /// by the library itself.
        /// They must have their priority set to [PropertyProvider.Priority#FALLBACK]
        ///
        /// @see IntrospectionImpl.Builder#addFallback(SpecificProperty, Function)
        /// @see IntrospectionImpl.Builder#addFallback(SpecificProperty, Class, Function)
        PROVIDED,

        /// For properties with source [#BUILDER] [PropertyProvider]s should be added by the
        /// library and user through a builder. For example, if the library provides a builder, the 'set' methods
        /// inside that should add [PropertyProvider]s with priority = [PropertyProvider.Priority#BUILDER]
        ///
        /// @see IntrospectionImpl.Builder#addBuilder(SpecificProperty, Function)
        /// @see IntrospectionImpl.Builder#addBuilder(SpecificProperty, Class, Function)
        BUILDER,

        // TODO: docs (extension)
        EXTENSION
    }

    /// A [SingleValue] can only hold one object.
    ///
    /// @param <T> the type represented by this interface
    /// @apiNote this is a marker interface
    sealed interface SingleValue<T> extends Property<T> permits SingletonProperty {}

    /// A [MultiValue] can hold multiple objects.
    /// It does not guarantee any sort of specific data structure, thus the final implementation is
    /// determined by the individual implementations of this interface.
    ///
    /// @param <T> the type represented by this interface
    /// @apiNote this is a marker interface
    sealed interface MultiValue<T> extends Property<T> permits EnumerationProperty, MappingProperty {
        FallbackBehaviour fallbackBehaviour();
    }

    /// The [FallbackBehaviour] specifies how fallback values of [multi value properties][MultiValue] are
    /// trod during resolution.
    ///
    /// More detailed: During resolution the values of all [PropertyProvider] of
    /// either [EnumerationProperty] or [MappingProperty] are combined (depending on the used property type).
    /// This enum defines how values with priority = [PropertyProvider.Priority#FALLBACK] are trod here.
    ///
    ///
    enum FallbackBehaviour {
        /// Values from [PropertyProvider]s with priority set to [PropertyProvider.Priority#FALLBACK]
        /// will be ignored during resolution, if [PropertyProvider]s with other priorities are present.
        OVERRIDE,

        /// Values from [PropertyProvider]s with priority set to [PropertyProvider.Priority#FALLBACK]
        /// will be combined with the other values.
        ACCUMULATE
    }

    // TODO docs (scoped access)
    T getScoped();

}
