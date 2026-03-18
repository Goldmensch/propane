package spec;


import spec.internal.Event;
import spec.internal.GenTestEvent;

@Event(GenTestScope.ROOT)
public record FooGenerationEvent() implements GenTestEvent {
}
