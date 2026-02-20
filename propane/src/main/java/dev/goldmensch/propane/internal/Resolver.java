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
    public static Resolver EMPTY = new Resolver(Map.of(), new ConcurrentHashMap<>());

    private static final ProviderExecutor executor = new ProviderExecutor();

    private final ConcurrentHashMap<Property<?>, CacheEntry> cache;
    private final Map<Property<?>, List<PropertyProvider<?>>> providers;

    /// Creates a copy of the passed providers (so that the user can't change them after Resolver creation through [Introspection.Builder#add(PropertyProvider)].
    /// The cache isn't exposed as a public API, thus it's directly used. It never leaves this class
    private Resolver(Map<Property<?>, List<PropertyProvider<?>>> providers, ConcurrentHashMap<Property<?>, CacheEntry> cache) {
        this.providers = providers;
        this.cache = cache;
    }


    // Invariant:
    // merge == true only originates from createChild()
    // and is monotonic within a Resolver instance (true -> false).
    // get() never writes merge=true.
    @SuppressWarnings("unchecked")
    public <T> T get(Property<T> property, Introspection introspection) {
        CacheEntry existing = cache.get(property);
        if (existing != null && !existing.merge) {
            return (T) existing.value;
        }

        // for thread safety
        T computed = compute(property, introspection);

        // if we have an exising, it must be old and merged
        if (existing != null) {
            ArrayList<T> copy = new ArrayList<>((List<T>) computed);
            copy.addAll((Collection<T>) existing.value);

            List<T> finalList = Collections.unmodifiableList(copy);
            if (cache.replace(property, existing, new CacheEntry(finalList, false))) {
                return (T) finalList;
            }
        } else {
            if (cache.putIfAbsent(property, new CacheEntry(computed, false)) == null) {
                return computed;
            }
        }

        // must be newly computed (and maybe) merged value
        return (T) cache.get(property);
    }

    @SuppressWarnings("unchecked")
    public <T> T compute(Property<T> property, Introspection introspection) {
        List<PropertyProvider<T>> currentProviders = Helpers.castUnsafe(providers.getOrDefault(property, List.of()));

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
                    Map::putAll,
                    introspection
            );
        };

        return (T) switch (result) {
            case Result(List<?> list, _) -> Collections.unmodifiableList(list);
            case Result(Map<?, ?> map, _) -> Collections.unmodifiableMap(map);
            case Result(T val, _) -> val;
        };
    }

    public void ensureAllComputed(Introspection introspection) {
        providers.keySet().forEach(property -> get(property, introspection));
    }


    private <T> Result<T> handleOne(Collection<PropertyProvider<T>> providers, Introspection introspection) {
        return providers.stream()
                .flatMap(provider -> {
                    T obj = executor.applyProvider(provider, introspection);

                    return Stream.ofNullable(obj)
                            .map(t -> new Result<>(t, List.of(provider.owner())));
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("property-not-set"));
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

    private Properties propertiesCopy() {
        Properties properties = new Properties();
        providers.values()
                .stream()
                .flatMap(List::stream)
                .forEach(properties::add);

        return properties;
    }

    public Resolver createChild(Properties additional) {
        Properties properties = propertiesCopy();
        properties.addAll(additional);

        ConcurrentHashMap<Property<?>, CacheEntry> cacheCopy = new ConcurrentHashMap<>(cache);

        // invalidate caches / update merge information
        additional.providers().forEach((property, _) -> {
            switch (property) {
                case Property.SingleProperty<?> _ -> cacheCopy.remove(property);
                case Property.CollectionProperty<?> _ -> cacheCopy.computeIfPresent(property, (_, old) -> new CacheEntry(old.value, true));
                default -> {}
            }
        });

        return new Resolver(deepImmutableCopy(properties.providers()), cacheCopy);
    }

    private static Map<Property<?>, List<PropertyProvider<?>>> deepImmutableCopy(Map<Property<?>, List<PropertyProvider<?>>> oldMap) {
        Map<Property<?>, List<PropertyProvider<?>>> newMap = new HashMap<>();
        oldMap.forEach((property, providers) -> newMap.put(property, List.copyOf(providers)));
        return Collections.unmodifiableMap(newMap);
    }

    // merge indicates that the cached value must be merged once in Resolver#get
    // It can only transition from true to false within one Resolver, merge=true can only be set in createChild
    private record CacheEntry(Object value, boolean merge) {}
    private record Result<T>(T value, Collection<Class<?>> owners) {}
}
