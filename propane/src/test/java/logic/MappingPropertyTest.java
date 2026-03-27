package logic;

import dev.goldmensch.propane.Scope;
import dev.goldmensch.propane.property.PropertyProvider;
import dev.goldmensch.propane.property.Property;
import logic.impl.TestIntrospectionImpl;
import logic.impl.TestMappingProperty;
import logic.impl.TestProperty;
import logic.impl.TestPropertyProvider;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class MappingPropertyTest {
    private enum Scopes implements Scope {
        ROOT;

        @Override
        public int priority() {
            return ordinal();
        }
    }

    private static class Properties {
        private record TestStub() {}

        static TestProperty<Map<String, String>> ONE = new TestMappingProperty<>("ONE", Property.Source.EXTENSION, Scopes.ROOT, String.class, String.class, Property.FallbackStrategy.IGNORE);
        static TestProperty<Map<String, TestStub>> TWO = new TestMappingProperty<>("TWO", Property.Source.EXTENSION, Scopes.ROOT, String.class, TestStub.class, Property.FallbackStrategy.COMBINE);
    }

    @Test
    public void without_dependencies() {
        TestIntrospectionImpl introspection = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.ONE, PropertyProvider.Priority.FALLBACK, MappingPropertyTest.class, _ -> Map.of("hello", "world")))
                .build();

        assertEquals(Map.of("hello", "world"), introspection.get(Properties.ONE));
    }

    @Test
    public void should_always_return_same_instance() {
        AtomicReference<Map<String, Properties.TestStub>> ref = new AtomicReference<>();
        TestIntrospectionImpl introspection = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.TWO, PropertyProvider.Priority.FALLBACK, MappingPropertyTest.class, _ -> {
                    ref.set(Map.of("stub", new Properties.TestStub()));
                    return ref.get();
                }))
                .build();

        Properties.TestStub expected = introspection.get(Properties.TWO).get("stub"); // generate value for "expected"

        // get 2 times, should always return the same instance - not map directly, but contents of map
        assertSame(expected, introspection.get(Properties.TWO).get("stub"));
        assertSame(expected, introspection.get(Properties.TWO).get("stub"));
    }

    @Test
    public void should_not_accumulate_fallback() {
        TestIntrospectionImpl introspection = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.ONE, PropertyProvider.Priority.FALLBACK, MappingPropertyTest.class, _ -> Map.of("hello", "fallback")))
                .add(new TestPropertyProvider<>(Properties.ONE, PropertyProvider.Priority.of(10), MappingPropertyTest.class, _ -> Map.of("hello", "world")))
                .build();

        assertEquals(Map.of("hello", "world"), introspection.get(Properties.ONE));
    }

    @Test
    public void should_accumulate_fallback() {
        TestIntrospectionImpl introspection = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.TWO, PropertyProvider.Priority.FALLBACK, MappingPropertyTest.class, _ -> Map.of("1", new Properties.TestStub())))
                .add(new TestPropertyProvider<>(Properties.TWO, PropertyProvider.Priority.of(10), MappingPropertyTest.class, _ -> Map.of("2", new Properties.TestStub())))
                .build();

        assertEquals(Map.of("1", new Properties.TestStub(), "2", new Properties.TestStub()), introspection.get(Properties.TWO));
    }
}
