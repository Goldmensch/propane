package logic;

import dev.goldmensch.propane.Scope;
import dev.goldmensch.propane.property.Priority;
import dev.goldmensch.propane.property.Property;
import logic.impl.TestIntrospectionImpl;
import logic.impl.TestProperty;
import logic.impl.TestPropertyProvider;
import logic.impl.TestSingletonProperty;
import org.junit.Assert;
import org.junit.Test;

public class SourceTest {

    private enum Scopes implements Scope {
        ROOT;

        @Override
        public int priority() {
            return ordinal();
        }
    }

    private static class Properties {
        static TestProperty<String> BUILDER_PROP = new TestSingletonProperty<>("BUILDER_PROP", Property.Source.BUILDER, Scopes.ROOT, String.class);
        static TestProperty<String> PROVIDED_PROP = new TestSingletonProperty<>("PROVIDED_PROP", Property.Source.PROVIDED, Scopes.ROOT, String.class);
        static TestProperty<String> EXTENSION_PROP = new TestSingletonProperty<>("EXTENSION_PROP", Property.Source.EXTENSION, Scopes.ROOT, String.class);
    }

    @Test
    public void builder_accept_builder_fallback() {
        TestIntrospectionImpl.create(Scopes.ROOT)
                .addBuilder(Properties.BUILDER_PROP, _ -> "bruch")
                .addFallback(Properties.BUILDER_PROP, _ -> "bruch")
                .build();
    }

    @Test
    public void builder_reject_builder_extension() {
        Assert.assertThrows(RuntimeException.class, () -> {
            TestIntrospectionImpl.create(Scopes.ROOT)
                    .add(new TestPropertyProvider<>(Properties.BUILDER_PROP, Priority.of(500), SourceTest.class, _ -> "bruch"))
                    .build();
        });
    }

    @Test
    public void provided_accept_fallback() {
        TestIntrospectionImpl.create(Scopes.ROOT)
                .addFallback(Properties.PROVIDED_PROP, _ -> "bruch")
                .build();
    }

    @Test
    public void provided_reject_extension_priority() {
        Assert.assertThrows(RuntimeException.class, () -> {
            TestIntrospectionImpl.create(Scopes.ROOT)
                    .add(new TestPropertyProvider<>(Properties.PROVIDED_PROP, Priority.of(500), SourceTest.class, _ -> "bruch"))
                    .build();
        });
    }

    @Test
    public void provided_reject_builder_priority() {
        Assert.assertThrows(RuntimeException.class, () -> {
            TestIntrospectionImpl.create(Scopes.ROOT)
                    .addBuilder(Properties.PROVIDED_PROP, _ -> "bruch")
                    .build();
        });
    }

    @Test
    public void extension_accept_all_priorities() {
        TestIntrospectionImpl.create(Scopes.ROOT)
                .addBuilder(Properties.EXTENSION_PROP, _ -> "bruch")
                .addFallback(Properties.EXTENSION_PROP, _ -> "bruch")
                .add(new TestPropertyProvider<>(Properties.EXTENSION_PROP, Priority.of(500), SourceTest.class, _ -> "bruch"))
                .build();
    }


}
