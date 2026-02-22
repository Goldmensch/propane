import dev.goldmensch.propane.Introspection;
import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.PropertyProvider;
import dev.goldmensch.propane.property.SingleProperty;
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
        static Property<String> NEGATIVE = new SingleProperty<>("NEGATIVE", Property.Source.PROVIDED, ScopeTest.Scopes.NEGATIVE, String.class);

        static Property<String> ONE = new SingleProperty<>("ONE", Property.Source.PROVIDED, ScopeTest.Scopes.ROOT, String.class);

        static Property<String> TWO = new SingleProperty<>("TWO", Property.Source.PROVIDED, ScopeTest.Scopes.CHILD, String.class);
    }

    @Test
    public void priority_must_not_negative() {
        ThrowingRunnable run = () -> {
            Introspection.create(Scopes.NEGATIVE)
                    .add(new PropertyProvider<>(Properties.NEGATIVE, PropertyProvider.Priority.FALLBACK, ScopeTest.class, _ -> "negative"))
                    .build();
        };

        Assert.assertThrows(RuntimeException.class, run);
    }

    @Test
    public void child_introspection_same_scope() {
        Introspection parent = Introspection.create(Scopes.ROOT)
                .build();

        parent.createChild(Scopes.ROOT)
                .build();
    }

    @Test
    public void child_introspection_child_scope() {
        Introspection parent = Introspection.create(Scopes.ROOT)
                .build();

        parent.createChild(Scopes.CHILD)
                .build();
    }

    @Test
    public void child_introspection_parent_scope() {
        ThrowingRunnable run = () -> {
            Introspection parent = Introspection.create(Scopes.CHILD)
                    .build();

            parent.createChild(Scopes.ROOT)
                    .build();
        };

        Assert.assertThrows(RuntimeException.class, run);
    }

    @Test
    public void add_value_same_scope() {
        Introspection.create(Scopes.ROOT)
                .add(new PropertyProvider<>(Properties.ONE, PropertyProvider.Priority.FALLBACK, ScopeTest.class, _ -> "one"))
                .build();
    }

    @Test
    public void add_value_parent_scope() {
        Introspection.create(Scopes.CHILD)
                .add(new PropertyProvider<>(Properties.ONE, PropertyProvider.Priority.FALLBACK, ScopeTest.class, _ -> "one"))
                .build();
    }

    @Test
    public void add_value_child_scope() {
        ThrowingRunnable run = () -> {
            Introspection.create(Scopes.ROOT)
                    .add(new PropertyProvider<>(Properties.TWO, PropertyProvider.Priority.FALLBACK, ScopeTest.class, _ -> "two"))
                    .build();
        };

        Assert.assertThrows(RuntimeException.class, run);
    }
}
