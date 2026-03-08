package dev.goldmensch.propane.spec;

public class SkeletonMethodException extends RuntimeException {
    public SkeletonMethodException() {
        String caller = StackWalker.getInstance().walk(frames -> frames
                .skip(1)
                .map(frame -> frame.getClassName() + "#" + frame.getMethodName())
                .findFirst()
                .orElseThrow());

        super("The method: %s can only be called on specifications of this skeleton class".formatted(caller));
    }
}
