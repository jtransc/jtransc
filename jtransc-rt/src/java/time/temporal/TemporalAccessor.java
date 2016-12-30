package java.time.temporal;

public interface TemporalAccessor {
	boolean isSupported(TemporalField field);

	long getLong(TemporalField field);

	default ValueRange range(TemporalField field) {
		if (field instanceof ChronoField) {
			return field.range();
		} else {
			return field.rangeRefinedBy(this);
		}
	}

	default int get(TemporalField field) {
		ValueRange range = range(field);
		long value = getLong(field);
		return (int) value;
	}

	default <R> R query(TemporalQuery<R> query) {
		return query.queryFrom(this);
	}

}
