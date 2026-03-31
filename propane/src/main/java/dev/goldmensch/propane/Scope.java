package dev.goldmensch.propane;


/// # Concept of Scopes
/// Commonly a library is structured in certain scopes.
/// Each [Scope] represents a stage in the libraries lifetime (runtime).
/// Scopes life in a hierarchy, where a scope inherits all properties of its parents.
///
/// For example, a library can consist of 3 stages/scopes:
///
/// 1. CONFIGURATION → the library is fully configured
/// 2. INITIALIZED   → the library is fully initialized/the framework is started
/// 3. REQUEST       → the library got a user request and is computing it
///
/// If you are in scope INITIALIZED, you can access all properties of CONFIGURATION too.
///
/// If you are in scope REQUEST, you can access all properties of CONFIGURATION and INITIALIZED too.
///
/// ## Ordering
/// Scopes are ordered by their [priority][Scope#priority()]. A higher priority means, that the scope
/// is a child, thus inherits all properties, of scopes with a lower priority.
///
/// ## Scopes as Enums
/// In most cases, the scopes of a library are predefined and can be conveniently be modeled as a
/// [Enum] where the enums' [ordinal][Enum#ordinal()] is the scopes' [priority][#priority()].
/// That means, that the order of this enum will be very important and must be trodden carefully.
///
/// ```java
/// enum FooScope implements Scope {
///     CONFIGURATION,
///     INITIALIZED,
///     REQUEST;
///
///     @Override
///     public int priority() {
///         this.ordinal();
///     }
/// }
/// ```
public interface Scope {

    /// The priority of scope determines its order relative to other scopes.
    /// Scopes with a higher priority are children, thus inherits all properties
    /// of scopes with a lower priority.
    ///
    /// @return the scopes' priority
    int priority();
}
