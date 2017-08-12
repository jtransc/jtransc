package java.util;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public interface Spliterator<T> {
	int ORDERED = 0x00000010;
	int DISTINCT = 0x00000001;
	int SORTED = 0x00000004;
	int SIZED = 0x00000040;
	int NONNULL = 0x00000100;
	int IMMUTABLE = 0x00000400;
	int CONCURRENT = 0x00001000;
	int SUBSIZED = 0x00004000;

	boolean tryAdvance(Consumer<? super T> action);

	default void forEachRemaining(Consumer<? super T> action) {
		do {
		} while (tryAdvance(action));
	}

	Spliterator<T> trySplit();

	long estimateSize();

	default long getExactSizeIfKnown() {
		return (characteristics() & SIZED) == 0 ? -1L : estimateSize();
	}

	int characteristics();

	default boolean hasCharacteristics(int characteristics) {
		return (characteristics() & characteristics) == characteristics;
	}

	default Comparator<? super T> getComparator() {
		throw new IllegalStateException();
	}

	interface OfPrimitive<T, T_CONS, T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>> extends Spliterator<T> {
		@Override
		T_SPLITR trySplit();

		@SuppressWarnings("overloads")
		boolean tryAdvance(T_CONS action);

		@SuppressWarnings("overloads")
		default void forEachRemaining(T_CONS action) {
			do {
			} while (tryAdvance(action));
		}
	}

	interface OfInt extends OfPrimitive<Integer, IntConsumer, OfInt> {

		@Override
		OfInt trySplit();

		@Override
		boolean tryAdvance(IntConsumer action);

		@Override
		default void forEachRemaining(IntConsumer action) {
			do {
			} while (tryAdvance(action));
		}

		@Override
		default boolean tryAdvance(Consumer<? super Integer> action) {
			throw new RuntimeException("Not implemented");

		}

		@Override
		default void forEachRemaining(Consumer<? super Integer> action) {
			throw new RuntimeException("Not implemented");

		}
	}

	interface OfLong extends OfPrimitive<Long, LongConsumer, OfLong> {

		@Override
		OfLong trySplit();

		@Override
		boolean tryAdvance(LongConsumer action);

		@Override
		default void forEachRemaining(LongConsumer action) {
			do {
			} while (tryAdvance(action));
		}

		@Override
		default boolean tryAdvance(Consumer<? super Long> action) {
			throw new RuntimeException("Not implemented");

		}

		@Override
		default void forEachRemaining(Consumer<? super Long> action) {
			throw new RuntimeException("Not implemented");

		}
	}

	interface OfDouble extends OfPrimitive<Double, DoubleConsumer, OfDouble> {

		@Override
		OfDouble trySplit();

		@Override
		boolean tryAdvance(DoubleConsumer action);

		@Override
		default void forEachRemaining(DoubleConsumer action) {
			do {
			} while (tryAdvance(action));
		}

		@Override
		default boolean tryAdvance(Consumer<? super Double> action) {
			throw new RuntimeException("Not implemented");
		}

		@Override
		default void forEachRemaining(Consumer<? super Double> action) {
			throw new RuntimeException("Not implemented");
		}
	}
}
