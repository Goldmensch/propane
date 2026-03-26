package dev.goldmensch.propane.internal;

import dev.goldmensch.propane.Introspection;
import dev.goldmensch.propane.internal.exposed.Properties;
import dev.goldmensch.propane.property.*;
import dev.goldmensch.propane.property.PropertyProvider;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class Resolver<INTROSPECTION extends Introspection<INTROSPECTION, ?>> {
    private static final ProviderExecutor executor = new ProviderExecutor();

    private final @Nullable INTROSPECTION introspection;
    private final @Nullable Resolver<INTROSPECTION> parent;
    private final ConcurrentHashMap<Property<?>, Object> cache = new ConcurrentHashMap<>();
    private final Map<Property<?>, List<PropertyProvider<?, ?, INTROSPECTION>>> providers;

    // if introspection and parent == null -> EMPTY resolver, get() -> always Optional.empty()
    private Resolver(@Nullable INTROSPECTION introspection, @Nullable Resolver<INTROSPECTION> parent, Properties<INTROSPECTION> properties) {
        this.introspection = introspection;
        this.parent = parent;
        this.providers = copy(properties.providers());
    }

    private Map<Property<?>, List<PropertyProvider<?, ?, INTROSPECTION>>> copy(Map<Property<?>, List<PropertyProvider<?, ?, INTROSPECTION>>> oldMap) {
        Map<Property<?>, List<PropertyProvider<?, ?, INTROSPECTION>>> newMap = new HashMap<>();
        oldMap.forEach((property, providers) -> {
            List<PropertyProvider<?, ?, INTROSPECTION>> list = providers.stream()
                    .sorted(Comparator.comparing(PropertyProvider::priority))
                    .toList();

            newMap.put(property, list);
        });

        return Collections.unmodifiableMap(newMap);
    }

    public static <INTROSPECTION extends Introspection<INTROSPECTION, ?>> Resolver<INTROSPECTION> createEmpty() {
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
            case SingletonProperty<T> _ -> computed
                    .or(() -> parent.get(property))
                    .flatMap(t -> putInCache(property, t));

            case MappingProperty<?,?> _ -> computed
                    .or(() -> parent.get(property))
                    .map(t -> Map.copyOf((Map<?, ?>) t))
                    .flatMap(t -> putInCache(property, (T) t));

            case EnumerationProperty<?> colP -> {
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
        List<PropertyProvider<T, ?, INTROSPECTION>> currentProviders = Helpers.castUnsafe(providers.getOrDefault(property, List.of()));

        Result<T> result = switch (property) {
            case SingletonProperty<T> _ -> handleOne(currentProviders);
            case EnumerationProperty<?> _ -> (Result<T>) handleMany(
                    this.<Collection<Object>>castProvider(currentProviders),
                    new ArrayList<>(),
                    List::addAll
            );

            case MappingProperty<?, ?> _ -> (Result<T>) handleMany(
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

    private <T> List<PropertyProvider<T, ?, INTROSPECTION>> castProvider(List<?> providers) {
        return Helpers.castUnsafe(providers);
    }

    @Nullable
    private <T> Result<T> handleOne(Collection<PropertyProvider<T, ?, INTROSPECTION>> providers) {
        return providers.stream()
                .flatMap(provider -> {
                    T obj = executor.applyProvider(provider, introspection);

                    return Stream.ofNullable(obj)
                            .map(t -> new Result<>(t, List.of(provider.owner())));
                })
                .findFirst()
                .orElse(null);
    }

    private <T, B extends T> Result<T> handleMany(Collection<PropertyProvider<T, ?, INTROSPECTION>> providers, B collection, BiConsumer<B, T> adder) {
        Collection<Class<?>> owners = new ArrayList<>();

        for (PropertyProvider<T, ?, INTROSPECTION> provider : providers) {
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
    private <T> boolean shouldSkip(Collection<PropertyProvider<T, ?, INTROSPECTION>> providers, PropertyProvider<T, ?, INTROSPECTION> provider) {
        return providers.size() > 1
                && provider.priority() == PropertyProvider.Priority.FALLBACK
                && ((Property.MultiValue<T>) provider.property().generalized()).fallbackBehaviour() == Property.FallbackBehaviour.OVERRIDE;
    }


    private record Result<T>(T value, Collection<Class<?>> owners) {}
}
