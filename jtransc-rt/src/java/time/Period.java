package java.time;

import java.io.Serializable;
import java.time.chrono.ChronoPeriod;
import java.time.chrono.IsoChronology;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.List;

public final class Period implements ChronoPeriod, Serializable {
	public static final Period ZERO = new Period(0, 0, 0);

	private final int years;
	private final int months;
	private final int days;

	private Period(int years, int months, int days) {
		this.years = years;
		this.months = months;
		this.days = days;
	}

	native public static Period ofYears(int years);

	native public static Period ofMonths(int months);

	native public static Period ofWeeks(int weeks);

	native public static Period ofDays(int days);

	native public static Period of(int years, int months, int days);

	native public static Period from(TemporalAmount amount);

	native public static Period parse(CharSequence text);

	native public static Period between(LocalDate startDateInclusive, LocalDate endDateExclusive);

	native public long get(TemporalUnit unit);

	native public List<TemporalUnit> getUnits();

	native public IsoChronology getChronology();

	native public boolean isZero();

	native public boolean isNegative();

	native public int getYears();

	native public int getMonths();

	native public int getDays();

	native public Period withYears(int years);

	native public Period withMonths(int months);

	native public Period withDays(int days);

	native public Period plus(TemporalAmount amountToAdd);

	native public Period plusYears(long yearsToAdd);

	native public Period plusMonths(long monthsToAdd);

	native public Period plusDays(long daysToAdd);

	native public Period minus(TemporalAmount amountToSubtract);

	native public Period minusYears(long yearsToSubtract);

	native public Period minusMonths(long monthsToSubtract);

	native public Period minusDays(long daysToSubtract);

	native public Period multipliedBy(int scalar);

	native public Period negated();

	native public Period normalized();

	native public long toTotalMonths();

	native public Temporal addTo(Temporal temporal);

	native public Temporal subtractFrom(Temporal temporal);

	native public boolean equals(Object obj);

	native public int hashCode();

	native public String toString();
}
