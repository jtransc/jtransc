package java.util;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class OptionalInt {
	private static OptionalInt INSTANCE_EMPTY;

	private final boolean isPresent;
	private final int value;

	private OptionalInt(boolean isPresent, int value) {
		this.isPresent = isPresent;
		this.value = value;
	}

	public static OptionalInt empty() {
		if (INSTANCE_EMPTY == null) {
			INSTANCE_EMPTY = new OptionalInt(false, 0);
		}
		return INSTANCE_EMPTY;
	}

	public static OptionalInt of(int value) {
		return new OptionalInt(true, value);
	}

	native public int getAsInt();

	native public boolean isPresent();

	native public void ifPresent(IntConsumer consumer);

	native public int orElse(int other);

	native public int orElseGet(IntSupplier other);

	native public <X extends Throwable> int orElseThrow(Supplier<X> exceptionSupplier) throws X;

	native public boolean equals(Object obj);

	native public int hashCode();

	native public String toString();
}
