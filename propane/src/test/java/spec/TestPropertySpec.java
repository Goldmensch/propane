package spec;


import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.spec.annotation.*;

import java.util.Collection;
import java.util.Map;

import static dev.goldmensch.propane.property.Property.FallbackBehaviour.ACCUMULATE;
import static dev.goldmensch.propane.property.Property.Source.BUILDER;
import static spec.GenTestScope.ROOT;

@Propane("GenTest")
@Scopes("ROOT")
public interface TestPropertySpec {

    @Singleton(scope = ROOT, source = BUILDER)
    String FOO_SINGLE();

    @Internal
    @Singleton(scope = ROOT, source = BUILDER)
    String FOO_INTERNAL();

    @Mapping(scope = ROOT, source = BUILDER, fallback = ACCUMULATE)
    Map<String, String> FOO_MAPPING();

    @Enumeration(scope = ROOT, source = BUILDER, fallback = ACCUMULATE)
    Collection<String> FOO_ENUMERATION();


}
