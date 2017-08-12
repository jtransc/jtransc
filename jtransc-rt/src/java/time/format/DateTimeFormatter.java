package java.time.format;

import java.text.Format;
import java.text.ParsePosition;
import java.time.Period;
import java.time.ZoneId;
import java.time.chrono.Chronology;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQuery;
import java.util.Locale;
import java.util.Set;

public final class DateTimeFormatter {
	public static final DateTimeFormatter ISO_LOCAL_DATE = new DateTimeFormatter();
	public static final DateTimeFormatter ISO_OFFSET_DATE = new DateTimeFormatter();
	public static final DateTimeFormatter ISO_DATE = new DateTimeFormatter();
	public static final DateTimeFormatter ISO_LOCAL_TIME = new DateTimeFormatter();
	public static final DateTimeFormatter ISO_OFFSET_TIME = new DateTimeFormatter();
	public static final DateTimeFormatter ISO_TIME = new DateTimeFormatter();
	public static final DateTimeFormatter ISO_LOCAL_DATE_TIME = new DateTimeFormatter();
	public static final DateTimeFormatter ISO_OFFSET_DATE_TIME = new DateTimeFormatter();
	public static final DateTimeFormatter ISO_ZONED_DATE_TIME = new DateTimeFormatter();
	public static final DateTimeFormatter ISO_DATE_TIME = new DateTimeFormatter();
	public static final DateTimeFormatter ISO_ORDINAL_DATE = new DateTimeFormatter();
	public static final DateTimeFormatter ISO_WEEK_DATE = new DateTimeFormatter();
	public static final DateTimeFormatter ISO_INSTANT = new DateTimeFormatter();
	public static final DateTimeFormatter BASIC_ISO_DATE = new DateTimeFormatter();
	public static final DateTimeFormatter RFC_1123_DATE_TIME = new DateTimeFormatter();

	private DateTimeFormatter() {
	}

	native public static DateTimeFormatter ofPattern(String pattern);

	native public static DateTimeFormatter ofPattern(String pattern, Locale locale);

	native public static DateTimeFormatter ofLocalizedDate(FormatStyle dateStyle);

	native public static DateTimeFormatter ofLocalizedTime(FormatStyle timeStyle);

	native public static DateTimeFormatter ofLocalizedDateTime(FormatStyle dateTimeStyle);

	native public static DateTimeFormatter ofLocalizedDateTime(FormatStyle dateStyle, FormatStyle timeStyle);

	native public static final TemporalQuery<Period> parsedExcessDays();

	native public static final TemporalQuery<Boolean> parsedLeapSecond();

	native public Locale getLocale();

	native public DateTimeFormatter withLocale(Locale locale);

	native public DecimalStyle getDecimalStyle();

	native public DateTimeFormatter withDecimalStyle(DecimalStyle decimalStyle);

	native public Chronology getChronology();

	native public DateTimeFormatter withChronology(Chronology chrono);

	native public ZoneId getZone();

	native public DateTimeFormatter withZone(ZoneId zone);

	native public ResolverStyle getResolverStyle();

	native public DateTimeFormatter withResolverStyle(ResolverStyle resolverStyle);

	native public Set<TemporalField> getResolverFields();

	native public DateTimeFormatter withResolverFields(TemporalField... resolverFields);

	native public DateTimeFormatter withResolverFields(Set<TemporalField> resolverFields);

	native public String format(TemporalAccessor temporal);

	native public void formatTo(TemporalAccessor temporal, Appendable appendable);

	native public TemporalAccessor parse(CharSequence text);

	native public TemporalAccessor parse(CharSequence text, ParsePosition position);

	native public <T> T parse(CharSequence text, TemporalQuery<T> query);

	native public TemporalAccessor parseBest(CharSequence text, TemporalQuery<?>... queries);

	native public TemporalAccessor parseUnresolved(CharSequence text, ParsePosition position);

	native public Format toFormat();

	native public Format toFormat(TemporalQuery<?> parseQuery);

	native public String toString();
}
