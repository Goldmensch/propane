package dev.goldmensch.propane.internal;

import dev.goldmensch.propane.IntrospectionSkeleton;
import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.property.PropertyProviderSkeleton;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProviderExecutor {
    private static final ScopedValue<SequencedMap<Property<?>, PropertyProviderSkeleton<?, ?, ?>>> STACK = ScopedValue.newInstance();

    @Nullable
    <T, I extends IntrospectionSkeleton<I, ?>> T applyProvider(PropertyProviderSkeleton<T, ?, I> provider, I introspection) {
        SequencedMap<Property<?>, PropertyProviderSkeleton<?, ?, ?>> stack = STACK.isBound()
                ? new LinkedHashMap<>(STACK.get())
                : new LinkedHashMap<>();

        checkCycling(stack, provider);

        stack.putLast(provider.property().generalized(), provider);
        return ScopedValue.where(STACK, stack)
                .call(() -> provider.supplier().apply(introspection));
    }

    private void checkCycling(SequencedMap<Property<?>, PropertyProviderSkeleton<?, ?, ?>> stack, PropertyProviderSkeleton<?, ?, ?> current) {
        Property<?> property = current.property().generalized();
        if (stack.containsKey(property)) {
            SequencedCollection<PropertyProviderSkeleton<?, ?, ?>> callchain = stack.sequencedValues();

            if (callchain.getLast().property().equals(property)) {
                throw new RuntimeException("cycling: call it self");
            }

            String tree = formatTree(callchain, current);
            throw new RuntimeException("cycling: cycling tree: " + tree);
        }
    }

    private String formatTree(SequencedCollection<PropertyProviderSkeleton<?, ?, ?>> stack, PropertyProviderSkeleton<?, ?, ?> current) {
        SequencedCollection<PropertyProviderSkeleton<?, ?, ?>> shortStack = new ArrayList<>();
        for (PropertyProviderSkeleton<?, ?, ?> p : stack.reversed()) {
            shortStack.add(p);
            if (p.property().equals(current.property())) break;
        }

        List<String> lines = shortStack.reversed().stream()
                .flatMap(frame -> Stream.of("↓ [requires]", "%s (provider in %s)".formatted(frame.property().generalized().name(), frame.owner())))
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
