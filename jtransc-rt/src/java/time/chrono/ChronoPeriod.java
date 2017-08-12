package java.time.chrono;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.List;

public interface ChronoPeriod extends TemporalAmount {
	static ChronoPeriod between(ChronoLocalDate startDateInclusive, ChronoLocalDate endDateExclusive) {
		return startDateInclusive.until(endDateExclusive);
	}

	@Override
	long get(TemporalUnit unit);

	@Override
	List<TemporalUnit> getUnits();

	Chronology getChronology();

	default boolean isZero() {
		throw new RuntimeException("Not implemented");
	}

	default boolean isNegative() {
		throw new RuntimeException("Not implemented");
	}

	ChronoPeriod plus(TemporalAmount amountToAdd);

	ChronoPeriod minus(TemporalAmount amountToSubtract);

	ChronoPeriod multipliedBy(int scalar);

	default ChronoPeriod negated() {
		return multipliedBy(-1);
	}

	ChronoPeriod normalized();

	Temporal addTo(Temporal temporal);

	Temporal subtractFrom(Temporal temporal);

	boolean equals(Object obj);

	int hashCode();

	String toString();
}
