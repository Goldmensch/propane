import dev.goldmensch.propane.Introspection;
import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.PropertyProvider;
import dev.goldmensch.propane.PropertyProvider.Priority;
import dev.goldmensch.propane.property.SingleProperty;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class SinglePropertyTest {

    private enum Scopes implements Property.Scope {
        ROOT;

        @Override
        public int priority() {
            return ordinal();
        }
    }

    private class Properties {
        private record TestStub() {}

        static Property<String> HELLO_WORLD = new SingleProperty<>("HELLO_WORLD", Property.Source.EXTENSION, Scopes.ROOT, String.class);
        static Property<TestStub> TEST_STUB = new SingleProperty<>("TEST_STUB", Property.Source.EXTENSION, Scopes.ROOT, TestStub.class);
    }

    @Test
    public void without_dependencies() {
        Introspection introspection = Introspection.create(Scopes.ROOT)
                .add(new PropertyProvider<>(Properties.HELLO_WORLD, Priority.FALLBACK, SinglePropertyTest.class, _ -> "Hello World"))
                .build();

        assertEquals("Hello World", introspection.get(Properties.HELLO_WORLD));
    }

    @Test
    public void should_always_return_same_instance() {
        Introspection introspection = Introspection.create(Scopes.ROOT)
                .add(new PropertyProvider<>(Properties.TEST_STUB, Priority.FALLBACK, SinglePropertyTest.class, _ -> new Properties.TestStub()))
                .build();

        Properties.TestStub expected = introspection.get(Properties.TEST_STUB); // generate value for "expected"

        // get 2 times, should always return the same instance
        assertSame(expected, introspection.get(Properties.TEST_STUB));
        assertSame(expected, introspection.get(Properties.TEST_STUB));
    }

    @Test
    public void should_not_return_fallback() {
        Introspection introspection = Introspection.create(Scopes.ROOT)
                .add(new PropertyProvider<>(Properties.HELLO_WORLD, Priority.FALLBACK, SinglePropertyTest.class, _ -> "Hello World (Fallback)"))
                .add(new PropertyProvider<>(Properties.HELLO_WORLD, Priority.of(10), SinglePropertyTest.class, _ -> "Hello World"))
                .build();

        assertEquals("Hello World", introspection.get(Properties.HELLO_WORLD));
    }

}
