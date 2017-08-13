package java.util;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public final class OptionalDouble {
	native public static OptionalDouble empty();

	native public static OptionalDouble of(double value);

	native public double getAsDouble();

	native public boolean isPresent();

	native public void ifPresent(DoubleConsumer consumer);

	native public double orElse(double other);

	native public double orElseGet(DoubleSupplier other);

	native public <X extends Throwable> double orElseThrow(Supplier<X> exceptionSupplier) throws X;

	native public boolean equals(Object obj);

	native public int hashCode();

	native public String toString();
}
