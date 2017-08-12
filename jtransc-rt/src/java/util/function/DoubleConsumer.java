package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface DoubleConsumer {
	void accept(double value);

	default DoubleConsumer andThen(DoubleConsumer after) {
		return (double t) -> { accept(t); after.accept(t); };
	}
}
