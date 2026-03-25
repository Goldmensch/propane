# Propane

Propane is a library designed to ease the development of libraries by featuring scope based access to the components.

You can think of propane like a manual dependency injection framework without all the 'magic'. At it's core
it's actually nothing more than a mapping of properties and values, accessible by a scope based api.

Propanes primary goal is to be used inside libraries. It should not replace a dependency injection framework
nor should it be a general application configuration framework.

## Properties
A [dev.goldmensch.propane.property.Property] is an identifier for a certain component of a library.
It holds information about the type of property, the java class represented by this component, the scope of a property 
and more.

The value of a property is provided by [property providers](#property-provider).

## Property Provider
The value of a [property](#properties) is provided by one or more [dev.goldmensch.propane.property.PropertyProvider].
They are called lazily when a properties value is requested. The value is then cached and reused.

## Introspection
The [dev.goldmensch.propane.Introspection] and [dev.goldmensch.propane.IntrospectionImpl] classes are the main entry points
of this framework at runtime. They allow access to property values and the event bus. 

Additionally, these classes shape the scoped based character of propane. Each [dev.goldmensch.propane.Introspection]
is bound to a specific [scope](#scopes), thus restricting access to certain properties.

## Scopes
Commonly a library is structured in certain scopes. Each [dev.goldmensch.propane.Scope] represents a stage in the libraries lifetime (runtime).
Scopes life in a hierarchy, where a scope inherits all properties of its parents.

For example, a library can consist of 3 stages:

1. CONFIGURATION → the library is fully configured   
2. INITIALIZED   → the library is fully initialized/the framework is started
3. REQUEST       → the library got a user request and is computing it

If you are in scope INITIALIZED, you can access all properties of CONFIGURATION too.

If you are in scope REQUEST, you can access all properties of CONFIGURATION and INITIALIZED too.



