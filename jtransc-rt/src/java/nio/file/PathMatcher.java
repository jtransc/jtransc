package java.nio.file;

@FunctionalInterface
public interface PathMatcher {
	boolean matches(Path path);
}
