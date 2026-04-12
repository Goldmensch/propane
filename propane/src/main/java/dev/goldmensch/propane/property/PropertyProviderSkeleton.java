package dev.goldmensch.propane.property;

import dev.goldmensch.propane.IntrospectionSkeleton;
import dev.goldmensch.propane.Skeleton;
import dev.goldmensch.propane.property.Property.Source;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/// A [PropertyProviderSkeleton] supplies the values for a property.
/// Each [PropertyProviderSkeleton] is bound to _one_ property for which it can provide values.
///
/// Based on the [Source] of the property, property providers can be registered via different ways. Visit the documentation
/// of [Source] for more information on that matter.
///
/// The possibly [returned][PropertyProviderSkeleton#supplier()] types vary based on the type of the property:
/// - for [SingletonPropertySkeleton], a provider can "return" one instance
/// - for [EnumerationPropertySkeleton], a provider can "return" an instance of [Collection]
/// - for [MappingPropertySkeleton], a provider can "return" an instance of [Map]
///
/// The values are "provided" by the [supplier][PropertyProviderSkeleton#supplier()], which is called during resolution.
/// The supplier must be threadsafe and side effect free, as it could be called multiple times during resolution simultaneously.
/// Although that, only one value is stored finally (using a "check-then-act" pattern).
///
/// Furthermore, each [PropertyProviderSkeleton] has a [priority][PropertyProviderSkeleton.Priority] stating which providers take
/// precedence over others. For more information on that, visit the documentation of [SingletonPropertySkeleton], [EnumerationPropertySkeleton]
/// and [MappingPropertySkeleton].
///
/// For debugging purpose, [PropertyProviderSkeleton]s store their "owner", which is the class or "logical unit"
/// (for example the library providing an extension for some property), that can be used to identify an [PropertyProviderSkeleton]
/// during runtime.
///
/// ## Dependencies on other properties
/// The [supplier][PropertyProviderSkeleton#supplier()] allows access to the [IntrospectionSkeleton] instance used to resolve this
/// property. This can be used to retrieve the values of other properties needed by this [PropertyProviderSkeleton], thus creating
/// a dependency on that other property.
///
/// Propane checks for cycling dependencies during runtime and will provide all needed information to identify the cycle.
///
/// @param <T> the type returned by the supplier
/// @param <PROPERTY> the subtype of [SpecificProperty] this provider is bound to
/// @param <INTROSPECTION> the subtype of [IntrospectionSkeleton] this provider is bound to
/// @see SpecificProperty why you have to use the "specific" version of this class
@Skeleton
public abstract class PropertyProviderSkeleton<T, PROPERTY extends SpecificProperty<T>, INTROSPECTION extends IntrospectionSkeleton<INTROSPECTION, ?>> {
    private final PROPERTY property;
    private final Priority priority;
    private final Class<?> owner;
    private final Function<INTROSPECTION, @Nullable T> supplier;

    /// @param property the [Property] this [PropertyProviderSkeleton] is providing values for
    /// @param priority te [Priority] of this provider
    /// @param owner the owner [Class] of this provider
    /// @param supplier the [Supplier] returning the value of this provider.
    /// It takes the [IntrospectionSkeleton] instance used to resolve this property as argument, allowing dependencies on
    /// other properties.
    public PropertyProviderSkeleton(
            PROPERTY property,
            Priority priority,
            Class<?> owner,
            Function<INTROSPECTION, @Nullable T> supplier
    ) {
        this.property = property;
        this.priority = priority;
        this.owner = owner;
        this.supplier = supplier;
    }

    /// @return the [Property] this [PropertyProviderSkeleton] if providing values for
    public PROPERTY property() {
        return property;
    }

    /// @return the [Priority] of this property provider
    public Priority priority() {
        return priority;
    }

    /// @return the owner [Class] of this property provider
    public Class<?> owner() {
        return owner;
    }

    /// This supplier returns the value(s) of this [PropertyProviderSkeleton].
    /// It takes the [IntrospectionSkeleton] instance used to resolve this property as an argument, allowing
    /// for dependencies on other properties.
    ///
    /// The supplier may be called multiple times during property resolution, for more information
    /// visit the class Javadoc.
    ///
    /// @return the [Supplier] returning the value of this provider
    public Function<INTROSPECTION, @Nullable T> supplier() {
        return supplier;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PropertyProviderSkeleton<?, ?, ?>) obj;
        return Objects.equals(this.property, that.property) &&
                Objects.equals(this.priority, that.priority) &&
                Objects.equals(this.owner, that.owner) &&
                Objects.equals(this.supplier, that.supplier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(property, priority, owner, supplier);
    }

    @Override
    public String toString() {
        return "PropertyProviderSkeleton[" +
                "property=" + property + ", " +
                "priority=" + priority + ", " +
                "owner=" + owner + ", " +
                "supplier=" + supplier + ']';
    }


    /// A [Priority] specifies with [PropertyProviderSkeleton]s take precedence over others.
    ///
    /// Each [Priority] is backed by an [Integer]: Higher/greater priorities take precedence
    /// over lower ones.
    ///
    /// There are 2 reserved priorities:
    /// - 0 for [Priority#FALLBACK], used for fallback values and ["provided properties"][Source#PROVIDED]
    /// - [Integer#MAX_VALUE] for [Priority#BUILDER], used for values provided by the user through some builder
    ///
    /// For [PropertyProviderSkeleton]s whose [priority][PropertyProviderSkeleton#priority()] is set to [Priority#FALLBACK],
    /// the specific rules of [Property.FallbackStrategy] apply.
    public static class Priority implements Comparable<Priority> {
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
}
