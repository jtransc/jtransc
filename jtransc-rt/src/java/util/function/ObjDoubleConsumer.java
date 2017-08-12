package java.util.function;

@FunctionalInterface
public interface ObjDoubleConsumer<T> {
	void accept(T t, double value);
}
