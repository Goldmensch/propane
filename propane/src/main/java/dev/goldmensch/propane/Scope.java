package dev.goldmensch.propane;

public interface Scope {
    /// sorted lower to higher - best to be enum, could be backed by [Enum#ordinal()]
    int priority();
}
