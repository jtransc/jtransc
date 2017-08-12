package java.util.stream;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Collector<T, A, R> {
	Supplier<A> supplier();

	BiConsumer<A, T> accumulator();

	BinaryOperator<A> combiner();

	Function<A, R> finisher();

	Set<Characteristics> characteristics();

	static <T, R> Collector<T, R, R> of(Supplier<R> supplier, BiConsumer<R, T> accumulator, BinaryOperator<R> combiner, Characteristics... characteristics) {
		throw new RuntimeException("Not implemented");
	}

	static <T, A, R> Collector<T, A, R> of(Supplier<A> supplier, BiConsumer<A, T> accumulator, BinaryOperator<A> combiner, Function<A, R> finisher, Characteristics... characteristics) {
		throw new RuntimeException("Not implemented");
	}

	enum Characteristics {
		CONCURRENT,
		UNORDERED,
		IDENTITY_FINISH
	}
}
