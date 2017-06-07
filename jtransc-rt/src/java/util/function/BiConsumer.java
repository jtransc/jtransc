package java.util.function;

@FunctionalInterface
public interface BiConsumer<T, U> {
	void accept(T t, U u);

	default BiConsumer<T, U> andThen(BiConsumer<? super T, ? super U> after) {
		return (l, r) -> {
			accept(l, r);
			after.accept(l, r);
		};
	}
}
