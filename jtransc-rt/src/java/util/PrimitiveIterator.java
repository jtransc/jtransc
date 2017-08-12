package java.util;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public interface PrimitiveIterator<T, T_CONS> extends Iterator<T> {
	@SuppressWarnings("overloads")
	void forEachRemaining(T_CONS action);

	interface OfInt extends PrimitiveIterator<Integer, IntConsumer> {
		int nextInt();

		default void forEachRemaining(IntConsumer action) {
			while (hasNext()) action.accept(nextInt());
		}

		@Override
		default Integer next() {
			throw new RuntimeException("Not implemented");
		}

		@Override
		default void forEachRemaining(Consumer<? super Integer> action) {
			throw new RuntimeException("Not implemented");
		}
	}

	interface OfLong extends PrimitiveIterator<Long, LongConsumer> {
		long nextLong();

		default void forEachRemaining(LongConsumer action) {
			while (hasNext())
				action.accept(nextLong());
		}

		@Override
		default Long next() {
			throw new RuntimeException("Not implemented");
		}

		@Override
		default void forEachRemaining(Consumer<? super Long> action) {
			throw new RuntimeException("Not implemented");
		}
	}

	interface OfDouble extends PrimitiveIterator<Double, DoubleConsumer> {
		double nextDouble();

		default void forEachRemaining(DoubleConsumer action) {
			while (hasNext())
				action.accept(nextDouble());
		}

		@Override
		default Double next() {
			throw new RuntimeException("Not implemented");
		}

		@Override
		default void forEachRemaining(Consumer<? super Double> action) {
			throw new RuntimeException("Not implemented");
		}
	}
}
