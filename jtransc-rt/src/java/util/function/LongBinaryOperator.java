package java.util.function;

@FunctionalInterface
public interface LongBinaryOperator {
	long applyAsLong(long left, long right);
}
