package logic;

import dev.goldmensch.propane.Registry;
import dev.goldmensch.propane.Scope;
import dev.goldmensch.propane.event.Event;
import dev.goldmensch.propane.event.Listener;
import logic.impl.TestIntrospection;
import logic.impl.TestIntrospectionImpl;
import org.jspecify.annotations.NonNull;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class EventTest {

    static Registry<Scope> registry = new Registry<>(Map.of(
            FooEvent.class, Scopes.ROOT
    ));

    private enum Scopes implements Scope {
        FIRST,
        ROOT,
        OTHER;

        @Override
        public int priority() {
            return ordinal();
        }
    }

    public sealed interface TestEvent extends Event<Scope> permits FooEvent {
    }

    public record FooEvent(String value) implements TestEvent {
        @Override
        public Scope scope() {
            return Scopes.ROOT;
        }
    }

    public interface TestListener<E extends TestEvent> extends Listener<E, Scope, TestIntrospection> {
    }

    private static class FooListener implements TestListener<FooEvent> {

        @Override
        public void accept(FooEvent event, TestIntrospection introspection) {

        }

        @Override
        public @NonNull Class<FooEvent> event() {
            return FooEvent.class;
        }
    }

    @Test
    public void register() {
        ScopedValue.where(TestIntrospectionImpl.TEST_REGISTRY, registry).run(() -> {
            TestIntrospection introspection = TestIntrospectionImpl.create(Scopes.ROOT)
                    .build();

            introspection.subscribe(new FooListener());
        });
    }

    @Test
    public void register_parent_event() {
        ScopedValue.where(TestIntrospectionImpl.TEST_REGISTRY, registry).run(() -> {
            TestIntrospection introspection = TestIntrospectionImpl.create(Scopes.FIRST)
                    .build();

            introspection.subscribe(new FooListener());
        });
    }

    @Test
    public void register_wrong_scope() {
        ScopedValue.where(TestIntrospectionImpl.TEST_REGISTRY, registry).run(() -> {
            TestIntrospection introspection = TestIntrospectionImpl.create(Scopes.OTHER)
                    .build();

            Assert.assertThrows(RuntimeException.class, () -> introspection.subscribe(new FooListener()));
        });

    }

    @Test
    public void publish() {
        ScopedValue.where(TestIntrospectionImpl.TEST_REGISTRY, registry).run(() -> {
            TestIntrospectionImpl introspection = TestIntrospectionImpl.create(Scopes.ROOT)
                    .build();


            AtomicReference<FooEvent> executed = new AtomicReference<>();
            introspection.subscribe(Listener.create(FooEvent.class, (e, _) -> executed.set(e)));

            FooEvent event = new FooEvent("");
            introspection.publish(event);

            Assert.assertEquals(event, executed.get());
        });
    }

    @Test
    public void publish_wrong_scope() {
        ScopedValue.where(TestIntrospectionImpl.TEST_REGISTRY, registry).run(() -> {
            TestIntrospectionImpl introspection = TestIntrospectionImpl.create(Scopes.FIRST)
                    .build();

            Assert.assertThrows(RuntimeException.class, () -> introspection.publish(new FooEvent("")));
        });
    }
}
