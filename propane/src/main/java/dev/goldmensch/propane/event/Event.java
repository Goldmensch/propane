package dev.goldmensch.propane.event;

import dev.goldmensch.propane.Scope;

@SuppressWarnings("unused")
public interface Event<S extends Scope> {
    S scope();

}
