package java.util.function;

@FunctionalInterface
public interface ToLongFunction<T> {
	long applyAsLong(T value);
}
