package dev.goldmensch.propane.event;

import dev.goldmensch.propane.property.Property;

@SuppressWarnings("unused")
public interface Event<S extends Property.Scope> {
    S scope();

}
