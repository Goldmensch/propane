package dev.goldmensch.propane.property;

import java.util.Comparator;
import java.util.stream.Stream;

/// A [Priority] specifies with [PropertyProviderSkeleton]s take precedence over others.
///
/// Each [Priority] is backed by an [Integer]: Higher/greater priorities take precedence
/// over lower ones.
///
/// There are 2 reserved priorities:
/// - 0 for [Priority#FALLBACK], used for fallback values and ["provided properties"][Property.Source#PROVIDED]
/// - [Integer#MAX_VALUE] for [Priority#BUILDER], used for values provided by the user through some builder
///
/// For [PropertyProviderSkeleton]s whose [priority][PropertyProviderSkeleton#priority()] is set to [Priority#FALLBACK],
/// the specific rules of [Property.FallbackStrategy] apply.
public class Priority implements Comparable<Priority> {
    /// The priority used to mark [PropertyProviderSkeleton]s providing "fallback" (also called "default")
    /// values. Its numeric value is `0`.
    public static final Priority FALLBACK = new Priority(0);

    /// The priority is used to mark [PropertyProviderSkeleton]s encapsulating values provided by the
    /// user through some builder. Its numeric value is [Integer#MAX_VALUE].
    public static final Priority BUILDER = new Priority(Integer.MAX_VALUE);

    private final int ordinal;

    Priority(int ordinal) {
        this.ordinal = ordinal;
    }

    /// Creates an [Priority] with the given numeric value.
    ///
    /// @param ordinal the numeric value of this priority. Higher values take precedence over lower ones.
    public static Priority of(int ordinal) {
        if (ordinal < 0) {
            throw new RuntimeException("priority ordinal can't be negative");
        }

        if (ordinal == FALLBACK.ordinal || ordinal == BUILDER.ordinal) {
            throw new RuntimeException("priority ordinal can't be 0 or Integer.MAX_VALUE");
        }

        return new Priority(ordinal);
    }

    /// Returns the ordinal value of this [Priority], that is used to determine the
    /// precedence.
    ///
    /// @return the ordinal value of this priority
    public int ordinal() {
        return ordinal;
    }

    /// Compares two properties by their numeric value in [reverse order][Comparator#reverseOrder()].
    ///
    /// For example:
    /// - `BUILDER.compareTo(FALLBACK)` will return -1
    /// - `FALLBACK.compareTo(BUILDER)` will return 1
    ///
    /// This is done, so that [Stream#sorted()] returns the [PropertyProviderSkeleton]s with the highest priority first.
    ///
    /// @param o the [Priority] that this one should be compared to
    /// @return a negative integer, zero, or a positive integer as this priority
    ///         is greater than, equal to, or less than the other one
    @Override
    public int compareTo(Priority o) {
        return Comparator.<Integer>reverseOrder().compare(ordinal, o.ordinal);
    }
}
