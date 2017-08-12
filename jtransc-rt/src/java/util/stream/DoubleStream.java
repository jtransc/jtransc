package java.util.stream;

import java.util.DoubleSummaryStatistics;
import java.util.OptionalDouble;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.function.*;

public interface DoubleStream extends BaseStream<Double, DoubleStream> {
	DoubleStream filter(DoublePredicate predicate);

	DoubleStream map(DoubleUnaryOperator mapper);

	<U> Stream<U> mapToObj(DoubleFunction<? extends U> mapper);

	IntStream mapToInt(DoubleToIntFunction mapper);

	LongStream mapToLong(DoubleToLongFunction mapper);

	DoubleStream flatMap(DoubleFunction<? extends DoubleStream> mapper);

	DoubleStream distinct();

	DoubleStream sorted();

	DoubleStream peek(DoubleConsumer action);

	DoubleStream limit(long maxSize);

	DoubleStream skip(long n);

	void forEach(DoubleConsumer action);

	void forEachOrdered(DoubleConsumer action);

	double[] toArray();

	double reduce(double identity, DoubleBinaryOperator op);

	OptionalDouble reduce(DoubleBinaryOperator op);

	<R> R collect(Supplier<R> supplier, ObjDoubleConsumer<R> accumulator, BiConsumer<R, R> combiner);

	double sum();

	OptionalDouble min();

	OptionalDouble max();

	long count();

	OptionalDouble average();

	DoubleSummaryStatistics summaryStatistics();

	boolean anyMatch(DoublePredicate predicate);

	boolean allMatch(DoublePredicate predicate);

	boolean noneMatch(DoublePredicate predicate);

	OptionalDouble findFirst();

	OptionalDouble findAny();

	Stream<Double> boxed();

	@Override
	DoubleStream sequential();

	@Override
	DoubleStream parallel();

	@Override
	PrimitiveIterator.OfDouble iterator();

	@Override
	Spliterator.OfDouble spliterator();

	static Builder builder() {
		throw new RuntimeException("Not implemented");
	}

	static DoubleStream empty() {
		throw new RuntimeException("Not implemented");
	}

	static DoubleStream of(double t) {
		throw new RuntimeException("Not implemented");
	}

	static DoubleStream of(double... values) {
		throw new RuntimeException("Not implemented");
	}

	static DoubleStream iterate(final double seed, final DoubleUnaryOperator f) {
		throw new RuntimeException("Not implemented");
	}

	static DoubleStream generate(DoubleSupplier s) {
		throw new RuntimeException("Not implemented");
	}

	static DoubleStream concat(DoubleStream a, DoubleStream b) {
		throw new RuntimeException("Not implemented");
	}

	interface Builder extends DoubleConsumer {
		@Override
		void accept(double t);

		default Builder add(double t) {
			accept(t);
			return this;
		}

		DoubleStream build();
	}
}
