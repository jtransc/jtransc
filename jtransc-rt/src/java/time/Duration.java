package java.time;

import java.io.Serializable;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.List;

public final class Duration implements TemporalAmount, Comparable<Duration>, Serializable {
	public static final Duration ZERO = new Duration(0, 0);

	private final long seconds;
	private final int nanos;

	private Duration(long seconds, int nanos) {
		this.seconds = seconds;
		this.nanos = nanos;
	}

	native public static Duration ofDays(long days);

	native public static Duration ofHours(long hours);

	native public static Duration ofMinutes(long minutes);

	native public static Duration ofSeconds(long seconds);

	native public static Duration ofSeconds(long seconds, long nanoAdjustment);

	native public static Duration ofMillis(long millis);

	native public static Duration ofNanos(long nanos);

	native public static Duration of(long amount, TemporalUnit unit);

	native public static Duration from(TemporalAmount amount);

	native public static Duration parse(CharSequence text);

	native public static Duration between(Temporal startInclusive, Temporal endExclusive);

	native public long get(TemporalUnit unit);

	native public List<TemporalUnit> getUnits();

	native public boolean isZero();

	native public boolean isNegative();

	native public long getSeconds();

	native public int getNano();

	native public Duration withSeconds(long seconds);

	native public Duration withNanos(int nanoOfSecond);

	native public Duration plus(Duration duration);

	native public Duration plus(long amountToAdd, TemporalUnit unit);

	native public Duration plusDays(long daysToAdd);

	native public Duration plusHours(long hoursToAdd);

	native public Duration plusMinutes(long minutesToAdd);

	native public Duration plusSeconds(long secondsToAdd);

	native public Duration plusMillis(long millisToAdd);

	native public Duration plusNanos(long nanosToAdd);

	native public Duration minus(Duration duration);

	native public Duration minus(long amountToSubtract, TemporalUnit unit);

	native public Duration minusDays(long daysToSubtract);

	native public Duration minusHours(long hoursToSubtract);

	native public Duration minusMinutes(long minutesToSubtract);

	native public Duration minusSeconds(long secondsToSubtract);

	native public Duration minusMillis(long millisToSubtract);

	native public Duration minusNanos(long nanosToSubtract);

	native public Duration multipliedBy(long multiplicand);

	native public Duration dividedBy(long divisor);

	native public Duration negated();

	native public Duration abs();

	native public Temporal addTo(Temporal temporal);

	native public Temporal subtractFrom(Temporal temporal);

	native public long toDays();

	native public long toHours();

	native public long toMinutes();

	native public long toMillis();

	native public long toNanos();

	native public int compareTo(Duration otherDuration);

	native public boolean equals(Object otherDuration);

	native public int hashCode();

	native public String toString();
}
