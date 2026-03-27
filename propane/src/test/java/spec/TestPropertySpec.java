package spec;


import dev.goldmensch.propane.spec.annotation.*;

import java.util.Collection;
import java.util.Map;

import static dev.goldmensch.propane.property.Property.FallbackStrategy.COMBINE;
import static dev.goldmensch.propane.property.Property.Source.PROVIDED;
import static spec.GenTestScope.ROOT;

@Propane("GenTest")
@Scopes("ROOT")
public interface TestPropertySpec {

    @Singleton(scope = ROOT, source = PROVIDED)
    String FOO_SINGLE();

    @Internal
    @Singleton(scope = ROOT, source = PROVIDED)
    String FOO_INTERNAL();

    @Mapping(scope = ROOT, source = PROVIDED, fallback = COMBINE)
    Map<String, String> FOO_MAPPING();

    @Enumeration(scope = ROOT, source = PROVIDED, fallback = COMBINE)
    Collection<String> FOO_ENUMERATION();


}
