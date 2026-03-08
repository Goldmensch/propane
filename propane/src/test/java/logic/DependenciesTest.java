package logic;

import dev.goldmensch.propane.property.Property;
import logic.impl.TestIntrospectionImpl;
import logic.impl.TestProperty;
import logic.impl.TestPropertyProvider;
import logic.impl.TestSingleProperty;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import static org.junit.Assert.*;

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

        static TestProperty<String> HELLO_WORLD = new TestSingleProperty<>("HELLO_WORLD", Property.Source.PROVIDED, Scopes.ROOT, String.class);
        static TestProperty<Properties.TestStub> TEST_STUB = new TestSingleProperty<>("TEST_STUB", Property.Source.PROVIDED, Scopes.ROOT, Properties.TestStub.class);
        static TestProperty<String> GOODBYE = new TestSingleProperty<>("GOODBYE", Property.Source.PROVIDED, Scopes.ROOT, String.class);
    }

    @Test
    public void one_layer_dependency() {
        Properties.TestStub testStub = new Properties.TestStub();

        TestIntrospectionImpl introspection = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.TEST_STUB, TestPropertyProvider.Priority.FALLBACK, DependenciesTest.class, _ -> testStub))
                .add(new TestPropertyProvider<>(Properties.HELLO_WORLD, TestPropertyProvider.Priority.FALLBACK, DependenciesTest.class, ctx -> {
                    assertSame(testStub, ctx.get(Properties.TEST_STUB));
                    return "Hello World";
                }))
                .build();

        assertEquals("Hello World", introspection.get(Properties.HELLO_WORLD));
    }

    @Test
    public void two_layer_dependency() {
        Properties.TestStub testStub = new Properties.TestStub();

        TestIntrospectionImpl introspection = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.TEST_STUB, TestPropertyProvider.Priority.FALLBACK, DependenciesTest.class, _ -> testStub))
                .add(new TestPropertyProvider<>(Properties.HELLO_WORLD, TestPropertyProvider.Priority.FALLBACK, DependenciesTest.class, ctx -> {
                    assertSame(testStub, ctx.get(Properties.TEST_STUB));
                    return "Hello World";
                }))
                .add(new TestPropertyProvider<>(Properties.GOODBYE, TestPropertyProvider.Priority.FALLBACK, DependenciesTest.class, ctx -> {
                    String hello = ctx.get(Properties.HELLO_WORLD);
                    return hello + " was nice, but now: Goodbye!";
                }))
                .build();

        assertEquals("Hello World was nice, but now: Goodbye!", introspection.get(Properties.GOODBYE));
    }

    @Test
    public void check_cycling_self() {
        ThrowingRunnable run = () -> {
            TestIntrospectionImpl introspection = TestIntrospectionImpl.create(Scopes.ROOT)
                    .add(new TestPropertyProvider<>(Properties.HELLO_WORLD, TestPropertyProvider.Priority.FALLBACK, DependenciesTest.class, ctx -> {
                        ctx.get(Properties.HELLO_WORLD);
                        return "Hello World";
                    }))
                    .build();
            introspection.get(Properties.HELLO_WORLD);
        };

        assertThrows(RuntimeException.class, run);
    }

    @Test
    public void check_cycling_middleman() {
        Properties.TestStub testStub = new Properties.TestStub();

        ThrowingRunnable run = () -> {
            TestIntrospectionImpl introspection = TestIntrospectionImpl.create(Scopes.ROOT)
                    .add(new TestPropertyProvider<>(Properties.TEST_STUB, TestPropertyProvider.Priority.FALLBACK, DependenciesTest.class, ctx -> {
                        ctx.get(Properties.GOODBYE);
                        return testStub;
                    }))
                    .add(new TestPropertyProvider<>(Properties.HELLO_WORLD, TestPropertyProvider.Priority.FALLBACK, DependenciesTest.class, ctx -> {
                        assertSame(testStub, ctx.get(Properties.TEST_STUB));
                        return "Hello World";
                    }))
                    .add(new TestPropertyProvider<>(Properties.GOODBYE, TestPropertyProvider.Priority.FALLBACK, DependenciesTest.class, ctx -> {
                        String hello = ctx.get(Properties.HELLO_WORLD);
                        return hello + " was nice, but now: Goodbye!";
                    }))
                    .build();
            introspection.get(Properties.GOODBYE);
        };

        assertThrows(RuntimeException.class, run);
    }
}
