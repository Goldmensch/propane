package logic.impl;

import dev.goldmensch.propane.IntrospectionImpl;
import dev.goldmensch.propane.PropertyProvider;
import dev.goldmensch.propane.internal.exposed.Properties;
import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.property.SpecificProperty;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public class TestIntrospectionImpl extends IntrospectionImpl<TestIntrospectionImpl, TestIntrospection, TestIntrospectionImpl.TestBuilder, Property.Scope> implements TestIntrospection {

    public static final ScopedValue<TestIntrospectionImpl> INTROSPECTION = ScopedValue.newInstance();

    private TestIntrospectionImpl(Property.Scope scope, Properties<TestIntrospection> properties, TestIntrospectionImpl parent) {
        super(scope, properties, parent);
    }

    // called by create(Scope)
    private TestIntrospectionImpl(Property.Scope scope) {
        super(scope);
    }

    public static TestBuilder create(Property.Scope scope) {
        return new TestIntrospectionImpl(scope).createChild(scope);
    }

    public <T> T get(TestProperty<T> specific) {
        return super.get(specific);
    }

    @Override
    public TestBuilder createChild(Property.Scope scope) {
        return this.new TestBuilder(scope);
    }

    // TestIntrospectionImpl.TestBuilder doesn't work, because of... java generics
    public class TestBuilder extends IntrospectionImpl<TestIntrospectionImpl, TestIntrospection, TestBuilder, Property.Scope>.Builder {
        private TestBuilder(Property.Scope scope) {
            super(scope);
        }

        public <T> TestBuilder addFallback(TestProperty<T> property, Function<TestIntrospection, T> supplier) {
            return add(new TestPropertyProvider<>(property, PropertyProvider.Priority.FALLBACK, caller(), supplier));
        }

        public <T> TestBuilder addBuilder(TestProperty<T> property, Function<TestIntrospection, T> supplier) {
            return add(new TestPropertyProvider<>(property, PropertyProvider.Priority.BUILDER, caller(), supplier));
        }

        public <T> TestBuilder addFallback(TestProperty<T> property, Class<?> owner, Function<TestIntrospection, T> supplier) {
            return add(new TestPropertyProvider<>(property, PropertyProvider.Priority.FALLBACK, owner, supplier));
        }

        public <T> TestBuilder addBuilder(TestProperty<T> property, Class<?> owner, Function<TestIntrospection, T> supplier) {
            return add(new TestPropertyProvider<>(property, PropertyProvider.Priority.BUILDER, owner, supplier));
        }

        @Override
        protected TestIntrospectionImpl newInstance() {
            return new TestIntrospectionImpl(scope, properties, TestIntrospectionImpl.this);
        }
    }
}
