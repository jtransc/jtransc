package java.util.function;

@FunctionalInterface
public interface ToDoubleFunction<T> {
	double applyAsDouble(T value);
}
