package dev.goldmensch.propane.internal;

import dev.goldmensch.propane.IntrospectionSkeleton;
import dev.goldmensch.propane.internal.exposed.Properties;
import dev.goldmensch.propane.property.*;
import dev.goldmensch.propane.property.PropertyProviderSkeleton;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class Resolver<INTROSPECTION extends IntrospectionSkeleton<INTROSPECTION, ?>> {
    private static final ProviderExecutor executor = new ProviderExecutor();

    private final @Nullable INTROSPECTION introspection;
    private final @Nullable Resolver<INTROSPECTION> parent;
    private final ConcurrentHashMap<Property<?>, Object> cache = new ConcurrentHashMap<>();
    private final Map<Property<?>, List<PropertyProviderSkeleton<?, ?, INTROSPECTION>>> providers;

    // if introspection and parent == null -> EMPTY resolver, get() -> always Optional.empty()
    private Resolver(@Nullable INTROSPECTION introspection, @Nullable Resolver<INTROSPECTION> parent, Properties<INTROSPECTION> properties) {
        this.introspection = introspection;
        this.parent = parent;
        this.providers = copy(properties.providers());
    }

    private Map<Property<?>, List<PropertyProviderSkeleton<?, ?, INTROSPECTION>>> copy(Map<Property<?>, List<PropertyProviderSkeleton<?, ?, INTROSPECTION>>> oldMap) {
        Map<Property<?>, List<PropertyProviderSkeleton<?, ?, INTROSPECTION>>> newMap = new HashMap<>();
        oldMap.forEach((property, providers) -> {
            List<PropertyProviderSkeleton<?, ?, INTROSPECTION>> list = providers.stream()
                    .sorted(Comparator.comparing(PropertyProviderSkeleton::priority))
                    .toList();

            newMap.put(property, list);
        });

        return Collections.unmodifiableMap(newMap);
    }

    public static <INTROSPECTION extends IntrospectionSkeleton<INTROSPECTION, ?>> Resolver<INTROSPECTION> createEmpty() {
        return new Resolver<INTROSPECTION>(null, null, new Properties<>(ScopeStub.INSTANCE));
    }

    public Resolver<INTROSPECTION> createChild(Properties<INTROSPECTION> additional, INTROSPECTION child) {
        return new Resolver<>(child, this, additional);
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
            case SingletonPropertySkeleton<T> _ -> computed
                    .or(() -> parent.get(property))
                    .flatMap(t -> putInCache(property, t));

            case MappingPropertySkeleton<?, ?> mapP -> {
                Map<Object, Object> computedMap = ((Optional<Map<Object, Object>>) computed).orElseThrow(); // handleMany never returns optional empty

                parent.get(mapP)
                        .ifPresent(t -> t.forEach(computedMap::putIfAbsent));

                yield putInCache(mapP, (T) Map.copyOf(computedMap));
            }

            case EnumerationPropertySkeleton<?> colP -> {
                Collection<Object> computedList = ((Optional<Collection<Object>>) computed).orElseThrow(); // handleMany never returns optional empty

                parent.get(colP)
                        .ifPresent(computedList::addAll);

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
        List<PropertyProviderSkeleton<T, ?, INTROSPECTION>> currentProviders = Helpers.castUnsafe(providers.getOrDefault(property, List.of()));

        Result<T> result = switch (property) {
            case SingletonPropertySkeleton<T> _ -> handleOne(currentProviders);
            case EnumerationPropertySkeleton<?> _ -> (Result<T>) handleMany(
                    this.<Collection<Object>>castProvider(currentProviders),
                    new ArrayList<>(),
                    List::addAll
            );

            case MappingPropertySkeleton<?, ?> _ -> (Result<T>) handleMany(
                    this.<Map<?, ?>>castProvider(currentProviders),
                    new HashMap<>(),
                    Map::putAll
            );
        };

        if (result == null) {
            return Optional.empty();
        }

        return Optional.of(result.value);

    }

    private <T> List<PropertyProviderSkeleton<T, ?, INTROSPECTION>> castProvider(List<?> providers) {
        return Helpers.castUnsafe(providers);
    }

    @Nullable
    private <T> Result<T> handleOne(Collection<PropertyProviderSkeleton<T, ?, INTROSPECTION>> providers) {
        return providers.stream()
                .flatMap(provider -> {
                    T obj = executor.applyProvider(provider, introspection);

                    return Stream.ofNullable(obj)
                            .map(t -> new Result<>(t, List.of(provider.owner())));
                })
                .findFirst()
                .orElse(null);
    }

    private <T, B extends T> Result<T> handleMany(Collection<PropertyProviderSkeleton<T, ?, INTROSPECTION>> providers, B collection, BiConsumer<B, T> adder) {
        Collection<Class<?>> owners = new ArrayList<>();

        for (PropertyProviderSkeleton<T, ?, INTROSPECTION> provider : providers) {
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
    private <T> boolean shouldSkip(Collection<PropertyProviderSkeleton<T, ?, INTROSPECTION>> providers, PropertyProviderSkeleton<T, ?, INTROSPECTION> provider) {
        return providers.size() > 1
                && provider.priority() == Priority.FALLBACK
                && ((Property.MultiValue<T>) provider.property().generalized()).fallbackStrategy() == Property.FallbackStrategy.IGNORE;
    }


    private record Result<T>(T value, Collection<Class<?>> owners) {}
}
