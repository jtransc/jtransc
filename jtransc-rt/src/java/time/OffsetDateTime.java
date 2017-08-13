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
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.util.Comparator;

public final class OffsetDateTime implements Temporal, TemporalAdjuster, Comparable<OffsetDateTime>, Serializable {
	public static final OffsetDateTime MIN = new OffsetDateTime(null, null);
	public static final OffsetDateTime MAX = new OffsetDateTime(null, null);

	native public static Comparator<OffsetDateTime> timeLineOrder();

	private final LocalDateTime dateTime;
	private final ZoneOffset offset;

	private OffsetDateTime(LocalDateTime dateTime, ZoneOffset offset) {
		this.dateTime = dateTime;
		this.offset = offset;
	}

	native public static OffsetDateTime now();

	native public static OffsetDateTime now(ZoneId zone);

	native public static OffsetDateTime now(Clock clock);

	native public static OffsetDateTime of(LocalDate date, LocalTime time, ZoneOffset offset);

	native public static OffsetDateTime of(LocalDateTime dateTime, ZoneOffset offset);

	native public static OffsetDateTime of(int year, int month, int dayOfMonth, int hour, int minute, int second, int nanoOfSecond, ZoneOffset offset);

	native public static OffsetDateTime ofInstant(Instant instant, ZoneId zone);

	native public static OffsetDateTime from(TemporalAccessor temporal);

	native public static OffsetDateTime parse(CharSequence text);

	native public static OffsetDateTime parse(CharSequence text, DateTimeFormatter formatter);

	native public boolean isSupported(TemporalField field);

	native public boolean isSupported(TemporalUnit unit);

	native public ValueRange range(TemporalField field);

	native public int get(TemporalField field);

	native public long getLong(TemporalField field);

	native public ZoneOffset getOffset();

	native public OffsetDateTime withOffsetSameLocal(ZoneOffset offset);

	native public OffsetDateTime withOffsetSameInstant(ZoneOffset offset);

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

	native public OffsetDateTime with(TemporalAdjuster adjuster);

	native public OffsetDateTime with(TemporalField field, long newValue);

	native public OffsetDateTime withYear(int year);

	native public OffsetDateTime withMonth(int month);

	native public OffsetDateTime withDayOfMonth(int dayOfMonth);

	native public OffsetDateTime withDayOfYear(int dayOfYear);

	native public OffsetDateTime withHour(int hour);

	native public OffsetDateTime withMinute(int minute);

	native public OffsetDateTime withSecond(int second);

	native public OffsetDateTime withNano(int nanoOfSecond);

	native public OffsetDateTime truncatedTo(TemporalUnit unit);

	native public OffsetDateTime plus(TemporalAmount amountToAdd);

	native public OffsetDateTime plus(long amountToAdd, TemporalUnit unit);

	native public OffsetDateTime plusYears(long years);

	native public OffsetDateTime plusMonths(long months);

	native public OffsetDateTime plusWeeks(long weeks);

	native public OffsetDateTime plusDays(long days);

	native public OffsetDateTime plusHours(long hours);

	native public OffsetDateTime plusMinutes(long minutes);

	native public OffsetDateTime plusSeconds(long seconds);

	native public OffsetDateTime plusNanos(long nanos);

	native public OffsetDateTime minus(TemporalAmount amountToSubtract);

	native public OffsetDateTime minus(long amountToSubtract, TemporalUnit unit);

	native public OffsetDateTime minusYears(long years);

	native public OffsetDateTime minusMonths(long months);

	native public OffsetDateTime minusWeeks(long weeks);

	native public OffsetDateTime minusDays(long days);

	native public OffsetDateTime minusHours(long hours);

	native public OffsetDateTime minusMinutes(long minutes);

	native public OffsetDateTime minusSeconds(long seconds);

	native public OffsetDateTime minusNanos(long nanos);

	native public <R> R query(TemporalQuery<R> query);

	native public Temporal adjustInto(Temporal temporal);

	native public long until(Temporal endExclusive, TemporalUnit unit);

	native public String format(DateTimeFormatter formatter);

	native public ZonedDateTime atZoneSameInstant(ZoneId zone);

	native public ZonedDateTime atZoneSimilarLocal(ZoneId zone);

	native public OffsetTime toOffsetTime();

	native public ZonedDateTime toZonedDateTime();

	native public Instant toInstant();

	native public long toEpochSecond();

	native public int compareTo(OffsetDateTime other);

	native public boolean isAfter(OffsetDateTime other);

	native public boolean isBefore(OffsetDateTime other);

	native public boolean isEqual(OffsetDateTime other);

	native public boolean equals(Object obj);

	native public int hashCode();

	native public String toString();
}
