import dev.goldmensch.propane.Introspection;
import dev.goldmensch.propane.Property;
import dev.goldmensch.propane.PropertyProvider;
import org.junit.Assert;
import org.junit.Test;

public class ChildIntrospectionTest {
    private enum Scopes implements Property.Scope {
        ROOT;

        @Override
        public int priority() {
            return ordinal();
        }
    }

    private class Properties {
        private record TestStub() {}

        static Property<String> ONE = new Property.SingleProperty<>("ONE", Property.Source.PROVIDED, Scopes.ROOT, String.class);
    }

    @Test
    public void parent_values_in_child() {
        Introspection parent = Introspection.create()
                .add(new PropertyProvider<>(Properties.ONE, PropertyProvider.Priority.FALLBACK, ChildIntrospectionTest.class, _ -> "one"))
                .build();

        Introspection child = parent.createChild().build();
        Assert.assertSame(parent.get(Properties.ONE), child.get(Properties.ONE)); // must be same instance (cache is copied)
    }

    @Test
    public void child_values_not_in_parent() {
        Introspection parent = Introspection.create()
                .build();

        // create child
        parent.createChild()
                .add(new PropertyProvider<>(Properties.ONE, PropertyProvider.Priority.FALLBACK, ChildIntrospectionTest.class, _ -> "one"))
                .build();

        Assert.assertThrows(RuntimeException.class, () -> parent.get(Properties.ONE)); // must be same instance (cache is copied)
    }

}
