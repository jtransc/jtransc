package java.util.function;

import java.util.Comparator;

@FunctionalInterface
public interface BinaryOperator<T> extends BiFunction<T, T, T> {
	static <T> BinaryOperator<T> minBy(Comparator<? super T> comparator) {
		return (a, b) -> comparator.compare(a, b) <= 0 ? a : b;
	}

	static <T> BinaryOperator<T> maxBy(Comparator<? super T> comparator) {
		return (a, b) -> comparator.compare(a, b) >= 0 ? a : b;
	}
}