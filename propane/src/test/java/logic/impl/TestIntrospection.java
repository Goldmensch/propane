package logic.impl;

import dev.goldmensch.propane.IntrospectionImpl;
import dev.goldmensch.propane.internal.exposed.Properties;
import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.property.SpecificProperty;

public class TestIntrospection extends IntrospectionImpl<TestIntrospection, TestIntrospection.TestBuilder, TestPropertyProvider<?>> {

    private static final TestIntrospection EMPTY = new TestIntrospection();

    private TestIntrospection(Property.Scope scope, Properties<TestIntrospection> properties, TestIntrospection parent) {
        super(scope, properties, parent);
    }

    // called by create(Scope)
    private TestIntrospection() {
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

    // TestIntrospection.TestBuilder doesn't work, because of... java generics
    public class TestBuilder extends IntrospectionImpl<TestIntrospection, TestBuilder, TestPropertyProvider<?>>.Builder {
        private TestBuilder(Property.Scope scope) {
            super(scope);
        }
        @Override
        protected TestIntrospection newInstance() {
            return new TestIntrospection(scope, properties, TestIntrospection.this);
        }
    }




}
