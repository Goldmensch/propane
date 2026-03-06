package spec;


import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.spec.annotation.*;

import java.util.Collection;
import java.util.Map;

@Propane("Test")
@Scopes("ROOT")
public interface TestPropertySpec {

    @Singleton(scope = TestScope.ROOT, source = Property.Source.BUILDER)
    String FOO_SINGLE();

    @Mapping(scope = TestScope.ROOT, source = Property.Source.BUILDER, fallback = Property.FallbackBehaviour.ACCUMULATE)
    Map<String, String> FOO_MAP();

    @Enumeration(scope = TestScope.ROOT, source = Property.Source.BUILDER, fallback = Property.FallbackBehaviour.ACCUMULATE)
    Collection<String> FOO_COLLECTION();
}
