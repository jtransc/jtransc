package java.util;

import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public final class OptionalLong {
	native public static OptionalLong empty();

	native public static OptionalLong of(long value);

	native public long getAsLong();

	native public boolean isPresent();

	native public void ifPresent(LongConsumer consumer);

	native public long orElse(long other);

	native public long orElseGet(LongSupplier other);

	native public <X extends Throwable> long orElseThrow(Supplier<X> exceptionSupplier) throws X;

	native public boolean equals(Object obj);

	native public int hashCode();

	native public String toString();
}
