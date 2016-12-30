package java.time;

import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.time.temporal.*;
import java.time.zone.ZoneRules;

import static java.time.LocalTime.SECONDS_PER_HOUR;

public final class ZoneOffset extends ZoneId implements TemporalAccessor, TemporalAdjuster, Comparable<ZoneOffset>, Serializable {
	public static final ZoneOffset UTC = ZoneOffset.ofTotalSeconds(0);
	public static final ZoneOffset MIN = ZoneOffset.ofTotalSeconds(-18 * SECONDS_PER_HOUR);
	public static final ZoneOffset MAX = ZoneOffset.ofTotalSeconds(18 * SECONDS_PER_HOUR);

	private ZoneOffset(int totalSeconds) {
		super();
	}

	native public static ZoneOffset of(String offsetId);

	native public static ZoneOffset ofHours(int hours);

	native public static ZoneOffset ofHoursMinutes(int hours, int minutes);

	native public static ZoneOffset ofHoursMinutesSeconds(int hours, int minutes, int seconds);

	native public static ZoneOffset from(TemporalAccessor temporal);

	native public static ZoneOffset ofTotalSeconds(int totalSeconds);

	native public int getTotalSeconds();

	native public String getId();

	native public ZoneRules getRules();

	native public boolean isSupported(TemporalField field);

	native public ValueRange range(TemporalField field);

	native public int get(TemporalField field);

	native public long getLong(TemporalField field);

	native public <R> R query(TemporalQuery<R> query);

	native public Temporal adjustInto(Temporal temporal);

	native void write(DataOutput out) throws IOException;

	native public int compareTo(ZoneOffset other);

	native public boolean equals(Object obj);

	native public int hashCode();

	native public String toString();
}
