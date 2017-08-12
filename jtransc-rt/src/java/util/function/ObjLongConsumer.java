package java.util.function;

@FunctionalInterface
public interface ObjLongConsumer<T> {
	void accept(T t, long value);
}
