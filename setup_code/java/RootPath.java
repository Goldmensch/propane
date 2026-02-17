import java.nio.file.Files;
import java.nio.file.Path;

public class RootPath {
    public static Path ROOT;

    static {
        Path current = Path.of(".").toAbsolutePath();
        while (current != null) {
            boolean isRoot = Files.exists(current.resolve("README.md"));
            if (isRoot) {
                ROOT = current;
                break;
            }

            current = current.getParent();
        }
    }
}
