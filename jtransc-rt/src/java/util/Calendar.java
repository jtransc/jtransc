/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.util;

import java.io.Serializable;
import java.time.Instant;

abstract public class Calendar implements Serializable, Cloneable, Comparable<Calendar> {
	public final static int ERA = 0;
	public final static int YEAR = 1;
	public final static int MONTH = 2;
	public final static int WEEK_OF_YEAR = 3;
	public final static int WEEK_OF_MONTH = 4;
	public final static int DATE = 5;
	public final static int DAY_OF_MONTH = 5;
	public final static int DAY_OF_YEAR = 6;
	public final static int DAY_OF_WEEK = 7;
	public final static int DAY_OF_WEEK_IN_MONTH = 8;
	public final static int AM_PM = 9;
	public final static int HOUR = 10;
	public final static int HOUR_OF_DAY = 11;
	public final static int MINUTE = 12;
	public final static int SECOND = 13;
	public final static int MILLISECOND = 14;
	public final static int ZONE_OFFSET = 15;
	public final static int DST_OFFSET = 16;
	public final static int FIELD_COUNT = 17;
	public final static int SUNDAY = 1;
	public final static int MONDAY = 2;
	public final static int TUESDAY = 3;
	public final static int WEDNESDAY = 4;
	public final static int THURSDAY = 5;
	public final static int FRIDAY = 6;
	public final static int SATURDAY = 7;
	public final static int JANUARY = 0;
	public final static int FEBRUARY = 1;
	public final static int MARCH = 2;
	public final static int APRIL = 3;
	public final static int MAY = 4;
	public final static int JUNE = 5;
	public final static int JULY = 6;
	public final static int AUGUST = 7;
	public final static int SEPTEMBER = 8;
	public final static int OCTOBER = 9;
	public final static int NOVEMBER = 10;
	public final static int DECEMBER = 11;
	public final static int UNDECIMBER = 12;

	public final static int AM = 0;
	public final static int PM = 1;

	public static final int ALL_STYLES = 0;

	static final int STANDALONE_MASK = 0x8000;

	public static final int SHORT = 1;
	public static final int LONG = 2;
	public static final int NARROW_FORMAT = 4;
	public static final int NARROW_STANDALONE = NARROW_FORMAT | STANDALONE_MASK;
	public static final int SHORT_FORMAT = 1;
	public static final int LONG_FORMAT = 2;
	public static final int SHORT_STANDALONE = SHORT | STANDALONE_MASK;
	public static final int LONG_STANDALONE = LONG | STANDALONE_MASK;

	@SuppressWarnings("ProtectedField")
	protected int fields[];

	@SuppressWarnings("ProtectedField")
	protected boolean isSet[];

	@SuppressWarnings("ProtectedField")
	protected long time;

	@SuppressWarnings("ProtectedField")
	protected boolean isTimeSet;

	@SuppressWarnings("ProtectedField")
	protected boolean areFieldsSet;

	@SuppressWarnings("PointlessBitwiseExpression")
	final static int ERA_MASK = (1 << ERA);
	final static int YEAR_MASK = (1 << YEAR);
	final static int MONTH_MASK = (1 << MONTH);
	final static int WEEK_OF_YEAR_MASK = (1 << WEEK_OF_YEAR);
	final static int WEEK_OF_MONTH_MASK = (1 << WEEK_OF_MONTH);
	final static int DAY_OF_MONTH_MASK = (1 << DAY_OF_MONTH);
	final static int DATE_MASK = DAY_OF_MONTH_MASK;
	final static int DAY_OF_YEAR_MASK = (1 << DAY_OF_YEAR);
	final static int DAY_OF_WEEK_MASK = (1 << DAY_OF_WEEK);
	final static int DAY_OF_WEEK_IN_MONTH_MASK = (1 << DAY_OF_WEEK_IN_MONTH);
	final static int AM_PM_MASK = (1 << AM_PM);
	final static int HOUR_MASK = (1 << HOUR);
	final static int HOUR_OF_DAY_MASK = (1 << HOUR_OF_DAY);
	final static int MINUTE_MASK = (1 << MINUTE);
	final static int SECOND_MASK = (1 << SECOND);
	final static int MILLISECOND_MASK = (1 << MILLISECOND);
	final static int ZONE_OFFSET_MASK = (1 << ZONE_OFFSET);
	final static int DST_OFFSET_MASK = (1 << DST_OFFSET);

	public static class Builder {
		public Builder() {
		}

		native public Builder setInstant(long instant);

		native public Builder setInstant(Date instant);

		native public Builder set(int field, int value);

		native public Builder setFields(int... fieldValuePairs);

		native public Builder setDate(int year, int month, int dayOfMonth);

		native public Builder setTimeOfDay(int hourOfDay, int minute, int second);

		native public Builder setTimeOfDay(int hourOfDay, int minute, int second, int millis);

		native public Builder setWeekDate(int weekYear, int weekOfYear, int dayOfWeek);

		native public Builder setTimeZone(TimeZone zone);

		native public Builder setLenient(boolean lenient);

		native public Builder setCalendarType(String type);

		native public Builder setLocale(Locale locale);

		native public Builder setWeekDefinition(int firstDayOfWeek, int minimalDaysInFirstWeek);

		native public Calendar build();
	}

	protected Calendar() {
	}

	protected Calendar(TimeZone zone, Locale aLocale) {
	}

	native public static Calendar getInstance();

	native public static Calendar getInstance(TimeZone zone);

	native public static Calendar getInstance(Locale aLocale);

	native public static Calendar getInstance(TimeZone zone, Locale aLocale);

	native public static synchronized Locale[] getAvailableLocales();

	protected abstract void computeTime();

	protected abstract void computeFields();

	native public final Date getTime();

	native public final void setTime(Date date);

	native public long getTimeInMillis();

	native public void setTimeInMillis(long millis);

	native public int get(int field);

	native protected final int internalGet(int field);

	native public void set(int field, int value);

	native public final void set(int year, int month, int date);

	native public final void set(int year, int month, int date, int hourOfDay, int minute);

	native public final void set(int year, int month, int date, int hourOfDay, int minute, int second);

	native public final void clear();

	native public final void clear(int field);

	native public final boolean isSet(int field);

	native public String getDisplayName(int field, int style, Locale locale);

	native public Map<String, Integer> getDisplayNames(int field, int style, Locale locale);

	native protected void complete();

	native public static Set<String> getAvailableCalendarTypes();

	native public String getCalendarType();

	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	@Override
	native public boolean equals(Object obj);

	@Override
	native public int hashCode();

	native public boolean before(Object when);

	native public boolean after(Object when);

	@Override
	native public int compareTo(Calendar anotherCalendar);

	abstract public void add(int field, int amount);

	abstract public void roll(int field, boolean up);

	native public void roll(int field, int amount);


	native public void setTimeZone(TimeZone value);

	native public TimeZone getTimeZone();

	native TimeZone getZone();

	native public void setLenient(boolean lenient);

	native public boolean isLenient();

	native public void setFirstDayOfWeek(int value);

	native public int getFirstDayOfWeek();

	native public void setMinimalDaysInFirstWeek(int value);

	native public int getMinimalDaysInFirstWeek();

	native public boolean isWeekDateSupported();

	native public int getWeekYear();

	native public void setWeekDate(int weekYear, int weekOfYear, int dayOfWeek);

	native public int getWeeksInWeekYear();

	abstract public int getMinimum(int field);

	abstract public int getMaximum(int field);

	abstract public int getGreatestMinimum(int field);

	abstract public int getLeastMaximum(int field);

	native public int getActualMinimum(int field);

	native public int getActualMaximum(int field);

	@Override
	native public Object clone();

	@Override
	native public String toString();

	//native public final Instant toInstant();
}
