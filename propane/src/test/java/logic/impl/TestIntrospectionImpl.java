package logic.impl;

import dev.goldmensch.propane.IntrospectionImpl;
import dev.goldmensch.propane.internal.exposed.Properties;
import dev.goldmensch.propane.property.Property;

public class TestIntrospectionImpl extends IntrospectionImpl<TestIntrospectionImpl, TestIntrospection, TestIntrospectionImpl.TestBuilder> implements TestIntrospection {

    public static final ScopedValue<TestIntrospectionImpl> INTROSPECTION = ScopedValue.newInstance();

    private static final TestIntrospectionImpl EMPTY = new TestIntrospectionImpl();

    private TestIntrospectionImpl(Property.Scope scope, Properties<TestIntrospection> properties, TestIntrospectionImpl parent) {
        super(scope, properties, parent);
    }

    // called by create(Scope)
    private TestIntrospectionImpl() {
        super();
    }

    public static TestBuilder create(Property.Scope scope) {
        return EMPTY.createChild(scope);
    }

    public <T> T get(TestProperty<T> specific) {
        return super.get(specific);
    }

    @Override
    public TestBuilder createChild(Property.Scope scope) {
        return this.new TestBuilder(scope);
    }

    // TestIntrospectionImpl.TestBuilder doesn't work, because of... java generics
    public class TestBuilder extends IntrospectionImpl<TestIntrospectionImpl, TestIntrospection, TestBuilder>.Builder {
        private TestBuilder(Property.Scope scope) {
            super(scope);
        }
        @Override
        protected TestIntrospectionImpl newInstance() {
            return new TestIntrospectionImpl(scope, properties, TestIntrospectionImpl.this);
        }
    }




}
