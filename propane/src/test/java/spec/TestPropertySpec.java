package spec;


import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.spec.annotation.Propane;
import dev.goldmensch.propane.spec.annotation.Scopes;

@Propane("Test")
@Scopes("ROOT")
public interface TestPropertySpec {

    @Singleton(scope = TestScope.ROOT, source = Property.Source.BUILDER)
    String FOO_SINGLE();

    @Map(scope = TestScope.ROOT, source = Property.Source.BUILDER, fallbackBehaviour = Property.FallbackBehaviour.ACCUMULATE)
    String FOO_MAP();

    @Collection(scope = TestScope.ROOT, source = Property.Source.BUILDER, fallbackBehaviour = Property.FallbackBehaviour.ACCUMULATE)
    String FOO_COLLECTION();
}
