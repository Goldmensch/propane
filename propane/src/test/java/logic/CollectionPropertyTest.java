package logic;

import dev.goldmensch.propane.Scope;
import dev.goldmensch.propane.property.PropertyProvider;
import dev.goldmensch.propane.property.Property;
import logic.impl.TestCollectionProperty;
import logic.impl.TestIntrospectionImpl;
import logic.impl.TestProperty;
import logic.impl.TestPropertyProvider;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class CollectionPropertyTest {
    private enum Scopes implements Scope {
        ROOT;

        @Override
        public int priority() {
            return ordinal();
        }
    }

    private static class Properties {
        private record TestStub() {}

        static TestProperty<Collection<String>> ONE = new TestCollectionProperty<>("ONE", Property.Source.EXTENSION, Scopes.ROOT, String.class, Property.FallbackBehaviour.OVERRIDE);
        static TestProperty<Collection<TestStub>> TWO = new TestCollectionProperty<>("TWO", Property.Source.EXTENSION, Scopes.ROOT, Properties.TestStub.class, Property.FallbackBehaviour.ACCUMULATE);
    }

    @Test
    public void without_dependencies() {
        TestIntrospectionImpl introspection = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.ONE, PropertyProvider.Priority.FALLBACK, CollectionPropertyTest.class, _ -> List.of("hello", "world")))
                .build();

        assertEquals(List.of("hello", "world"), introspection.get(Properties.ONE));
    }

    @Test
    public void should_always_return_same_instance() {
        AtomicReference<Collection<Properties.TestStub>> ref = new AtomicReference<>();
        TestIntrospectionImpl introspection = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.TWO, PropertyProvider.Priority.FALLBACK, CollectionPropertyTest.class, _ -> {
                    ref.set(List.of(new Properties.TestStub()));
                    return ref.get();
                }))
                .build();

        Properties.TestStub expected = introspection.get(Properties.TWO).toArray(Properties.TestStub[]::new)[0]; // generate value for "expected"

        // get 2 times, should always return the same instance - not map directly, but contents of map
        assertSame(expected, introspection.get(Properties.TWO).toArray(Properties.TestStub[]::new)[0]);
        assertSame(expected, introspection.get(Properties.TWO).toArray(Properties.TestStub[]::new)[0]);
    }

    @Test
    public void should_not_accumulate_fallback() {
        TestIntrospectionImpl introspection = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.ONE, PropertyProvider.Priority.FALLBACK, CollectionPropertyTest.class, _ -> List.of("hello", "fallback")))
                .add(new TestPropertyProvider<>(Properties.ONE, PropertyProvider.Priority.of(10), CollectionPropertyTest.class, _ -> List.of("hello", "world")))
                .build();

        assertEquals(List.of("hello", "world"), introspection.get(Properties.ONE));
    }

    @Test
    public void should_accumulate_fallback() {
        TestIntrospectionImpl introspection = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.TWO, PropertyProvider.Priority.FALLBACK, CollectionPropertyTest.class, _ -> List.of(new Properties.TestStub())))
                .add(new TestPropertyProvider<>(Properties.TWO, PropertyProvider.Priority.of(10), CollectionPropertyTest.class, _ -> List.of(new Properties.TestStub())))
                .build();

        assertEquals(List.of(new Properties.TestStub(), new Properties.TestStub()), introspection.get(Properties.TWO));
    }
}
