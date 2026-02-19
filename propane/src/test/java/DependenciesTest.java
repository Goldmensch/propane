import dev.goldmensch.propane.Introspection;
import dev.goldmensch.propane.Property;
import dev.goldmensch.propane.PropertyProvider;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class DependenciesTest {

    private enum Scopes implements Property.Scope {
        ROOT;

        @Override
        public int priority() {
            return ordinal();
        }
    }

    private static class Properties {
        private static class TestStub {}

        static Property<String> HELLO_WORLD = new Property.SingleProperty<>("HELLO_WORLD", Property.Source.PROVIDED, Scopes.ROOT, String.class);
        static Property<Properties.TestStub> TEST_STUB = new Property.SingleProperty<>("TEST_STUB", Property.Source.PROVIDED, Scopes.ROOT, Properties.TestStub.class);
        static Property<String> GOODBYE = new Property.SingleProperty<>("GOODBYE", Property.Source.PROVIDED, Scopes.ROOT, String.class);
    }

    @Test
    public void one_layer_dependency() {
        Properties.TestStub testStub = new Properties.TestStub();

        Introspection introspection = Introspection.create()
                .add(new PropertyProvider<>(Properties.TEST_STUB, PropertyProvider.Priority.FALLBACK, SinglePropertyTest.class, _ -> testStub))
                .add(new PropertyProvider<>(Properties.HELLO_WORLD, PropertyProvider.Priority.FALLBACK, SinglePropertyTest.class, ctx -> {
                    assertSame(testStub, ctx.get(Properties.TEST_STUB));
                    return "Hello World";
                }))
                .build();

        assertEquals("Hello World", introspection.get(Properties.HELLO_WORLD));
    }

    @Test
    public void two_layer_dependency() {
        Properties.TestStub testStub = new Properties.TestStub();

        Introspection introspection = Introspection.create()
                .add(new PropertyProvider<>(Properties.TEST_STUB, PropertyProvider.Priority.FALLBACK, SinglePropertyTest.class, _ -> testStub))
                .add(new PropertyProvider<>(Properties.HELLO_WORLD, PropertyProvider.Priority.FALLBACK, SinglePropertyTest.class, ctx -> {
                    assertSame(testStub, ctx.get(Properties.TEST_STUB));
                    return "Hello World";
                }))
                .add(new PropertyProvider<>(Properties.GOODBYE, PropertyProvider.Priority.FALLBACK, SinglePropertyTest.class, ctx -> {
                    String hello = ctx.get(Properties.HELLO_WORLD);
                    return hello + " was nice, but now: Goodbye!";
                }))
                .build();

        assertEquals("Hello World was nice, but now: Goodbye!", introspection.get(Properties.GOODBYE));
    }
}
