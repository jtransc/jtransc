package java.util.function;

@FunctionalInterface
public interface DoublePredicate {
	boolean test(double value);

	default DoublePredicate and(DoublePredicate other) {
		return (value) -> test(value) && other.test(value);
	}

	default DoublePredicate negate() {
		return (value) -> !test(value);
	}

	default DoublePredicate or(DoublePredicate other) {
		return (value) -> test(value) || other.test(value);
	}
}
