import dev.goldmensch.propane.Introspection;
import dev.goldmensch.propane.Property;
import dev.goldmensch.propane.PropertyProvider;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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

        static Property<TestStub> TWO = new Property.SingleProperty<>("TWO", Property.Source.PROVIDED, Scopes.ROOT, TestStub.class);

        static Property<Collection<TestStub>> COLLECTION = new Property.CollectionProperty<>("COLLECTION", Property.Source.PROVIDED, Scopes.ROOT, TestStub.class, Property.FallbackBehaviour.ACCUMULATE);
        static Property<Map<String, TestStub>> MAP = new Property.MapProperty<>("MAP", Property.Source.PROVIDED, Scopes.ROOT, String.class, TestStub.class, Property.FallbackBehaviour.ACCUMULATE);
    }

    @Test
    public void parent_values_in_child() {
        Introspection parent = Introspection.create()
                .add(new PropertyProvider<>(Properties.TWO, PropertyProvider.Priority.FALLBACK, ChildIntrospectionTest.class, _ -> new Properties.TestStub()))
                .build();

        Introspection child = parent.createChild().build();
        Assert.assertSame(parent.get(Properties.TWO), child.get(Properties.TWO)); // must be same instance (cache is copied)
    }

    @Test
    public void child_values_not_in_parent() {
        Introspection parent = Introspection.create()
                .build();

        // create child
        parent.createChild()
                .add(new PropertyProvider<>(Properties.ONE, PropertyProvider.Priority.FALLBACK, ChildIntrospectionTest.class, _ -> "foo"))
                .build();

        Assert.assertThrows(RuntimeException.class, () -> parent.get(Properties.ONE)); // must be same instance (cache is copied)
    }

    @Test
    public void singleton_replace_provider_in_child() {
        Introspection parent = Introspection.create()
                .add(new PropertyProvider<>(Properties.ONE, PropertyProvider.Priority.FALLBACK, ChildIntrospectionTest.class, _ -> "parent"))
                .build();

        Introspection child = parent.createChild()
                .add(new PropertyProvider<>(Properties.ONE, PropertyProvider.Priority.FALLBACK, ChildIntrospectionTest.class, _ -> "child"))
                .build();

        Assert.assertEquals("child", child.get(Properties.ONE)); // must be same instance (cache is copied)
    }

    @Test
    public void singleton_always_take_last_inserted_value() {
        Introspection introspection = Introspection.create()
                .add(new PropertyProvider<>(Properties.ONE, PropertyProvider.Priority.FALLBACK, ChildIntrospectionTest.class, _ -> "one"))
                .add(new PropertyProvider<>(Properties.ONE, PropertyProvider.Priority.FALLBACK, ChildIntrospectionTest.class, _ -> "two"))
                .build();

        Assert.assertEquals("two", introspection.get(Properties.ONE));
    }

    @Test
    public void collection_should_be_joined_and_instance_reused() {
        Introspection parent = Introspection.create()
                .add(new PropertyProvider<>(Properties.COLLECTION, PropertyProvider.Priority.FALLBACK, ChildIntrospectionTest.class, _ -> List.of(new Properties.TestStub())))
                .build();
        Properties.TestStub parentVal = parent.get(Properties.COLLECTION).toArray(Properties.TestStub[]::new)[0];

        Introspection child = parent.createChild()
                .add(new PropertyProvider<>(Properties.COLLECTION, PropertyProvider.Priority.FALLBACK, ChildIntrospectionTest.class, _ -> List.of(new Properties.TestStub())))
                .build();

        Properties.TestStub[] childArray = child.get(Properties.COLLECTION).toArray(Properties.TestStub[]::new);
        Assert.assertEquals(List.of(parentVal, childArray[1]), Arrays.asList(childArray)); // must be same instance (cache is copied)
    }

    @Test
    public void map_replace_provider_in_child() {
        Introspection parent = Introspection.create()
                .add(new PropertyProvider<>(Properties.MAP, PropertyProvider.Priority.FALLBACK, ChildIntrospectionTest.class, _ -> Map.of("parent", new Properties.TestStub())))
                .build();

        Introspection child = parent.createChild()
                .add(new PropertyProvider<>(Properties.MAP, PropertyProvider.Priority.FALLBACK, ChildIntrospectionTest.class, _ -> Map.of("child", new Properties.TestStub())))
                .build();

        Assert.assertEquals(Map.of("child", new Properties.TestStub()), child.get(Properties.MAP)); // must be same instance (cache is copied)
    }

}
