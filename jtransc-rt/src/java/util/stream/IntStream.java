package java.util.stream;

import java.util.*;
import java.util.function.*;

public interface IntStream extends BaseStream<Integer, IntStream> {
	IntStream filter(IntPredicate predicate);

	IntStream map(IntUnaryOperator mapper);

	<U> Stream<U> mapToObj(IntFunction<? extends U> mapper);

	LongStream mapToLong(IntToLongFunction mapper);

	DoubleStream mapToDouble(IntToDoubleFunction mapper);

	IntStream flatMap(IntFunction<? extends IntStream> mapper);

	IntStream distinct();

	IntStream sorted();

	IntStream peek(IntConsumer action);

	IntStream limit(long maxSize);

	IntStream skip(long n);

	void forEach(IntConsumer action);

	void forEachOrdered(IntConsumer action);

	int[] toArray();

	int reduce(int identity, IntBinaryOperator op);

	OptionalInt reduce(IntBinaryOperator op);

	<R> R collect(Supplier<R> supplier, ObjIntConsumer<R> accumulator, BiConsumer<R, R> combiner);

	int sum();

	OptionalInt min();

	OptionalInt max();

	long count();

	OptionalDouble average();

	IntSummaryStatistics summaryStatistics();

	boolean anyMatch(IntPredicate predicate);

	boolean allMatch(IntPredicate predicate);

	boolean noneMatch(IntPredicate predicate);

	OptionalInt findFirst();

	OptionalInt findAny();

	LongStream asLongStream();

	DoubleStream asDoubleStream();

	Stream<Integer> boxed();

	@Override
	IntStream sequential();

	@Override
	IntStream parallel();

	@Override
	PrimitiveIterator.OfInt iterator();

	@Override
	Spliterator.OfInt spliterator();

	static Builder builder() {
		throw new RuntimeException("Not implemented");
	}

	static IntStream empty() {
		throw new RuntimeException("Not implemented");
	}

	static IntStream of(int t) {
		throw new RuntimeException("Not implemented");
	}

	static IntStream of(int... values) {
		throw new RuntimeException("Not implemented");
	}

	static IntStream iterate(final int seed, final IntUnaryOperator f) {
		throw new RuntimeException("Not implemented");
	}

	static IntStream generate(IntSupplier s) {
		throw new RuntimeException("Not implemented");
	}

	static IntStream range(int startInclusive, int endExclusive) {
		throw new RuntimeException("Not implemented");
	}

	static IntStream rangeClosed(int startInclusive, int endInclusive) {
		throw new RuntimeException("Not implemented");
	}

	static IntStream concat(IntStream a, IntStream b) {
		throw new RuntimeException("Not implemented");
	}

	interface Builder extends IntConsumer {
		@Override
		void accept(int t);

		default Builder add(int t) {
			accept(t);
			return this;
		}

		IntStream build();
	}
}
