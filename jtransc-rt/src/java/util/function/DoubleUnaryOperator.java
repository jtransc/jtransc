package java.util.function;

@FunctionalInterface
public interface DoubleUnaryOperator {
	double applyAsDouble(double operand);

	default DoubleUnaryOperator compose(DoubleUnaryOperator before) {
		return (double v) -> applyAsDouble(before.applyAsDouble(v));
	}

	default DoubleUnaryOperator andThen(DoubleUnaryOperator after) {
		return (double t) -> after.applyAsDouble(applyAsDouble(t));
	}

	static DoubleUnaryOperator identity() {
		return t -> t;
	}
}
