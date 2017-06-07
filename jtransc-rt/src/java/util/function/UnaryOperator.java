package java.util.function;

@FunctionalInterface
public interface UnaryOperator<T> extends Function<T, T> {
	static <T> UnaryOperator<T> identity() {
		return t -> t;
	}
}
