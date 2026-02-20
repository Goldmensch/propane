import dev.goldmensch.propane.Introspection;
import dev.goldmensch.propane.Property;
import dev.goldmensch.propane.PropertyProvider;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

public class PropertyProviderValidationTest {

    private enum Scopes implements Property.Scope {
        ROOT;

        @Override
        public int priority() {
            return ordinal();
        }
    }

    private final Property<String> providedProperty = new Property.SingleProperty<>("FOO", Property.Source.PROVIDED, Scopes.ROOT, String.class);
    @Test
    public void provided_always_fallback_priority() {
        Introspection.create(Scopes.ROOT)
                .add(providedProvider(PropertyProvider.Priority.FALLBACK))
                .build(); // should work

        Assert.assertThrows(RuntimeException.class, () -> Introspection.create(Scopes.ROOT)
                .add(providedProvider(PropertyProvider.Priority.BUILDER))
                .build());

        Assert.assertThrows(RuntimeException.class, () -> Introspection.create(Scopes.ROOT)
                .add(providedProvider(PropertyProvider.Priority.of(007)))
                .build());
    }

    private PropertyProvider<String> providedProvider(PropertyProvider.Priority priority) {
        return new PropertyProvider<>(providedProperty, priority, PropertyProviderValidationTest.class, _ -> "huhu");
    }


    @Test
    public void provided_multi_value_must_be_accumulate() {
        Property<Collection<String>> accumulate = new Property.CollectionProperty<>("BAR", Property.Source.PROVIDED, Scopes.ROOT, String.class, Property.FallbackBehaviour.ACCUMULATE);
        Property<Collection<String>> override = new Property.CollectionProperty<>("BAR", Property.Source.PROVIDED, Scopes.ROOT, String.class, Property.FallbackBehaviour.OVERRIDE);

        // should work
        Introspection.create(Scopes.ROOT)
                .add(multiProvider(accumulate))
                .build();

        Assert.assertThrows(RuntimeException.class, () -> Introspection.create(Scopes.ROOT)
                .add(multiProvider(override))
                .build());
    }

    private PropertyProvider<Collection<String>> multiProvider(Property<Collection<String>> property) {
        return new PropertyProvider<>(property, PropertyProvider.Priority.FALLBACK, PropertyProviderValidationTest.class, _ -> List.of());
    }


}
