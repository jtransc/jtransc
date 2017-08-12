package java.util.function;

@FunctionalInterface
public interface IntPredicate {
	boolean test(int value);

	default IntPredicate and(IntPredicate other) {
		return (value) -> test(value) && other.test(value);
	}

	default IntPredicate negate() {
		return (value) -> !test(value);
	}

	default IntPredicate or(IntPredicate other) {
		return (value) -> test(value) || other.test(value);
	}
}
