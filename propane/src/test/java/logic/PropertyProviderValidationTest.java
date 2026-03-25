package logic;

import dev.goldmensch.propane.Scope;
import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.property.PropertyProvider;
import logic.impl.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

public class PropertyProviderValidationTest {

    private enum Scopes implements Scope {
        ROOT;

        @Override
        public int priority() {
            return ordinal();
        }
    }

    private final TestProperty<String> providedProperty = new TestSingleProperty<>("FOO", Property.Source.PROVIDED, Scopes.ROOT, String.class);
    @Test
    public void provided_always_fallback_priority() {
        TestIntrospectionImpl.create(Scopes.ROOT)
                .add(providedProvider(PropertyProvider.Priority.FALLBACK))
                .build(); // should work

        Assert.assertThrows(RuntimeException.class, () -> TestIntrospectionImpl.create(Scopes.ROOT)
                .add(providedProvider(PropertyProvider.Priority.BUILDER))
                .build());

        Assert.assertThrows(RuntimeException.class, () -> TestIntrospectionImpl.create(Scopes.ROOT)
                .add(providedProvider(PropertyProvider.Priority.of(007)))
                .build());
    }

    private TestPropertyProvider<String> providedProvider(PropertyProvider.Priority priority) {
        return new TestPropertyProvider<>(providedProperty, priority, PropertyProviderValidationTest.class, _ -> "huhu");
    }


    @Test
    public void provided_multi_value_must_be_accumulate() {
        TestProperty<Collection<String>> accumulate = new TestCollectionProperty<>("BAR", Property.Source.PROVIDED, Scopes.ROOT, String.class, Property.FallbackBehaviour.ACCUMULATE);
        TestProperty<Collection<String>> override = new TestCollectionProperty<>("BAR", Property.Source.PROVIDED, Scopes.ROOT, String.class, Property.FallbackBehaviour.OVERRIDE);

        // should work
        TestIntrospectionImpl.create(Scopes.ROOT)
                .add(multiProvider(accumulate))
                .build();

        Assert.assertThrows(RuntimeException.class, () -> TestIntrospectionImpl.create(Scopes.ROOT)
                .add(multiProvider(override))
                .build());
    }

    private TestPropertyProvider<Collection<String>> multiProvider(TestProperty<Collection<String>> property) {
        return new TestPropertyProvider<>(property, PropertyProvider.Priority.FALLBACK, PropertyProviderValidationTest.class, _ -> List.of());
    }


}
