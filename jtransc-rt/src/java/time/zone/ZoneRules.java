package java.time.zone;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public final class ZoneRules implements Serializable {
	native public static ZoneRules of(
		ZoneOffset baseStandardOffset,
		ZoneOffset baseWallOffset,
		List<ZoneOffsetTransition> standardOffsetTransitionList,
		List<ZoneOffsetTransition> transitionList,
		List<ZoneOffsetTransitionRule> lastRules
	);

	native public static ZoneRules of(ZoneOffset offset);

	native public boolean isFixedOffset();

	native public ZoneOffset getOffset(Instant instant);

	native public ZoneOffset getOffset(LocalDateTime localDateTime);

	native public List<ZoneOffset> getValidOffsets(LocalDateTime localDateTime);

	native public ZoneOffsetTransition getTransition(LocalDateTime localDateTime);

	native public ZoneOffset getStandardOffset(Instant instant);

	native public Duration getDaylightSavings(Instant instant);

	native public boolean isDaylightSavings(Instant instant);

	native public boolean isValidOffset(LocalDateTime localDateTime, ZoneOffset offset);

	native public ZoneOffsetTransition nextTransition(Instant instant);

	native public ZoneOffsetTransition previousTransition(Instant instant);

	native public List<ZoneOffsetTransition> getTransitions();

	native public List<ZoneOffsetTransitionRule> getTransitionRules();

	native public boolean equals(Object otherRules);

	native public int hashCode();

	native public String toString();
}
