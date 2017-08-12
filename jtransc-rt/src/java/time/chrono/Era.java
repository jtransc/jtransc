package java.time.chrono;

import java.time.format.TextStyle;
import java.time.temporal.*;
import java.util.Locale;

public interface Era extends TemporalAccessor, TemporalAdjuster {
	int getValue();

	default boolean isSupported(TemporalField field) {
		throw new RuntimeException("Not implemented");
	}

	default ValueRange range(TemporalField field) {
		throw new RuntimeException("Not implemented");
	}

	default int get(TemporalField field) {
		throw new RuntimeException("Not implemented");
	}

	default long getLong(TemporalField field) {
		throw new RuntimeException("Not implemented");
	}

	default <R> R query(TemporalQuery<R> query) {
		throw new RuntimeException("Not implemented");
	}

	default Temporal adjustInto(Temporal temporal) {
		throw new RuntimeException("Not implemented");
	}

	default String getDisplayName(TextStyle style, Locale locale) {
		throw new RuntimeException("Not implemented");
	}
}
