package spec;

import dev.goldmensch.propane.PropertyProvider;
import org.junit.Assert;
import org.junit.Test;
import spec.bar.BarIntrospection;
import spec.bar.BarScope;

import java.util.List;
import java.util.Map;

public class GenerationTest {

    @Test
    public void bar_implicit_prefix() {
        BarIntrospection introspection = BarIntrospection.create(BarScope.ROOT)
                .build();

        Assert.assertNotNull(introspection);
    }

    @Test
    public void single_property() {
        GenTestIntrospection build = GenTestIntrospection.create(GenTestScope.ROOT)
                .add(new GenTestPropertyProvider<>(GenTestProperty.FOO_SINGLE, PropertyProvider.Priority.FALLBACK, GenerationTest.class, _ -> "hi"))
                .build();

        Assert.assertEquals("hi", build.get(GenTestProperty.FOO_SINGLE));
    }

    @Test
    public void enumeration_test() {
        GenTestIntrospection build = GenTestIntrospection.create(GenTestScope.ROOT)
                .add(new GenTestPropertyProvider<>(GenTestProperty.FOO_ENUMERATION, PropertyProvider.Priority.FALLBACK, GenerationTest.class, _ -> List.of("hi")))
                .build();

        Assert.assertEquals(List.of("hi"), build.get(GenTestProperty.FOO_ENUMERATION));
    }

    @Test
    public void mapping_property() {
        GenTestIntrospection build = GenTestIntrospection.create(GenTestScope.ROOT)
                .add(new GenTestPropertyProvider<>(GenTestProperty.FOO_MAPPING, PropertyProvider.Priority.FALLBACK, GenerationTest.class, _ -> Map.of("hello", "world")))
                .build();

        Assert.assertEquals(Map.of("hello", "world"), build.get(GenTestProperty.FOO_MAPPING));
    }

}
