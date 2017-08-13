/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package java.time;

import java.io.Serializable;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Era;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;

public final class LocalDate implements Temporal, TemporalAdjuster, ChronoLocalDate, Serializable {
	public static final LocalDate MIN = LocalDate.of(Year.MIN_VALUE, 1, 1);
	public static final LocalDate MAX = LocalDate.of(Year.MAX_VALUE, 12, 31);

	private final int year;
	private final short month;
	private final short day;

	public LocalDate(int year, short month, short day) {
		this.year = year;
		this.month = month;
		this.day = day;
	}

	native public static LocalDate now();

	native public static LocalDate now(ZoneId zone);

	native public static LocalDate now(Clock clock);

	public static LocalDate of(int year, Month month, int dayOfMonth) {
		return new LocalDate(year, (short) month.getValue(), (short) dayOfMonth);
	}

	public static LocalDate of(int year, int month, int dayOfMonth) {
		return new LocalDate(year, (short) month, (short) dayOfMonth);
	}

	native public static LocalDate ofYearDay(int year, int dayOfYear);

	native public static LocalDate ofEpochDay(long epochDay);

	native public static LocalDate from(TemporalAccessor temporal);

	native public static LocalDate parse(CharSequence text);

	native public static LocalDate parse(CharSequence text, DateTimeFormatter formatter);

	native public boolean isSupported(TemporalField field);

	native public boolean isSupported(TemporalUnit unit);

	native public ValueRange range(TemporalField field);

	native public int get(TemporalField field);

	native public long getLong(TemporalField field);

	native public IsoChronology getChronology();

	native public Era getEra();

	native public int getYear();

	native public int getMonthValue();

	native public Month getMonth();

	native public int getDayOfMonth();

	native public int getDayOfYear();

	native public DayOfWeek getDayOfWeek();

	native public boolean isLeapYear();

	native public int lengthOfMonth();

	native public int lengthOfYear();

	native public LocalDate with(TemporalAdjuster adjuster);

	native public LocalDate with(TemporalField field, long newValue);

	native public LocalDate withYear(int year);

	native public LocalDate withMonth(int month);

	native public LocalDate withDayOfMonth(int dayOfMonth);

	native public LocalDate withDayOfYear(int dayOfYear);

	native public LocalDate plus(TemporalAmount amountToAdd);

	native public LocalDate plus(long amountToAdd, TemporalUnit unit);

	native public LocalDate plusYears(long yearsToAdd);

	native public LocalDate plusMonths(long monthsToAdd);

	native public LocalDate plusWeeks(long weeksToAdd);

	native public LocalDate plusDays(long daysToAdd);

	native public LocalDate minus(TemporalAmount amountToSubtract);

	native public LocalDate minus(long amountToSubtract, TemporalUnit unit);

	native public LocalDate minusYears(long yearsToSubtract);

	native public LocalDate minusMonths(long monthsToSubtract);

	native public LocalDate minusWeeks(long weeksToSubtract);

	native public LocalDate minusDays(long daysToSubtract);

	native public <R> R query(TemporalQuery<R> query);

	native public Temporal adjustInto(Temporal temporal);

	native public long until(Temporal endExclusive, TemporalUnit unit);

	native public Period until(ChronoLocalDate endDateExclusive);

	native public String format(DateTimeFormatter formatter);

	native public LocalDateTime atTime(LocalTime time);

	native public LocalDateTime atTime(int hour, int minute);

	native public LocalDateTime atTime(int hour, int minute, int second);

	native public LocalDateTime atTime(int hour, int minute, int second, int nanoOfSecond);

	native public OffsetDateTime atTime(OffsetTime time);

	native public LocalDateTime atStartOfDay();

	native public ZonedDateTime atStartOfDay(ZoneId zone);

	native public long toEpochDay();

	native public int compareTo(ChronoLocalDate other);

	native int compareTo0(LocalDate otherDate);

	native public boolean isAfter(ChronoLocalDate other);

	native public boolean isBefore(ChronoLocalDate other);

	native public boolean isEqual(ChronoLocalDate other);

	native public boolean equals(Object obj);

	native public int hashCode();

	native public String toString();
}
