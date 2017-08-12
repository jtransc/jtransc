package java.util.function;

@FunctionalInterface
public interface IntUnaryOperator {
	int applyAsInt(int operand);

	default IntUnaryOperator compose(IntUnaryOperator before) {
		return (int v) -> applyAsInt(before.applyAsInt(v));
	}

	default IntUnaryOperator andThen(IntUnaryOperator after) {
		return (int t) -> after.applyAsInt(applyAsInt(t));
	}

	static IntUnaryOperator identity() {
		return t -> t;
	}
}
