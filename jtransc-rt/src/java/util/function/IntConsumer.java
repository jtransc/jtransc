package java.util.function;

@FunctionalInterface
public interface IntConsumer {
	void accept(int value);

	default IntConsumer andThen(IntConsumer after) {
		return (int t) -> {
			accept(t);
			after.accept(t);
		};
	}
}