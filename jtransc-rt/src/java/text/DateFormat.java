package java.text;

import java.io.InvalidObjectException;
import java.util.*;

public abstract class DateFormat extends Format {
	protected Calendar calendar;
	protected NumberFormat numberFormat;
	public final static int ERA_FIELD = 0;
	public final static int YEAR_FIELD = 1;
	public final static int MONTH_FIELD = 2;
	public final static int DATE_FIELD = 3;
	public final static int HOUR_OF_DAY1_FIELD = 4;
	public final static int HOUR_OF_DAY0_FIELD = 5;
	public final static int MINUTE_FIELD = 6;
	public final static int SECOND_FIELD = 7;
	public final static int MILLISECOND_FIELD = 8;
	public final static int DAY_OF_WEEK_FIELD = 9;
	public final static int DAY_OF_YEAR_FIELD = 10;
	public final static int DAY_OF_WEEK_IN_MONTH_FIELD = 11;
	public final static int WEEK_OF_YEAR_FIELD = 12;
	public final static int WEEK_OF_MONTH_FIELD = 13;
	public final static int AM_PM_FIELD = 14;
	public final static int HOUR1_FIELD = 15;
	public final static int HOUR0_FIELD = 16;
	public final static int TIMEZONE_FIELD = 17;

	public final StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition fieldPosition) {
		if (obj instanceof Date) return format((Date) obj, toAppendTo, fieldPosition);
		if (obj instanceof Number) return format(new Date(((Number) obj).longValue()), toAppendTo, fieldPosition);
		throw new IllegalArgumentException("Cannot format given Object as a Date");
	}

	public abstract StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition);

	public final String format(Date date) {
		return format(date, new StringBuffer(), new FieldPosition(0)).toString();
	}

	public Date parse(String source) throws ParseException {
		ParsePosition pos = new ParsePosition(0);
		Date result = parse(source, pos);
		if (pos.index == 0) throw new ParseException("Unparseable date: \"" + source + "\"", pos.errorIndex);
		return result;
	}

	public abstract Date parse(String source, ParsePosition pos);

	public Object parseObject(String source, ParsePosition pos) {
		return parse(source, pos);
	}

	public static final int FULL = 0;
	public static final int LONG = 1;
	public static final int MEDIUM = 2;
	public static final int SHORT = 3;
	public static final int DEFAULT = MEDIUM;

	native public final static DateFormat getTimeInstance();

	native public final static DateFormat getTimeInstance(int style);

	native public final static DateFormat getTimeInstance(int style, Locale aLocale);

	native public final static DateFormat getDateInstance();

	native public final static DateFormat getDateInstance(int style);

	native public final static DateFormat getDateInstance(int style, Locale aLocale);

	native public final static DateFormat getDateTimeInstance();

	native public final static DateFormat getDateTimeInstance(int dateStyle, int timeStyle);

	native public final static DateFormat getDateTimeInstance(int dateStyle, int timeStyle, Locale aLocale);

	public final static DateFormat getInstance() {
		return getDateTimeInstance(SHORT, SHORT);
	}

	native public static Locale[] getAvailableLocales();

	public void setCalendar(Calendar newCalendar) {
		this.calendar = newCalendar;
	}

	public Calendar getCalendar() {
		return calendar;
	}

	public void setTimeZone(TimeZone zone) {
		calendar.setTimeZone(zone);
	}

	public TimeZone getTimeZone() {
		return calendar.getTimeZone();
	}

	public void setNumberFormat(NumberFormat newNumberFormat) {
		this.numberFormat = newNumberFormat;
	}

	public NumberFormat getNumberFormat() {
		return numberFormat;
	}

	public void setLenient(boolean lenient) {
		calendar.setLenient(lenient);
	}

	public boolean isLenient() {
		return calendar.isLenient();
	}

	public int hashCode() {
		return numberFormat.hashCode();
		// just enough fields for a reasonable distribution
	}

	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		DateFormat other = (DateFormat) obj;
		return (// calendar.equivalentTo(other.calendar) // THIS API DOESN'T EXIST YET!
			calendar.getFirstDayOfWeek() == other.calendar.getFirstDayOfWeek() &&
				calendar.getMinimalDaysInFirstWeek() == other.calendar.getMinimalDaysInFirstWeek() &&
				calendar.isLenient() == other.calendar.isLenient() &&
				calendar.getTimeZone().equals(other.calendar.getTimeZone()) &&
				numberFormat.equals(other.numberFormat));
	}

	public Object clone() {
		DateFormat other = (DateFormat) super.clone();
		other.calendar = (Calendar) calendar.clone();
		other.numberFormat = (NumberFormat) numberFormat.clone();
		return other;
	}

	protected DateFormat() {
	}

	public static class Field extends Format.Field {
		private static final Map<String, Field> instanceMap = new HashMap<>(18);
		private static final Field[] calendarToFieldMapping = new Field[Calendar.FIELD_COUNT];

		private int calendarField;

		public static Field ofCalendarField(int calendarField) {
			if (calendarField < 0 || calendarField >=
				calendarToFieldMapping.length) {
				throw new IllegalArgumentException("Unknown Calendar constant " + calendarField);
			}
			return calendarToFieldMapping[calendarField];
		}

		protected Field(String name, int calendarField) {
			super(name);
			this.calendarField = calendarField;
			if (this.getClass() == DateFormat.Field.class) {
				instanceMap.put(name, this);
				if (calendarField >= 0) {
					// assert(calendarField < Calendar.FIELD_COUNT);
					calendarToFieldMapping[calendarField] = this;
				}
			}
		}

		public int getCalendarField() {
			return calendarField;
		}

		@Override
		protected Object readResolve() throws InvalidObjectException {
			if (this.getClass() != DateFormat.Field.class) {
				throw new InvalidObjectException("subclass didn't correctly implement readResolve");
			}

			Object instance = instanceMap.get(getName());
			if (instance != null) {
				return instance;
			} else {
				throw new InvalidObjectException("unknown attribute name");
			}
		}

		public final static Field ERA = new Field("era", Calendar.ERA);
		public final static Field YEAR = new Field("year", Calendar.YEAR);
		public final static Field MONTH = new Field("month", Calendar.MONTH);
		public final static Field DAY_OF_MONTH = new Field("day of month", Calendar.DAY_OF_MONTH);
		public final static Field HOUR_OF_DAY1 = new Field("hour of day 1", -1);
		public final static Field HOUR_OF_DAY0 = new Field("hour of day", Calendar.HOUR_OF_DAY);
		public final static Field MINUTE = new Field("minute", Calendar.MINUTE);
		public final static Field SECOND = new Field("second", Calendar.SECOND);
		public final static Field MILLISECOND = new Field("millisecond", Calendar.MILLISECOND);
		public final static Field DAY_OF_WEEK = new Field("day of week", Calendar.DAY_OF_WEEK);
		public final static Field DAY_OF_YEAR = new Field("day of year", Calendar.DAY_OF_YEAR);
		public final static Field DAY_OF_WEEK_IN_MONTH = new Field("day of week in month", Calendar.DAY_OF_WEEK_IN_MONTH);
		public final static Field WEEK_OF_YEAR = new Field("week of year", Calendar.WEEK_OF_YEAR);
		public final static Field WEEK_OF_MONTH = new Field("week of month", Calendar.WEEK_OF_MONTH);
		public final static Field AM_PM = new Field("am pm", Calendar.AM_PM);
		public final static Field HOUR1 = new Field("hour 1", -1);
		public final static Field HOUR0 = new Field("hour", Calendar.HOUR);
		public final static Field TIME_ZONE = new Field("time zone", -1);
	}
}
