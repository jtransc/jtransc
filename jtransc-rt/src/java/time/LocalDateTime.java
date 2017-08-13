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
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;

public final class LocalDateTime implements Temporal, TemporalAdjuster, ChronoLocalDateTime<LocalDate>, Serializable {
	public static final LocalDateTime MIN = new LocalDateTime(LocalDate.MIN, LocalTime.MIN);
	public static final LocalDateTime MAX = new LocalDateTime(LocalDate.MAX, LocalTime.MAX);

	private final LocalDate date;
	private final LocalTime time;

	private LocalDateTime(LocalDate date, LocalTime time) {
		this.date = date;
		this.time = time;
	}

	native public static LocalDateTime now();

	native public static LocalDateTime now(ZoneId zone);

	native public static LocalDateTime now(Clock clock);

	native public static LocalDateTime of(int year, Month month, int dayOfMonth, int hour, int minute);

	native public static LocalDateTime of(int year, Month month, int dayOfMonth, int hour, int minute, int second);

	native public static LocalDateTime of(int year, Month month, int dayOfMonth, int hour, int minute, int second, int nanoOfSecond);

	native public static LocalDateTime of(int year, int month, int dayOfMonth, int hour, int minute);

	native public static LocalDateTime of(int year, int month, int dayOfMonth, int hour, int minute, int second);

	native public static LocalDateTime of(int year, int month, int dayOfMonth, int hour, int minute, int second, int nanoOfSecond);

	native public static LocalDateTime of(LocalDate date, LocalTime time);

	native public static LocalDateTime ofInstant(Instant instant, ZoneId zone);

	native public static LocalDateTime ofEpochSecond(long epochSecond, int nanoOfSecond, ZoneOffset offset);

	native public static LocalDateTime from(TemporalAccessor temporal);

	native public static LocalDateTime parse(CharSequence text);

	native public static LocalDateTime parse(CharSequence text, DateTimeFormatter formatter);

	native public boolean isSupported(TemporalField field);

	native public boolean isSupported(TemporalUnit unit);

	native public ValueRange range(TemporalField field);

	native public int get(TemporalField field);

	native public long getLong(TemporalField field);

	native public LocalDate toLocalDate();

	native public int getYear();

	native public int getMonthValue();

	native public Month getMonth();

	native public int getDayOfMonth();

	native public int getDayOfYear();

	native public DayOfWeek getDayOfWeek();

	native public LocalTime toLocalTime();

	native public int getHour();

	native public int getMinute();

	native public int getSecond();

	native public int getNano();

	native public LocalDateTime with(TemporalAdjuster adjuster);

	native public LocalDateTime with(TemporalField field, long newValue);

	native public LocalDateTime withYear(int year);

	native public LocalDateTime withMonth(int month);

	native public LocalDateTime withDayOfMonth(int dayOfMonth);

	native public LocalDateTime withDayOfYear(int dayOfYear);

	native public LocalDateTime withHour(int hour);

	native public LocalDateTime withMinute(int minute);

	native public LocalDateTime withSecond(int second);

	native public LocalDateTime withNano(int nanoOfSecond);

	native public LocalDateTime truncatedTo(TemporalUnit unit);

	native public LocalDateTime plus(TemporalAmount amountToAdd);

	native public LocalDateTime plus(long amountToAdd, TemporalUnit unit);

	native public LocalDateTime plusYears(long years);

	native public LocalDateTime plusMonths(long months);

	native public LocalDateTime plusWeeks(long weeks);

	native public LocalDateTime plusDays(long days);

	native public LocalDateTime plusHours(long hours);

	native public LocalDateTime plusMinutes(long minutes);

	native public LocalDateTime plusSeconds(long seconds);

	native public LocalDateTime plusNanos(long nanos);

	native public LocalDateTime minus(TemporalAmount amountToSubtract);

	native public LocalDateTime minus(long amountToSubtract, TemporalUnit unit);

	native public LocalDateTime minusYears(long years);

	native public LocalDateTime minusMonths(long months);

	native public LocalDateTime minusWeeks(long weeks);

	native public LocalDateTime minusDays(long days);

	native public LocalDateTime minusHours(long hours);

	native public LocalDateTime minusMinutes(long minutes);

	native public LocalDateTime minusSeconds(long seconds);

	native public LocalDateTime minusNanos(long nanos);

	native public <R> R query(TemporalQuery<R> query);

	native public Temporal adjustInto(Temporal temporal);

	native public long until(Temporal endExclusive, TemporalUnit unit);

	native public String format(DateTimeFormatter formatter);

	native public OffsetDateTime atOffset(ZoneOffset offset);

	native public ZonedDateTime atZone(ZoneId zone);

	native public int compareTo(ChronoLocalDateTime<?> other);

	native public boolean isAfter(ChronoLocalDateTime<?> other);

	native public boolean isBefore(ChronoLocalDateTime<?> other);

	native public boolean isEqual(ChronoLocalDateTime<?> other);

	native public boolean equals(Object obj);

	native public int hashCode();

	native public String toString();
}
