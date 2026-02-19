package dev.goldmensch.propane.internal;

import dev.goldmensch.propane.Introspection;
import dev.goldmensch.propane.Property;
import dev.goldmensch.propane.PropertyProvider;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Resolver {
    @SuppressWarnings("SortedCollectionWithNonComparableKeys")
    private static final SortedSet<PropertyProvider<?>> EMPTY_SORTEDSET = new TreeSet<>();
    private static final ProviderExecutor executor = new ProviderExecutor();

    private final Map<Property<?>, Object> cache = new ConcurrentHashMap<>();
    private final Map<Property<?>, SortedSet<PropertyProvider<?>>> providers;

    Resolver(Map<Property<?>, SortedSet<PropertyProvider<?>>> providers) {
        this.providers = deepImmutableCopy(providers);
    }

    private Map<Property<?>, SortedSet<PropertyProvider<?>>> deepImmutableCopy(Map<Property<?>, SortedSet<PropertyProvider<?>>> oldMap) {
        Map<Property<?>, SortedSet<PropertyProvider<?>>> newMap = new HashMap<>();
        oldMap.forEach((property, providers) -> newMap.put(property, Collections.unmodifiableSortedSet(new TreeSet<>(providers))));
        return Collections.unmodifiableMap(newMap);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Property<T> property, Introspection introspection) {
        if (cache.containsKey(property)) return (T) cache.get(property);

        // for thread safety
        T computed = compute(property, introspection);
        var old = (T) cache.putIfAbsent(property, computed);
        return old == null ? computed : old;
    }

    @SuppressWarnings("unchecked")
    public <T> T compute(Property<T> property, Introspection introspection) {
        SortedSet<PropertyProvider<T>> currentProviders = Helpers.castUnsafe(providers.getOrDefault(property, EMPTY_SORTEDSET));

        Result<T> result = switch (property) {
            case Property.SingleProperty<T> _ -> handleOne(currentProviders, introspection);
            case Property.CollectionProperty<?> _ -> (Result<T>) handleMany(
                    Helpers.<SortedSet<PropertyProvider<Collection<Object>>>>castUnsafe(currentProviders),
                    ArrayList::new,
                    List::addAll,
                    introspection
            );

            case Property.MapProperty<?, ?> _ -> (Result<T>) handleMany(
                    Helpers.<SortedSet<PropertyProvider<Map<Object, Object>>>>castUnsafe(currentProviders),
                    HashMap::new,
                    java.util.Map::putAll,
                    introspection
            );
        };

        return (T) switch (result) {
            case Result(List<?> list, _) -> Collections.unmodifiableList(list);
            case Result(Map<?, ?> map, _) -> Collections.unmodifiableMap(map);
            case Result(T val, _) -> val;
        };
    }


    private <T> Result<T> handleOne(SortedSet<PropertyProvider<T>> providers, Introspection introspection) {
        return providers.stream()
                .flatMap(provider -> {
                    T obj = executor.applyProvider(provider, introspection);

                    return Stream.ofNullable(obj)
                            .map(t -> new Result<>(t, List.of(provider.owner())));
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("property-not-set"));
    }

    private <T, B extends T> Result<T> handleMany(SortedSet<PropertyProvider<T>> providers, Supplier<B> collectionSup, BiConsumer<B, T> adder, Introspection introspection) {
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

    private <T> boolean shouldSkip(SortedSet<PropertyProvider<T>> providers, PropertyProvider<T> provider) {
        return providers.size() > 1
                && provider.priority() == PropertyProvider.FALLBACK_PRIORITY
                && ((Property.MultiValue<T>) provider.property()).fallbackBehaviour() == Property.FallbackBehaviour.OVERRIDE;
    }

    public Properties properties() {
        Properties properties = new Properties();
        providers.values()
                .stream()
                .flatMap(SortedSet::stream)
                .forEach(properties::add);

        return properties;
    }

    private record Result<T>(T value, Collection<Class<?>> owners) {}
}
