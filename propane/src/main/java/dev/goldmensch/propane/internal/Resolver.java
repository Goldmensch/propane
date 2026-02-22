package dev.goldmensch.propane.internal;

import dev.goldmensch.propane.Introspection;
import dev.goldmensch.propane.internal.exposed.Properties;
import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.PropertyProvider;
import dev.goldmensch.propane.property.CollectionProperty;
import dev.goldmensch.propane.property.MapProperty;
import dev.goldmensch.propane.property.SingleProperty;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Resolver {
    public static final Resolver EMPTY = new Resolver(null, null, new dev.goldmensch.propane.internal.exposed.Properties(ScopeStub.INSTANCE));

    private static final ProviderExecutor executor = new ProviderExecutor();

    private final @Nullable Introspection introspection;
    private final @Nullable Resolver parent;
    private final ConcurrentHashMap<Property<?>, Object> cache = new ConcurrentHashMap<>();
    private final Map<Property<?>, List<PropertyProvider<?>>> providers;

    // if introspection and parent == null -> EMPTY resolver, get() -> always Optional.empty()
    private Resolver(@Nullable Introspection introspection, @Nullable Resolver parent, dev.goldmensch.propane.internal.exposed.Properties properties) {
        this.introspection = introspection;
        this.parent = parent;
        this.providers = properties.providers();
    }


    public Resolver createChild(Properties additional, Introspection child) {
        return new Resolver(child, this, additional);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(Property<T> property) {
        if (this.introspection == null && this.parent == null) return Optional.empty();

        T existing = (T) cache.get(property);
        if (existing != null) {
            return Optional.of(existing);
        }

        Optional<T> computed = compute(property);

        return switch (property) {
            case SingleProperty<T> _ -> computed
                    .or(() -> parent.get(property))
                    .flatMap(t -> putInCache(property, t));

            case MapProperty<?,?> _ -> computed
                    .or(() -> parent.get(property))
                    .map(t -> Map.copyOf((Map<?, ?>) t))
                    .flatMap(t -> putInCache(property, (T) t));

            case CollectionProperty<?> colP -> {
                Collection<Object> computedList = ((Optional<Collection<Object>>) computed).orElseGet(ArrayList::new);

                parent.get(property)
                        .ifPresent(t -> computedList.addAll((Collection<Object>) t));

                yield putInCache(colP, (T) List.copyOf(computedList));
            }
        };
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> putInCache(Property<?> property, T computed) {
        T raced = (T) cache.putIfAbsent(property, computed);
        T val = Objects.requireNonNullElse(raced, computed);
        return Optional.of(val);
    }

    @SuppressWarnings("unchecked")
    private  <T> Optional<T> compute(Property<T> property) {
        List<PropertyProvider<T>> currentProviders = Helpers.castUnsafe(providers.getOrDefault(property, List.of()));

        Result<T> result = switch (property) {
            case SingleProperty<T> _ -> handleOne(currentProviders, introspection);
            case CollectionProperty<?> _ -> (Result<T>) handleMany(
                    this.<Collection<Object>>castProvider(currentProviders),
                    ArrayList::new,
                    List::addAll,
                    introspection
            );

            case MapProperty<?, ?> _ -> (Result<T>) handleMany(
                    this.<Map<?, ?>>castProvider(currentProviders),
                    HashMap::new,
                    Map::putAll,
                    introspection
            );
        };

        if (result == null) {
            return Optional.empty();
        }

        return Optional.of(result.value);

    }

    private <T> Collection<PropertyProvider<T>> castProvider(List<?> providers) {
        return Helpers.castUnsafe(providers);
    }

    @Nullable
    private <T> Result<T> handleOne(Collection<PropertyProvider<T>> providers, Introspection introspection) {
        return providers.stream()
                .flatMap(provider -> {
                    T obj = executor.applyProvider(provider, introspection);

                    return Stream.ofNullable(obj)
                            .map(t -> new Result<>(t, List.of(provider.owner())));
                })
                .findFirst()
                .orElse(null);
    }

    private <T, B extends T> Result<T> handleMany(Collection<PropertyProvider<T>> providers, Supplier<B> collectionSup, BiConsumer<B, T> adder, Introspection introspection) {
        Collection<Class<?>> owners = new ArrayList<>();

        B collection = collectionSup.get();
        for (PropertyProvider<T> provider : providers) {
            if (shouldSkip(providers, provider)) {
                continue;
            }

            T applied = executor.applyProvider(provider, introspection);
            if (applied == null) continue;
            adder.accept(collection, applied);
            owners.add(provider.owner());
        }

        return new Result<>(collection, owners);
    }

    // if there are more than 1 provider, check if we should accumulate fallback values
    private <T> boolean shouldSkip(Collection<PropertyProvider<T>> providers, PropertyProvider<T> provider) {
        return providers.size() > 1
                && provider.priority() == PropertyProvider.Priority.FALLBACK
                && ((Property.MultiValue<T>) provider.property()).fallbackBehaviour() == Property.FallbackBehaviour.OVERRIDE;
    }


    private record Result<T>(T value, Collection<Class<?>> owners) {}
}
