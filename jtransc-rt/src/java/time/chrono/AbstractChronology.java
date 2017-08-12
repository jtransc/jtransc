package java.time.chrono;

import java.time.format.ResolverStyle;
import java.time.temporal.TemporalField;
import java.util.Map;

public abstract class AbstractChronology implements Chronology {
	protected AbstractChronology() {
	}

	native public ChronoLocalDate resolveDate(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle);

	native public int compareTo(Chronology other);

	native public boolean equals(Object obj);

	native public int hashCode();

	public String toString() {
		return getId();
	}
}
