package java.util.function;

@FunctionalInterface
public interface Consumer<T> {
	void accept(T value);

	default Consumer<T> andThen(Consumer<? super T> then) {
		return (value) -> {
			this.accept(value);
			then.accept(value);
		};
	}
}
