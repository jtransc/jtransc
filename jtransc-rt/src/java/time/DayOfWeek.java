package java.time;

import java.time.format.TextStyle;
import java.time.temporal.*;
import java.util.Locale;

public enum DayOfWeek implements TemporalAccessor, TemporalAdjuster {
	MONDAY,
	TUESDAY,
	WEDNESDAY,
	THURSDAY,
	FRIDAY,
	SATURDAY,
	SUNDAY;

	native public static DayOfWeek of(int dayOfWeek);

	native public static DayOfWeek from(TemporalAccessor temporal);

	native public int getValue();

	native public String getDisplayName(TextStyle style, Locale locale);

	native public boolean isSupported(TemporalField field);

	native public ValueRange range(TemporalField field);

	native public int get(TemporalField field);

	native public long getLong(TemporalField field);

	native public DayOfWeek plus(long days);

	native public DayOfWeek minus(long days);

	native public <R> R query(TemporalQuery<R> query);

	native public Temporal adjustInto(Temporal temporal);
}
