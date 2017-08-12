package java.time.temporal;

import java.time.Duration;

public enum ChronoUnit implements TemporalUnit {
	NANOS("Nanos", Duration.ofNanos(1)),
	MICROS("Micros", Duration.ofNanos(1000)),
	MILLIS("Millis", Duration.ofNanos(1000_000)),
	SECONDS("Seconds", Duration.ofSeconds(1)),
	MINUTES("Minutes", Duration.ofSeconds(60)),
	HOURS("Hours", Duration.ofSeconds(3600)),
	HALF_DAYS("HalfDays", Duration.ofSeconds(43200)),
	DAYS("Days", Duration.ofSeconds(86400)),
	WEEKS("Weeks", Duration.ofSeconds(7 * 86400L)),
	MONTHS("Months", Duration.ofSeconds(31556952L / 12)),
	YEARS("Years", Duration.ofSeconds(31556952L)),
	DECADES("Decades", Duration.ofSeconds(31556952L * 10L)),
	CENTURIES("Centuries", Duration.ofSeconds(31556952L * 100L)),
	MILLENNIA("Millennia", Duration.ofSeconds(31556952L * 1000L)),
	ERAS("Eras", Duration.ofSeconds(31556952L * 1000_000_000L)),
	FOREVER("Forever", Duration.ofSeconds(Long.MAX_VALUE, 999_999_999));

	private final String name;
	private final Duration duration;

	ChronoUnit(String name, Duration estimatedDuration) {
		this.name = name;
		this.duration = estimatedDuration;
	}

	native public Duration getDuration();

	native public boolean isDurationEstimated();

	native public boolean isDateBased();

	native public boolean isTimeBased();

	native public boolean isSupportedBy(Temporal temporal);

	native public <R extends Temporal> R addTo(R temporal, long amount);

	native public long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive);

	native public String toString();
}