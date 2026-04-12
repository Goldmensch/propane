package logic;

import dev.goldmensch.propane.Registry;
import dev.goldmensch.propane.Scope;
import dev.goldmensch.propane.event.Event;
import dev.goldmensch.propane.event.Listener;
import dev.goldmensch.propane.property.Priority;
import dev.goldmensch.propane.property.Property;
import logic.impl.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ScopedAccessTest {

    static Registry<Scope> registry = new Registry<>(Map.of(
            FooEvent.class, Scopes.ROOT
    ));

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
    }

    private class FooEvent implements Event<Scope> {

        @Override
        public Scopes scope() {
            return Scopes.ROOT;
        }
    }

    @Test
    public void scoped_get() {
        TestIntrospectionImpl build = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.HELLO_WORLD, Priority.FALLBACK, ScopedAccessTest.class, _ -> "hi"))
                .build();

        build.scoped().run(() -> {
            String s = TestIntrospection.scopedGet(Properties.HELLO_WORLD);
            Assert.assertEquals("hi", s);
        });
    }

    @Test
    public void scoped_get_on_property() {
        TestIntrospectionImpl build = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.HELLO_WORLD, Priority.FALLBACK, ScopedAccessTest.class, _ -> "hi"))
                .build();

        build.scoped().run(() -> {
            Assert.assertEquals("hi", Properties.HELLO_WORLD.scopedGet());
        });
    }

    @Test
    public void scoped_access_introspection() {
        TestIntrospectionImpl build = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.HELLO_WORLD, Priority.FALLBACK, ScopedAccessTest.class, _ -> "hi"))
                .build();

        build.scoped().run(() -> {
            TestIntrospection introspection = TestIntrospection.accessScoped();
            Assert.assertEquals(build, introspection);
        });
    }

    @Test
    public void scoped_accessible() {
        TestIntrospectionImpl build = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.HELLO_WORLD, Priority.FALLBACK, ScopedAccessTest.class, _ -> "hi"))
                .build();

        build.scoped().run(() -> {
            boolean val = TestIntrospection.accessible();
            Assert.assertTrue(val);
        });

        boolean val = TestIntrospection.accessible();
        Assert.assertFalse(val);
    }

    @Test
    public void inside_listener() {
        ScopedValue.where(TestIntrospectionImpl.TEST_REGISTRY, registry).run(() -> {
            TestIntrospectionImpl build = TestIntrospectionImpl.create(Scopes.ROOT)
                    .add(new TestPropertyProvider<>(Properties.HELLO_WORLD, Priority.FALLBACK, ScopedAccessTest.class, _ -> "hi"))
                    .build();

            AtomicReference<String> val = new AtomicReference<>();
            build.subscribe(Listener.create(FooEvent.class, (_, _) -> {
                String v = TestIntrospection.scopedGet(Properties.HELLO_WORLD);
                val.set(v);
            }));

            build.publish(new FooEvent());

            Assert.assertEquals("hi", val.get());
        });

    }
}
