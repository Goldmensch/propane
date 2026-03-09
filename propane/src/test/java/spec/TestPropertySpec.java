package spec;


import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.property.SpecificProperty;
import dev.goldmensch.propane.spec.annotation.*;

import java.util.Collection;
import java.util.Map;

@Propane("GenTest")
@Scopes("ROOT")
public interface TestPropertySpec {

    /// My comment [GenTestScope#ROOT], [Singleton], [spec.GenTestIntrospection], [Map.Entry]
    /// Multiline baby!!
    ///
    /// @see SpecificProperty<String>
    @Singleton(scope = GenTestScope.ROOT, source = Property.Source.BUILDER)
    String FOO_SINGLE();

    @Internal
    @Singleton(scope = GenTestScope.ROOT, source = Property.Source.BUILDER)
    String FOO_INTERNAL();

    @Mapping(scope = GenTestScope.ROOT, source = Property.Source.BUILDER, fallback = Property.FallbackBehaviour.ACCUMULATE)
    Map<String, String> FOO_MAPPING();

    @Enumeration(scope = GenTestScope.ROOT, source = Property.Source.BUILDER, fallback = Property.FallbackBehaviour.ACCUMULATE)
    Collection<String> FOO_ENUMERATION();
}
