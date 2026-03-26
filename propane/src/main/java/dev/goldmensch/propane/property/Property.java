package dev.goldmensch.propane.property;

import dev.goldmensch.propane.IntrospectionImpl;
import dev.goldmensch.propane.Scope;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/// A [Property] is an identifier for a certain component of a library.
///
/// A library component is actually just some sort of information. That could be some service,
/// some setting configured by the user via a builder, a custom implementation of an interface or something else.
///
/// It's recommended that the library also uses properties for its internal services. They can be specified in a
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


    /// The source of this property.
    ///
    /// @return the properties source
    Source source();

    /// The source of a property defines, "where" an [PropertyProvider] for this property
    /// can be "registered". Each source is paired with a specific [PropertyProvider.Priority].
    enum Source {
        /// For properties with source [#PROVIDED] [PropertyProvider]s should only be registered
        /// by the library itself.
        /// They must have their priority set to [0][PropertyProvider.Priority#FALLBACK]
        ///
        /// @see IntrospectionImpl.Builder#addFallback(SpecificProperty, Function)
        /// @see IntrospectionImpl.Builder#addFallback(SpecificProperty, Class, Function)
        PROVIDED,

        /// For properties with source [#BUILDER] [PropertyProvider]s should be added by the
        /// library and user through a builder. For example, if the library provides a builder, the 'set' methods
        /// inside that should add [PropertyProvider]s with priority = [Integer#MAX_VALUE][PropertyProvider.Priority#BUILDER]
        ///
        /// @see IntrospectionImpl.Builder#addBuilder(SpecificProperty, Function)
        /// @see IntrospectionImpl.Builder#addBuilder(SpecificProperty, Class, Function)
        BUILDER,

        EXTENSION
    }

    sealed interface SingleValue<T> extends Property<T> permits SingletonProperty {}

    sealed interface MultiValue<T> extends Property<T> permits EnumerationProperty, MappingProperty {
        FallbackBehaviour fallbackBehaviour();
    }

    enum FallbackBehaviour {
        OVERRIDE,
        ACCUMULATE
    }

    T getScoped();

}
