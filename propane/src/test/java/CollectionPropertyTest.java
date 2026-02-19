import dev.goldmensch.propane.Introspection;
import dev.goldmensch.propane.Property;
import dev.goldmensch.propane.PropertyProvider;
import dev.goldmensch.propane.PropertyProvider.Priority;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class CollectionPropertyTest {
    private enum Scopes implements Property.Scope {
        ROOT;

        @Override
        public int priority() {
            return ordinal();
        }
    }

    private static class Properties {
        private record TestStub() {}

        static Property<Collection<String>> ONE = new Property.CollectionProperty<>("ONE", Property.Source.EXTENSION, Scopes.ROOT, String.class, Property.FallbackBehaviour.OVERRIDE);
        static Property<Collection<TestStub>> TWO = new Property.CollectionProperty<>("TWO", Property.Source.EXTENSION, Scopes.ROOT, Properties.TestStub.class, Property.FallbackBehaviour.ACCUMULATE);
    }

    @Test
    public void without_dependencies() {
        Introspection introspection = Introspection.create()
                .add(new PropertyProvider<>(Properties.ONE, Priority.FALLBACK, CollectionPropertyTest.class, _ -> List.of("hello", "world")))
                .build();

        assertEquals(List.of("hello", "world"), introspection.get(Properties.ONE));
    }

    @Test
    public void should_always_return_same_instance() {
        AtomicReference<Collection<Properties.TestStub>> ref = new AtomicReference<>();
        Introspection introspection = Introspection.create()
                .add(new PropertyProvider<>(Properties.TWO, Priority.FALLBACK, MapPropertyTest.class, _ -> {
                    ref.set(List.of(new Properties.TestStub()));
                    return ref.get();
                }))
                .build();

        introspection.get(Properties.TWO); // generate value for "expected"

        // get 2 times, should always return the same instance - not map directly, but contents of map
        assertSame(ref.get().toArray(Properties.TestStub[]::new)[0], introspection.get(Properties.TWO).toArray(Properties.TestStub[]::new)[0]);
        assertSame(ref.get().toArray(Properties.TestStub[]::new)[0], introspection.get(Properties.TWO).toArray(Properties.TestStub[]::new)[0]);
    }

    @Test
    public void should_not_accumulate_fallback() {
        Introspection introspection = Introspection.create()
                .add(new PropertyProvider<>(Properties.ONE, Priority.FALLBACK, CollectionPropertyTest.class, _ -> List.of("hello", "fallback")))
                .add(new PropertyProvider<>(Properties.ONE, Priority.of(10), CollectionPropertyTest.class, _ -> List.of("hello", "world")))
                .build();

        assertEquals(List.of("hello", "world"), introspection.get(Properties.ONE));
    }

    @Test
    public void should_accumulate_fallback() {
        Introspection introspection = Introspection.create()
                .add(new PropertyProvider<>(Properties.TWO, Priority.FALLBACK, CollectionPropertyTest.class, _ -> List.of(new Properties.TestStub())))
                .add(new PropertyProvider<>(Properties.TWO, Priority.of(10), CollectionPropertyTest.class, _ -> List.of(new Properties.TestStub())))
                .build();

        assertEquals(List.of(new Properties.TestStub(), new Properties.TestStub()), introspection.get(Properties.TWO));
    }
}
