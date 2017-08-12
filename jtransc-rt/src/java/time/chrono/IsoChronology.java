package java.time.chrono;

import java.io.Serializable;
import java.time.*;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.ValueRange;
import java.util.List;
import java.util.Map;

public final class IsoChronology extends AbstractChronology implements Serializable {
	public static final IsoChronology INSTANCE = new IsoChronology();

	private IsoChronology() {
	}

	native public String getId();

	native public String getCalendarType();

	native public LocalDate date(Era era, int yearOfEra, int month, int dayOfMonth);

	native public LocalDate date(int prolepticYear, int month, int dayOfMonth);

	native public LocalDate dateYearDay(Era era, int yearOfEra, int dayOfYear);

	native public LocalDate dateYearDay(int prolepticYear, int dayOfYear);

	native public LocalDate dateEpochDay(long epochDay);

	native public LocalDate date(TemporalAccessor temporal);

	native public LocalDateTime localDateTime(TemporalAccessor temporal);

	native public ZonedDateTime zonedDateTime(TemporalAccessor temporal);

	native public ZonedDateTime zonedDateTime(Instant instant, ZoneId zone);

	native public LocalDate dateNow();

	native public LocalDate dateNow(ZoneId zone);

	native public LocalDate dateNow(Clock clock);

	native public boolean isLeapYear(long prolepticYear);

	native public int prolepticYear(Era era, int yearOfEra);

	native public IsoEra eraOf(int eraValue);

	native public List<Era> eras();

	native public LocalDate resolveDate(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle);

	native void resolveProlepticMonth(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle);

	native LocalDate resolveYearOfEra(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle);

	native LocalDate resolveYMD(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle);

	native public ValueRange range(ChronoField field);

	native public Period period(int years, int months, int days);
}
