package java.util.function;

@FunctionalInterface
public interface BiConsumer<T, U> {
	void accept(T t, U u);

	//default native BiConsumer<T, U> andThen(BiConsumer<? super T, ? super U> after);
}
