package spec;

import dev.goldmensch.propane.PropertyProvider;
import spec.bar.BarIntrospection;
import spec.bar.BarProperty;
import spec.bar.BarPropertyProvider;
import spec.bar.BarScope;

public class Test {

    @org.junit.Test
    public void foo() {
        BarIntrospection introspection = BarIntrospection.create(BarScope.ROOT)
                .add(new BarPropertyProvider<>(BarProperty.FOO_SINGLE, PropertyProvider.Priority.FALLBACK, Test.class, _ -> "test"))
                .build();

        System.out.println(introspection.get(BarProperty.FOO_SINGLE));
    }
}
