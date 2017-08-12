package java.time.zone;

import java.io.Serializable;
import java.time.*;

public final class ZoneOffsetTransitionRule implements Serializable {
	native public static ZoneOffsetTransitionRule of(
		Month month,
		int dayOfMonthIndicator,
		DayOfWeek dayOfWeek,
		LocalTime time,
		boolean timeEndOfDay,
		TimeDefinition timeDefnition,
		ZoneOffset standardOffset,
		ZoneOffset offsetBefore,
		ZoneOffset offsetAfter
	);


	native public Month getMonth();

	native public int getDayOfMonthIndicator();

	native public DayOfWeek getDayOfWeek();

	native public LocalTime getLocalTime();

	native public boolean isMidnightEndOfDay();

	native public TimeDefinition getTimeDefinition();

	native public ZoneOffset getStandardOffset();

	native public ZoneOffset getOffsetBefore();

	native public ZoneOffset getOffsetAfter();

	native public ZoneOffsetTransition createTransition(int year);

	native public boolean equals(Object otherRule);

	native public int hashCode();

	native public String toString();

	public enum TimeDefinition {
		UTC,
		WALL,
		STANDARD;

		native public LocalDateTime createDateTime(LocalDateTime dateTime, ZoneOffset standardOffset, ZoneOffset wallOffset);
	}
}
