package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface Predicate<T> {
	boolean test(T t);

	default Predicate<T> and(Predicate<? super T> that) {
		return null;
	}
	default Predicate<T> negate() {
		return null;
	}
	default Predicate<T> or(Predicate<? super T> that) {
		return null;
	}
	static <T> Predicate<T> isEqual(Object targetRef) {
		return null;
	}

	/*
	default Predicate<T> and(Predicate<? super T> that) {
		Objects.requireNonNull(that);
		return (t) -> test(t) && that.test(t);
	}
	default Predicate<T> negate() {
		return (t) -> !test(t);
	}
	default Predicate<T> or(Predicate<? super T> that) {
		Objects.requireNonNull(that);
		return (t) -> test(t) || that.test(t);
	}
	static <T> Predicate<T> isEqual(Object targetRef) {
		return (targetRef != null) ? object -> targetRef.equals(object) : Objects::isNull;
	}
	*/
}
