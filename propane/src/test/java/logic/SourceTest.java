package logic;

import dev.goldmensch.propane.Scope;
import dev.goldmensch.propane.property.Property;
import logic.impl.TestProperty;
import logic.impl.TestSingletonProperty;

public class SourceTest {

    private enum Scopes implements Scope {
        ROOT;

        @Override
        public int priority() {
            return ordinal();
        }
    }

    private class Properties {
        static TestProperty<String> BUILDER_PROP = new TestSingletonProperty<>("BUILDER_PROP", Property.Source.EXTENSION, Scopes.ROOT, String.class);
        static TestProperty<String> PROVIDED_PROP = new TestSingletonProperty<>("PROVIDED_PROP", Property.Source.EXTENSION, Scopes.ROOT, String.class);
        static TestProperty<String> EXTENSION_PROP = new TestSingletonProperty<>("EXTENSION_PROP", Property.Source.EXTENSION, Scopes.ROOT, String.class);
    }


}
