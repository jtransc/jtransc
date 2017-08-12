package java.util.function;

@FunctionalInterface
public interface LongFunction<R> {
	R apply(long value);
}
