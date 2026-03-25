package logic;


import dev.goldmensch.propane.Scope;
import dev.goldmensch.propane.property.Property;
import logic.impl.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ChildIntrospectionTest {
    private enum Scopes implements Scope {
        ROOT;

        @Override
        public int priority() {
            return ordinal();
        }
    }

    private class Properties {
        private record TestStub() {}

        static TestProperty<String> ONE = new TestSingleProperty<>("ONE", Property.Source.PROVIDED, Scopes.ROOT, String.class);

        static TestProperty<TestStub> TWO = new TestSingleProperty<>("TWO", Property.Source.PROVIDED, Scopes.ROOT, TestStub.class);

        static TestProperty<Collection<TestStub>> COLLECTION = new TestCollectionProperty<>("COLLECTION", Property.Source.PROVIDED, Scopes.ROOT, TestStub.class, Property.FallbackBehaviour.ACCUMULATE);
        static TestProperty<Map<String, TestStub>> MAP = new TestMapProperty<>("MAP", Property.Source.PROVIDED, Scopes.ROOT, String.class, TestStub.class, Property.FallbackBehaviour.ACCUMULATE);
    }

    @Test
    public void parent_values_in_child() {
        TestIntrospectionImpl parent = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.TWO, TestPropertyProvider.Priority.FALLBACK, ChildIntrospectionTest.class, _ -> new Properties.TestStub()))
                .build();

        TestIntrospectionImpl child = parent.createChild(Scopes.ROOT).build();
        Assert.assertSame(parent.get(Properties.TWO), child.get(Properties.TWO)); // must be same instance (cache is copied)
    }

    @Test
    public void child_values_not_in_parent() {
        TestIntrospectionImpl parent = TestIntrospectionImpl.create(Scopes.ROOT)
                .build();

        // create child
        parent.createChild(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.ONE, TestPropertyProvider.Priority.FALLBACK, ChildIntrospectionTest.class, _ -> "foo"))
                .build();

        Assert.assertThrows(RuntimeException.class, () -> parent.get(Properties.ONE)); // must be same instance (cache is copied)
    }

    @Test
    public void singleton_replace_provider_in_child() {
        TestIntrospectionImpl parent = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.ONE, TestPropertyProvider.Priority.FALLBACK, ChildIntrospectionTest.class, _ -> "parent"))
                .build();

        TestIntrospectionImpl child = parent.createChild(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.ONE, TestPropertyProvider.Priority.FALLBACK, ChildIntrospectionTest.class, _ -> "child"))
                .build();

        Assert.assertEquals("child", child.get(Properties.ONE)); // must be same instance (cache is copied)
    }

    @Test
    public void singleton_always_take_last_inserted_value() {
        TestIntrospectionImpl introspection = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.ONE, TestPropertyProvider.Priority.FALLBACK, ChildIntrospectionTest.class, _ -> "one"))
                .add(new TestPropertyProvider<>(Properties.ONE, TestPropertyProvider.Priority.FALLBACK, ChildIntrospectionTest.class, _ -> "two"))
                .build();

        Assert.assertEquals("two", introspection.get(Properties.ONE));
    }

    @Test
    public void collection_should_be_joined_and_instance_reused() {
        TestIntrospectionImpl parent = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.COLLECTION, TestPropertyProvider.Priority.FALLBACK, ChildIntrospectionTest.class, _ -> List.of(new Properties.TestStub())))
                .build();
        Properties.TestStub parentVal = parent.get(Properties.COLLECTION).toArray(Properties.TestStub[]::new)[0];

        TestIntrospectionImpl child = parent.createChild(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.COLLECTION, TestPropertyProvider.Priority.FALLBACK, ChildIntrospectionTest.class, _ -> List.of(new Properties.TestStub())))
                .build();

        Properties.TestStub[] childArray = child.get(Properties.COLLECTION).toArray(Properties.TestStub[]::new);
        Assert.assertEquals(List.of(parentVal, childArray[1]), Arrays.asList(childArray)); // must be same instance (cache is copied)
    }

    @Test
    public void map_replace_provider_in_child() {
        TestIntrospectionImpl parent = TestIntrospectionImpl.create(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.MAP, TestPropertyProvider.Priority.FALLBACK, ChildIntrospectionTest.class, _ -> Map.of("parent", new Properties.TestStub())))
                .build();

        TestIntrospectionImpl child = parent.createChild(Scopes.ROOT)
                .add(new TestPropertyProvider<>(Properties.MAP, TestPropertyProvider.Priority.FALLBACK, ChildIntrospectionTest.class, _ -> Map.of("child", new Properties.TestStub())))
                .build();

        Assert.assertEquals(Map.of("child", new Properties.TestStub()), child.get(Properties.MAP)); // must be same instance (cache is copied)
    }

}
