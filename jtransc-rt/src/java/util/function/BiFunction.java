package java.util.function;

@FunctionalInterface
public interface BiFunction<T, U, R> {
	R apply(T left, U right);

	default <V> BiFunction<T, U, V> andThen(Function<? super R, ? extends V> then) {
		return (T left, U right) -> then.apply(apply(left, right));
	}
}
