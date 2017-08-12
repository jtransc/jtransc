package java.time;

public abstract class Clock {
	protected Clock() {
	}

	native public static Clock systemUTC();

	native public static Clock systemDefaultZone();

	native public static Clock system(ZoneId zone);

	native public static Clock tickSeconds(ZoneId zone);

	native public static Clock tickMinutes(ZoneId zone);

	native public static Clock tick(Clock baseClock, Duration tickDuration);

	native public static Clock fixed(Instant fixedInstant, ZoneId zone);

	native public static Clock offset(Clock baseClock, Duration offsetDuration);

	public abstract ZoneId getZone();

	public abstract Clock withZone(ZoneId zone);

	public long millis() {
		return instant().toEpochMilli();
	}

	public abstract Instant instant();

	native public boolean equals(Object obj);

	native public int hashCode();
}