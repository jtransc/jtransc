package java.util.stream;

import java.util.*;
import java.util.function.*;

public interface LongStream extends BaseStream<Long, LongStream> {
	LongStream filter(LongPredicate predicate);

	LongStream map(LongUnaryOperator mapper);

	<U> Stream<U> mapToObj(LongFunction<? extends U> mapper);

	IntStream mapToInt(LongToIntFunction mapper);

	DoubleStream mapToDouble(LongToDoubleFunction mapper);

	LongStream flatMap(LongFunction<? extends LongStream> mapper);

	LongStream distinct();

	LongStream sorted();

	LongStream peek(LongConsumer action);

	LongStream limit(long maxSize);

	LongStream skip(long n);

	void forEach(LongConsumer action);

	void forEachOrdered(LongConsumer action);

	long[] toArray();

	long reduce(long identity, LongBinaryOperator op);

	OptionalLong reduce(LongBinaryOperator op);

	<R> R collect(Supplier<R> supplier, ObjLongConsumer<R> accumulator, BiConsumer<R, R> combiner);

	long sum();

	OptionalLong min();

	OptionalLong max();

	long count();

	OptionalDouble average();

	LongSummaryStatistics summaryStatistics();

	boolean anyMatch(LongPredicate predicate);

	boolean allMatch(LongPredicate predicate);

	boolean noneMatch(LongPredicate predicate);

	OptionalLong findFirst();

	OptionalLong findAny();

	DoubleStream asDoubleStream();

	Stream<Long> boxed();

	@Override
	LongStream sequential();

	@Override
	LongStream parallel();

	@Override
	PrimitiveIterator.OfLong iterator();

	@Override
	Spliterator.OfLong spliterator();

	static Builder builder() {
		throw new RuntimeException("Not implemented");
	}

	static LongStream empty() {
		throw new RuntimeException("Not implemented");
	}

	static LongStream of(long t) {
		throw new RuntimeException("Not implemented");
	}

	static LongStream of(long... values) {
		throw new RuntimeException("Not implemented");
	}

	static LongStream iterate(final long seed, final LongUnaryOperator f) {
		throw new RuntimeException("Not implemented");
	}

	static LongStream generate(LongSupplier s) {
		throw new RuntimeException("Not implemented");
	}

	static LongStream range(long startInclusive, final long endExclusive) {
		throw new RuntimeException("Not implemented");
	}

	static LongStream rangeClosed(long startInclusive, final long endInclusive) {
		throw new RuntimeException("Not implemented");
	}

	static LongStream concat(LongStream a, LongStream b) {
		throw new RuntimeException("Not implemented");
	}

	interface Builder extends LongConsumer {
		@Override
		void accept(long t);

		default Builder add(long t) {
			accept(t);
			return this;
		}

		LongStream build();
	}
}
