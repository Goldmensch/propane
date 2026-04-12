package logic;

import dev.goldmensch.propane.Scope;
import dev.goldmensch.propane.property.PropertyProviderSkeleton;
import dev.goldmensch.propane.property.Property;
import logic.impl.*;
import org.junit.Assert;
import org.junit.Test;

public class ScopedAccessTest {

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
        static TestProperty<Properties.TestStub> TEST_STUB = new TestSingletonProperty<>("TEST_STUB", Property.Source.EXTENSION, Scopes.ROOT, Properties.TestStub.class);
    }

    @Test
    public void scoped_get() {
        TestIntrospectionImpl build = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.HELLO_WORLD, PropertyProviderSkeleton.Priority.FALLBACK, ScopedAccessTest.class, _ -> "hi"))
                .build();

        ScopedValue.where(TestIntrospectionImpl.INTROSPECTION, build).run(() -> {
            String s = TestIntrospection.scopedGet(Properties.HELLO_WORLD);
            Assert.assertEquals("hi", s);
        });
    }

    @Test
    public void scoped_get_on_property() {
        TestIntrospectionImpl build = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.HELLO_WORLD, PropertyProviderSkeleton.Priority.FALLBACK, ScopedAccessTest.class, _ -> "hi"))
                .build();

        ScopedValue.where(TestIntrospectionImpl.INTROSPECTION, build).run(() -> {
            Assert.assertEquals("hi", Properties.HELLO_WORLD.scopedGet());
        });
    }

    @Test
    public void scoped_access_introspection() {
        TestIntrospectionImpl build = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.HELLO_WORLD, PropertyProviderSkeleton.Priority.FALLBACK, ScopedAccessTest.class, _ -> "hi"))
                .build();

        ScopedValue.where(TestIntrospectionImpl.INTROSPECTION, build).run(() -> {
            TestIntrospection introspection = TestIntrospection.accessScoped();
            Assert.assertEquals(build, introspection);
        });
    }

    @Test
    public void scoped_accessible() {
        TestIntrospectionImpl build = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.HELLO_WORLD, PropertyProviderSkeleton.Priority.FALLBACK, ScopedAccessTest.class, _ -> "hi"))
                .build();

        ScopedValue.where(TestIntrospectionImpl.INTROSPECTION, build).run(() -> {
            boolean val = TestIntrospection.accessible();
            Assert.assertTrue(val);
        });

        boolean val = TestIntrospection.accessible();
        Assert.assertFalse(val);
    }
}
