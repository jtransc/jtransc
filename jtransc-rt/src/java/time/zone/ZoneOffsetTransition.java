package java.time.zone;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public final class ZoneOffsetTransition implements Comparable<ZoneOffsetTransition>, Serializable {
	native public static ZoneOffsetTransition of(LocalDateTime transition, ZoneOffset offsetBefore, ZoneOffset offsetAfter);

	native public Instant getInstant();

	native public long toEpochSecond();

	native public LocalDateTime getDateTimeBefore();

	native public LocalDateTime getDateTimeAfter();

	native public ZoneOffset getOffsetBefore();

	native public ZoneOffset getOffsetAfter();

	native public Duration getDuration();

	native public boolean isGap();

	native public boolean isOverlap();

	native public boolean isValidOffset(ZoneOffset offset);

	native List<ZoneOffset> getValidOffsets();

	native public int compareTo(ZoneOffsetTransition transition);

	native public boolean equals(Object other);

	native public int hashCode();

	native public String toString();
}
