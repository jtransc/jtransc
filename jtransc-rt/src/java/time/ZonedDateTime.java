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
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;

public final class ZonedDateTime implements Temporal, ChronoZonedDateTime<LocalDate>, Serializable {
	private final LocalDateTime dateTime;
	private final ZoneOffset offset;
	private final ZoneId zone;

	private ZonedDateTime(LocalDateTime dateTime, ZoneOffset offset, ZoneId zone) {
		this.dateTime = dateTime;
		this.offset = offset;
		this.zone = zone;
	}

	native public static ZonedDateTime now();

	native public static ZonedDateTime now(ZoneId zone);

	native public static ZonedDateTime now(Clock clock);

	native public static ZonedDateTime of(LocalDate date, LocalTime time, ZoneId zone);

	native public static ZonedDateTime of(LocalDateTime localDateTime, ZoneId zone);

	native public static ZonedDateTime of(int year, int month, int dayOfMonth, int hour, int minute, int second, int nanoOfSecond, ZoneId zone);

	native public static ZonedDateTime ofLocal(LocalDateTime localDateTime, ZoneId zone, ZoneOffset preferredOffset);

	native public static ZonedDateTime ofInstant(Instant instant, ZoneId zone);

	native public static ZonedDateTime ofInstant(LocalDateTime localDateTime, ZoneOffset offset, ZoneId zone);

	native public static ZonedDateTime ofStrict(LocalDateTime localDateTime, ZoneOffset offset, ZoneId zone);

	native public static ZonedDateTime from(TemporalAccessor temporal);

	native public static ZonedDateTime parse(CharSequence text);

	native public static ZonedDateTime parse(CharSequence text, DateTimeFormatter formatter);

	native public boolean isSupported(TemporalField field);

	native public boolean isSupported(TemporalUnit unit);

	native public ValueRange range(TemporalField field);

	native public int get(TemporalField field);

	native public long getLong(TemporalField field);

	native public ZoneOffset getOffset();

	native public ZonedDateTime withEarlierOffsetAtOverlap();

	native public ZonedDateTime withLaterOffsetAtOverlap();

	native public ZoneId getZone();

	native public ZonedDateTime withZoneSameLocal(ZoneId zone);

	native public ZonedDateTime withZoneSameInstant(ZoneId zone);

	native public ZonedDateTime withFixedOffsetZone();

	native public LocalDateTime toLocalDateTime();

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

	native public ZonedDateTime with(TemporalAdjuster adjuster);

	native public ZonedDateTime with(TemporalField field, long newValue);

	native public ZonedDateTime withYear(int year);

	native public ZonedDateTime withMonth(int month);

	native public ZonedDateTime withDayOfMonth(int dayOfMonth);

	native public ZonedDateTime withDayOfYear(int dayOfYear);

	native public ZonedDateTime withHour(int hour);

	native public ZonedDateTime withMinute(int minute);

	native public ZonedDateTime withSecond(int second);

	native public ZonedDateTime withNano(int nanoOfSecond);

	native public ZonedDateTime truncatedTo(TemporalUnit unit);

	native public ZonedDateTime plus(TemporalAmount amountToAdd);

	native public ZonedDateTime plus(long amountToAdd, TemporalUnit unit);

	native public ZonedDateTime plusYears(long years);

	native public ZonedDateTime plusMonths(long months);

	native public ZonedDateTime plusWeeks(long weeks);

	native public ZonedDateTime plusDays(long days);

	native public ZonedDateTime plusHours(long hours);

	native public ZonedDateTime plusMinutes(long minutes);

	native public ZonedDateTime plusSeconds(long seconds);

	native public ZonedDateTime plusNanos(long nanos);

	native public ZonedDateTime minus(TemporalAmount amountToSubtract);

	native public ZonedDateTime minus(long amountToSubtract, TemporalUnit unit);

	native public ZonedDateTime minusYears(long years);

	native public ZonedDateTime minusMonths(long months);

	native public ZonedDateTime minusWeeks(long weeks);

	native public ZonedDateTime minusDays(long days);

	native public ZonedDateTime minusHours(long hours);

	native public ZonedDateTime minusMinutes(long minutes);

	native public ZonedDateTime minusSeconds(long seconds);

	native public ZonedDateTime minusNanos(long nanos);

	native public <R> R query(TemporalQuery<R> query);

	native public long until(Temporal endExclusive, TemporalUnit unit);

	native public String format(DateTimeFormatter formatter);

	native public OffsetDateTime toOffsetDateTime();

	native public boolean equals(Object obj);

	native public int hashCode();

	native public String toString();
}
