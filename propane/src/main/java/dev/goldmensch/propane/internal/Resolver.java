package dev.goldmensch.propane.internal;

import dev.goldmensch.propane.Introspection;
import dev.goldmensch.propane.internal.exposed.Properties;
import dev.goldmensch.propane.property.*;
import dev.goldmensch.propane.PropertyProvider;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Resolver<SP extends SpecificProperty<?>, INTROSPECTION extends Introspection<SP>> {
    private static final ProviderExecutor executor = new ProviderExecutor();

    private final @Nullable INTROSPECTION introspection;
    private final @Nullable Resolver<SP, INTROSPECTION> parent;
    private final ConcurrentHashMap<Property<?>, Object> cache = new ConcurrentHashMap<>();
    private final Map<Property<?>, List<Special<?>>> providers;

    // if introspection and parent == null -> EMPTY resolver, get() -> always Optional.empty()
    private Resolver(@Nullable INTROSPECTION introspection, @Nullable Resolver<SP, INTROSPECTION> parent, dev.goldmensch.propane.internal.exposed.Properties<SP, INTROSPECTION> properties) {
        this.introspection = introspection;
        this.parent = parent;
        this.providers = copy(properties.providers());
    }

    private Map<Property<?>, List<Special<?>>> copy(Map<SP, List<PropertyProvider<?, ?, SP, INTROSPECTION>>> oldMap) {
        Map<Property<?>, List<Special<?>>> newMap = new HashMap<>();
        oldMap.forEach((property, providers) -> {
            List<Special<?>> list = providers.stream()
                    .map(this::from)
                    .sorted(Comparator.comparing(PropertyProvider::priority))
                    .collect(Collectors.toUnmodifiableList());

            newMap.put(property.generalized(), list);
        });

        return Collections.unmodifiableMap(newMap);
    }

    private <T> Special<?> from(PropertyProvider<T, ?, SP, INTROSPECTION> provider) {
        return new Special<>(provider.property(), provider.priority(), provider.owner(), provider.supplier());
    }

    private final class Special<T> extends PropertyProvider<T, SpecificProperty<T>, SP, INTROSPECTION> {
        public Special(SpecificProperty<T> property, Priority priority, Class<?> owner, Function<INTROSPECTION, T> supplier) {
            super(property, priority, owner, supplier);
        }
    }

    public static <SP extends SpecificProperty<?>, INTROSPECTION extends Introspection<SP>> Resolver<SP, INTROSPECTION> createEmpty() {
        return new Resolver<>(null, null, new Properties<>(ScopeStub.INSTANCE));
    }

    public Resolver<SP, INTROSPECTION> createChild(Properties<SP, INTROSPECTION> additional, INTROSPECTION child) {
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
        List<Special<T>> currentProviders = Helpers.castUnsafe(providers.getOrDefault(property, List.of()));

        Result<T> result = switch (property) {
            case SingleProperty<T> _ -> handleOne(currentProviders);
            case CollectionProperty<?> _ -> (Result<T>) handleMany(
                    this.<Collection<Object>>castProvider(currentProviders),
                    new ArrayList<>(),
                    List::addAll
            );

            case MapProperty<?, ?> _ -> (Result<T>) handleMany(
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

    private <T> List<Special<T>> castProvider(List<?> providers) {
        return Helpers.castUnsafe(providers);
    }

    @Nullable
    private <T> Result<T> handleOne(Collection<Special<T>> providers) {
        return providers.stream()
                .flatMap(provider -> {
                    T obj = executor.applyProvider(provider, introspection);

                    return Stream.ofNullable(obj)
                            .map(t -> new Result<>(t, List.of(provider.owner())));
                })
                .findFirst()
                .orElse(null);
    }

    private <T, B extends T> Result<T> handleMany(Collection<Special<T>> providers, B collection, BiConsumer<B, T> adder) {
        Collection<Class<?>> owners = new ArrayList<>();

        for (Special<T> provider : providers) {
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
    private <T> boolean shouldSkip(Collection<Special<T>> providers, Special<T> provider) {
        return providers.size() > 1
                && provider.priority() == PropertyProvider.Priority.FALLBACK
                && ((Property.MultiValue<T>) provider.property().generalized()).fallbackBehaviour() == Property.FallbackBehaviour.OVERRIDE;
    }


    private record Result<T>(T value, Collection<Class<?>> owners) {}
}
