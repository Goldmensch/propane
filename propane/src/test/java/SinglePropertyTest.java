import dev.goldmensch.propane.Introspection;
import dev.goldmensch.propane.Property;
import dev.goldmensch.propane.PropertyProvider;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

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

        static Property<String> HELLO_WORLD = new Property.SingleProperty<>("HELLO_WORLD", Property.Source.PROVIDED, Scopes.ROOT, String.class);
        static Property<TestStub> TEST_STUB = new Property.SingleProperty<>("TEST_STUB", Property.Source.PROVIDED, Scopes.ROOT, TestStub.class);
    }

    @Test
    public void without_dependencies() {
        Introspection introspection = Introspection.create()
                .add(new PropertyProvider<>(Properties.HELLO_WORLD, PropertyProvider.FALLBACK_PRIORITY, SinglePropertyTest.class, _ -> "Hello World"))
                .build();

        assertEquals("Hello World", introspection.get(Properties.HELLO_WORLD));
    }

    @Test
    public void should_always_return_same_instance() {
        AtomicReference<Properties.TestStub> ref = new AtomicReference<>();
        Introspection introspection = Introspection.create()
                .add(new PropertyProvider<>(Properties.TEST_STUB, PropertyProvider.FALLBACK_PRIORITY, SinglePropertyTest.class, _ -> {
                    ref.set(new Properties.TestStub());
                    return ref.get();
                }))
                .build();

        introspection.get(Properties.TEST_STUB); // generate value for "expected"

        // get 2 times, should always return the same instance
        assertSame(ref.get(), introspection.get(Properties.TEST_STUB));
        assertSame(ref.get(), introspection.get(Properties.TEST_STUB));
    }

    @Test
    public void should_not_return_fallback() {
        Introspection introspection = Introspection.create()
                .add(new PropertyProvider<>(Properties.HELLO_WORLD, PropertyProvider.FALLBACK_PRIORITY, SinglePropertyTest.class, _ -> "Hello World (Fallback)"))
                .add(new PropertyProvider<>(Properties.HELLO_WORLD, 10, SinglePropertyTest.class, _ -> "Hello World"))
                .build();

        assertEquals("Hello World", introspection.get(Properties.HELLO_WORLD));
    }

}
