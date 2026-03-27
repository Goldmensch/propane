package logic.impl;

import dev.goldmensch.propane.Scope;
import dev.goldmensch.propane.property.MappingProperty;

import java.util.Map;

public class TestMappingProperty<K, V> extends MappingProperty<K, V> implements TestProperty<Map<K, V>> {
    public TestMappingProperty(String name, Source source, Scope scope, Class<K> keyType, Class<V> valueType, FallbackStrategy fallbackStrategy) {
        super(name, source, scope, keyType, valueType, fallbackStrategy);
    }

    @Override
    public Map<K, V> getScoped() {
        return TestIntrospection.scopedGet(this);
    }
}
