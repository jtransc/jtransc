package java.util.concurrent;

public enum TimeUnit {
	NANOSECONDS(CST.NANO),
	MICROSECONDS(CST.MICRO),
	MILLISECONDS(CST.MILLIS),
	SECONDS(CST.SECOND),
	MINUTES(CST.MINUTE),
	HOURS(CST.HOURS),
	DAYS(CST.DAYS);

	private long value;

	static private class CST {
		static private final long NANO = 1L;
		static private final long MICRO = 1000L;
		static private final long MILLIS = 1000000L;
		static private final long SECOND = 1000000000L;
		static private final long MINUTE = 60000000000L;
		static private final long HOURS = 3600000000000L;
		static private final long DAYS = 86400000000000L;
	}

	TimeUnit(long value) {
		this.value = value;
	}

	public long convert(long duration, TimeUnit sourceUnit) {
		return _convert(duration, sourceUnit.value);
	}

	private long _convert(long duration, long constant) {
		return duration * this.value / constant;
	}

	public long toNanos(long duration) {
		return _convert(duration, CST.NANO);
	}

	public long toMicros(long duration) {
		return _convert(duration, CST.MICRO);
	}

	public long toMillis(long duration) {
		return _convert(duration, CST.MILLIS);
	}

	public long toSeconds(long duration) {
		return _convert(duration, CST.SECOND);
	}

	public long toMinutes(long duration) {
		return _convert(duration, CST.MINUTE);
	}

	public long toHours(long duration) {
		return _convert(duration, CST.HOURS);
	}

	public long toDays(long duration) {
		return _convert(duration, CST.DAYS);
	}

	int excessNanos(long d, long m) {
		return 0;
	}

	public void timedWait(Object obj, long timeout) {
	}

	public void timedJoin(Thread thread, long timeout) throws InterruptedException {
	}

	public void sleep(long timeout) throws InterruptedException {
	}
}
