package logic;

import dev.goldmensch.propane.IntrospectionImpl;
import dev.goldmensch.propane.property.Property;
import dev.goldmensch.propane.spec.SkeletonMethodException;
import org.junit.Assert;
import org.junit.Test;

public class CannotCallSkeletonTest {

    @Test
    public void fail_on_skeleton_call() {
        Property.Scope scope = new Property.Scope() {

            @Override
            public int priority() {
                return 100;
            }
        };

        Assert.assertThrows(SkeletonMethodException.class, () -> IntrospectionImpl.create(scope));
    }
}
