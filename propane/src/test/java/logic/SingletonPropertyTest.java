package logic;

import dev.goldmensch.propane.Scope;
import dev.goldmensch.propane.property.PropertyProviderSkeleton;
import dev.goldmensch.propane.property.Property;
import logic.impl.TestIntrospectionImpl;
import logic.impl.TestProperty;
import logic.impl.TestPropertyProvider;
import logic.impl.TestSingletonProperty;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class SingletonPropertyTest {

    private enum Scopes implements Scope {
        ROOT;

        @Override
        public int priority() {
            return ordinal();
        }
    }

    private class Properties {
        private record TestStub() {}

        static TestProperty<String> HELLO_WORLD = new TestSingletonProperty<>("HELLO_WORLD", Property.Source.EXTENSION, Scopes.ROOT, String.class);
        static TestProperty<TestStub> TEST_STUB = new TestSingletonProperty<>("TEST_STUB", Property.Source.EXTENSION, Scopes.ROOT, TestStub.class);
    }

    @Test
    public void without_dependencies() {
        TestIntrospectionImpl introspection = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.HELLO_WORLD, PropertyProviderSkeleton.Priority.FALLBACK, SingletonPropertyTest.class, _ -> "Hello World"))
                .build();

        assertEquals("Hello World", introspection.get(Properties.HELLO_WORLD));
    }

    @Test
    public void should_always_return_same_instance() {
        TestIntrospectionImpl introspection = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.TEST_STUB, PropertyProviderSkeleton.Priority.FALLBACK, SingletonPropertyTest.class, _ -> new Properties.TestStub()))
                .build();

        Properties.TestStub expected = introspection.get(Properties.TEST_STUB); // generate value for "expected"

        // get 2 times, should always return the same instance
        assertSame(expected, introspection.get(Properties.TEST_STUB));
        assertSame(expected, introspection.get(Properties.TEST_STUB));
    }

    @Test
    public void should_not_return_fallback() {
        TestIntrospectionImpl introspection = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.HELLO_WORLD, PropertyProviderSkeleton.Priority.FALLBACK, SingletonPropertyTest.class, _ -> "Hello World (Fallback)"))
                .add(new TestPropertyProvider<>(Properties.HELLO_WORLD, PropertyProviderSkeleton.Priority.of(10), SingletonPropertyTest.class, _ -> "Hello World"))
                .build();

        assertEquals("Hello World", introspection.get(Properties.HELLO_WORLD));
    }

}
