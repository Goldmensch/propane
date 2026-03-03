package logic.impl;

import dev.goldmensch.propane.Introspection;
import dev.goldmensch.propane.internal.exposed.Properties;
import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.property.SpecificProperty;

public class TestIntrospection extends Introspection<TestProperty<?>>  {

    private static final TestIntrospection EMPTY = new TestIntrospection();

    private TestIntrospection(Property.Scope scope, Properties<TestProperty<?>, TestIntrospection> properties, TestIntrospection parent) {
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

    public class TestBuilder extends Builder<TestBuilder, TestIntrospection, TestPropertyProvider<?>> {
        private TestBuilder(Property.Scope scope) {
            super(scope);
        }

        @Override
        protected TestIntrospection newInstance() {
            return new TestIntrospection(scope, properties, TestIntrospection.this);
        }
    }




}
