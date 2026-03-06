package spec.bar;


import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.spec.annotation.*;

import java.util.Collection;
import java.util.Map;

@Propane
@Scopes("ROOT")
public interface BarPropertySpec {

    @Singleton(scope = BarScope.ROOT, source = Property.Source.BUILDER)
    String FOO_SINGLE();

    @Mapping(scope = BarScope.ROOT, source = Property.Source.BUILDER, fallback = Property.FallbackBehaviour.ACCUMULATE)
    Map<String, String> FOO_MAP();

    @Enumeration(scope = BarScope.ROOT, source = Property.Source.BUILDER, fallback = Property.FallbackBehaviour.ACCUMULATE)
    Collection<String> FOO_COLLECTION();
}

