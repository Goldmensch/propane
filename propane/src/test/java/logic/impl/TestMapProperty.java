package logic.impl;

import dev.goldmensch.propane.property.MapProperty;

import java.util.Collection;
import java.util.Map;

public class TestMapProperty<K, V> extends MapProperty<K, V> implements TestProperty<Map<K, V>> {
    public TestMapProperty(String name, Source source, Scope scope, Class<K> keyType, Class<V> valueType, FallbackBehaviour fallbackBehaviour) {
        super(name, source, scope, keyType, valueType, fallbackBehaviour);
    }
}
