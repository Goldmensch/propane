package logic;

import dev.goldmensch.propane.IntrospectionImpl;
import dev.goldmensch.propane.Scope;
import dev.goldmensch.propane.spec.SkeletonMethodException;
import org.junit.Assert;
import org.junit.Test;

public class CannotCallSkeletonTest {

    @Test
    public void fail_on_skeleton_call() {
        Scope scope = new Scope() {

            @Override
            public int priority() {
                return 100;
            }
        };

        Assert.assertThrows(SkeletonMethodException.class, () -> IntrospectionImpl.create(scope));
    }
}
