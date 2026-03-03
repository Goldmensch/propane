package logic;

import dev.goldmensch.propane.PropertyProvider;
import dev.goldmensch.propane.property.Property;
import logic.impl.TestIntrospection;
import logic.impl.TestProperty;
import logic.impl.TestPropertyProvider;
import logic.impl.TestSingleProperty;
import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

public class ScopeTest {

    private enum Scopes implements Property.Scope {
        NEGATIVE(-1),
        ROOT(0),
        CHILD(1)
        ;

        private final int priority;

        Scopes(int priority) {
            this.priority = priority;
        }


        @Override
        public int priority() {
            return priority;
        }
    }

    private static class Properties {
        static TestProperty<String> NEGATIVE = new TestSingleProperty<>("NEGATIVE", Property.Source.PROVIDED, ScopeTest.Scopes.NEGATIVE, String.class);

        static TestProperty<String> ONE = new TestSingleProperty<>("ONE", Property.Source.PROVIDED, ScopeTest.Scopes.ROOT, String.class);

        static TestProperty<String> TWO = new TestSingleProperty<>("TWO", Property.Source.PROVIDED, ScopeTest.Scopes.CHILD, String.class);
    }

    @Test
    public void priority_must_not_negative() {
        ThrowingRunnable run = () -> {
            TestIntrospection.create(Scopes.NEGATIVE)
                    .add(new TestPropertyProvider<>(Properties.NEGATIVE, PropertyProvider.Priority.FALLBACK, ScopeTest.class, _ -> "negative"))
                    .build();
        };

        Assert.assertThrows(RuntimeException.class, run);
    }

    @Test
    public void child_TestIntrospection_same_scope() {
        TestIntrospection parent = TestIntrospection.create(Scopes.ROOT)
                .build();

        parent.createChild(Scopes.ROOT)
                .build();
    }

    @Test
    public void child_TestIntrospection_child_scope() {
        TestIntrospection parent = TestIntrospection.create(Scopes.ROOT)
                .build();

        parent.createChild(Scopes.CHILD)
                .build();
    }

    @Test
    public void child_TestIntrospection_parent_scope() {
        ThrowingRunnable run = () -> {
            TestIntrospection parent = TestIntrospection.create(Scopes.CHILD)
                    .build();

            parent.createChild(Scopes.ROOT)
                    .build();
        };

        Assert.assertThrows(RuntimeException.class, run);
    }

    @Test
    public void add_value_same_scope() {
        TestIntrospection.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.ONE, TestPropertyProvider.Priority.FALLBACK, ScopeTest.class, _ -> "one"))
                .build();
    }

    @Test
    public void add_value_parent_scope() {
        TestIntrospection.create(Scopes.CHILD)
                .add(new TestPropertyProvider<>(Properties.ONE, TestPropertyProvider.Priority.FALLBACK, ScopeTest.class, _ -> "one"))
                .build();
    }

    @Test
    public void add_value_child_scope() {
        ThrowingRunnable run = () -> {
            TestIntrospection.create(Scopes.ROOT)
                    .add(new TestPropertyProvider<>(Properties.TWO, TestPropertyProvider.Priority.FALLBACK, ScopeTest.class, _ -> "two"))
                    .build();
        };

        Assert.assertThrows(RuntimeException.class, run);
    }
}
