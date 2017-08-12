package java.util.function;

@FunctionalInterface
public interface LongConsumer {
	void accept(long value);

	default LongConsumer andThen(LongConsumer after) {
		return (long t) -> {
			accept(t);
			after.accept(t);
		};
	}
}
