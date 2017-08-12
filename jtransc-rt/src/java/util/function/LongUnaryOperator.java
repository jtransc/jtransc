package java.util.function;

@FunctionalInterface
public interface LongUnaryOperator {
	long applyAsLong(long operand);

	default LongUnaryOperator compose(LongUnaryOperator before) {
		return (long v) -> applyAsLong(before.applyAsLong(v));
	}

	default LongUnaryOperator andThen(LongUnaryOperator after) {
		return (long t) -> after.applyAsLong(applyAsLong(t));
	}

	static LongUnaryOperator identity() {
		return t -> t;
	}
}
