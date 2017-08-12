package java.time.temporal;

import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Map;

public interface TemporalField {
	TemporalUnit getBaseUnit();

	TemporalUnit getRangeUnit();

	ValueRange range();

	boolean isDateBased();

	boolean isTimeBased();

	boolean isSupportedBy(TemporalAccessor temporal);

	ValueRange rangeRefinedBy(TemporalAccessor temporal);

	long getFrom(TemporalAccessor temporal);

	<R extends Temporal> R adjustInto(R temporal, long newValue);

	@Override
	String toString();

	default String getDisplayName(Locale locale) {
		return toString();
	}

	default TemporalAccessor resolve(Map<TemporalField, Long> fieldValues, TemporalAccessor partialTemporal, ResolverStyle resolverStyle) {
		return null;
	}
}
