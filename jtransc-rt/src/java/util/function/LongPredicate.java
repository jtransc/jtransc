package java.util.function;

@FunctionalInterface
public interface LongPredicate {
	boolean test(long value);

	default LongPredicate and(LongPredicate other) {
		return (value) -> test(value) && other.test(value);
	}

	default LongPredicate negate() {
		return (value) -> !test(value);
	}

	default LongPredicate or(LongPredicate other) {
		return (value) -> test(value) || other.test(value);
	}
}
