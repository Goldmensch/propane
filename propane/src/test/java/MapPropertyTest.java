import dev.goldmensch.propane.Introspection;
import dev.goldmensch.propane.Property;
import dev.goldmensch.propane.PropertyProvider;
import dev.goldmensch.propane.PropertyProvider.Priority;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class MapPropertyTest {
    private enum Scopes implements Property.Scope {
        ROOT;

        @Override
        public int priority() {
            return ordinal();
        }
    }

    private static class Properties {
        private record TestStub() {}

        static Property<Map<String, String>> ONE = new Property.MapProperty<>("ONE", Property.Source.EXTENSION, Scopes.ROOT, String.class, String.class, Property.FallbackBehaviour.OVERRIDE);
        static Property<Map<String, TestStub>> TWO = new Property.MapProperty<>("TWO", Property.Source.EXTENSION, Scopes.ROOT, String.class, TestStub.class, Property.FallbackBehaviour.ACCUMULATE);
    }

    @Test
    public void without_dependencies() {
        Introspection introspection = Introspection.create()
                .add(new PropertyProvider<>(Properties.ONE, Priority.FALLBACK, MapPropertyTest.class, _ -> Map.of("hello", "world")))
                .build();

        assertEquals(Map.of("hello", "world"), introspection.get(Properties.ONE));
    }

    @Test
    public void should_always_return_same_instance() {
        AtomicReference<Map<String, Properties.TestStub>> ref = new AtomicReference<>();
        Introspection introspection = Introspection.create()
                .add(new PropertyProvider<>(Properties.TWO, Priority.FALLBACK, MapPropertyTest.class, _ -> {
                    ref.set(Map.of("stub", new Properties.TestStub()));
                    return ref.get();
                }))
                .build();

        introspection.get(Properties.TWO); // generate value for "expected"

        // get 2 times, should always return the same instance - not map directly, but contents of map
        assertSame(ref.get().get("stub"), introspection.get(Properties.TWO).get("stub"));
        assertSame(ref.get().get("stub"), introspection.get(Properties.TWO).get("stub"));
    }

    @Test
    public void should_not_accumulate_fallback() {
        Introspection introspection = Introspection.create()
                .add(new PropertyProvider<>(Properties.ONE, Priority.FALLBACK, MapPropertyTest.class, _ -> Map.of("hello", "fallback")))
                .add(new PropertyProvider<>(Properties.ONE, Priority.of(10), MapPropertyTest.class, _ -> Map.of("hello", "world")))
                .build();

        assertEquals(Map.of("hello", "world"), introspection.get(Properties.ONE));
    }

    @Test
    public void should_accumulate_fallback() {
        Introspection introspection = Introspection.create()
                .add(new PropertyProvider<>(Properties.TWO, Priority.FALLBACK, MapPropertyTest.class, _ -> Map.of("1", new Properties.TestStub())))
                .add(new PropertyProvider<>(Properties.TWO, Priority.of(10), MapPropertyTest.class, _ -> Map.of("2", new Properties.TestStub())))
                .build();

        assertEquals(Map.of("1", new Properties.TestStub(), "2", new Properties.TestStub()), introspection.get(Properties.TWO));
    }
}
