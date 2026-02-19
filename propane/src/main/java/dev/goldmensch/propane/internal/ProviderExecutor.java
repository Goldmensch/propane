package dev.goldmensch.propane.internal;

import dev.goldmensch.propane.Introspection;
import dev.goldmensch.propane.Property;
import dev.goldmensch.propane.PropertyProvider;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProviderExecutor {
    private static final ScopedValue<SequencedMap<Property<?>, PropertyProvider<?>>> STACK = ScopedValue.newInstance();

    <T> T applyProvider(PropertyProvider<T> provider, Introspection introspection) {
        SequencedMap<Property<?>, PropertyProvider<?>> stack = STACK.isBound()
                ? new LinkedHashMap<>(STACK.get())
                : new LinkedHashMap<>();

        checkCycling(stack, provider);

        stack.putLast(provider.property(), provider);
        return ScopedValue.where(STACK, stack)
                .call(() -> provider.supplier().apply(introspection));
    }

    private void checkCycling(SequencedMap<Property<?>, PropertyProvider<?>> stack, PropertyProvider<?> current) {
        Property<?> property = current.property();
        if (stack.containsKey(property)) {
            SequencedCollection<PropertyProvider<?>> callchain = stack.sequencedValues();

            if (callchain.getLast().property().equals(property)) {
                throw new RuntimeException("cycling: call it self");
            }


            String tree = formatTree(callchain, current);
            throw new RuntimeException("cycling: cycling tree: " + tree);
        }
    }

    private String formatTree(SequencedCollection<PropertyProvider<?>> stack, PropertyProvider<?> current) {
        SequencedCollection<PropertyProvider<?>> shortStack = new ArrayList<>();
        for (PropertyProvider<?> p : stack.reversed()) {
            shortStack.add(p);
            if (p.property().equals(current.property())) break;
        }

        List<String> lines = shortStack.reversed().stream()
                .flatMap(frame -> Stream.of("↓ [requires]", "%s (provider in %s)".formatted(frame.property().name(), frame.owner())))
                .skip(1)
                .collect(Collectors.toList());


        int intend = lines.stream().map(String::length).max(Integer::compare).orElseThrow() + 3;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int missing = intend - line.length();
            String appendix;
            if (i == 0) {
                appendix = " ".repeat(missing) + "←--|";
            } else if (i == (lines.size() - 1)) {
                appendix = " ".repeat(missing) + "→--|";
            } else {
                appendix = " ".repeat(missing + 3) + "|";
            }
            lines.set(i, line + appendix);
        }
        return String.join(System.lineSeparator(), lines);
    }
}
