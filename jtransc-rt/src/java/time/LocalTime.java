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

public final class LocalTime implements Temporal, TemporalAdjuster, Comparable<LocalTime>, Serializable {
	public static final LocalTime MIN = new LocalTime(0, 0, 0, 0);
	public static final LocalTime MAX = new LocalTime(23, 59, 59, 999_999_999);
	public static final LocalTime MIDNIGHT = new LocalTime(0, 0, 0, 0);
	public static final LocalTime NOON = new LocalTime(12, 0, 0, 0);

	private final byte hour;
	private final byte minute;
	private final byte second;
	private final int nano;

	private LocalTime(int hour, int minute, int second, int nanoOfSecond) {
		this.hour = (byte) hour;
		this.minute = (byte) minute;
		this.second = (byte) second;
		this.nano = nanoOfSecond;
	}

	native public static LocalTime now();

	native public static LocalTime now(ZoneId zone);

	native public static LocalTime now(Clock clock);

	native public static LocalTime of(int hour, int minute);

	native public static LocalTime of(int hour, int minute, int second);

	native public static LocalTime of(int hour, int minute, int second, int nanoOfSecond);

	native public static LocalTime ofSecondOfDay(long secondOfDay);

	native public static LocalTime ofNanoOfDay(long nanoOfDay);

	native public static LocalTime from(TemporalAccessor temporal);

	native public static LocalTime parse(CharSequence text);

	native public static LocalTime parse(CharSequence text, DateTimeFormatter formatter);

	native public boolean isSupported(TemporalField field);

	native public boolean isSupported(TemporalUnit unit);

	native public ValueRange range(TemporalField field);

	native public int get(TemporalField field);

	native public long getLong(TemporalField field);

	native public int getHour();

	native public int getMinute();

	native public int getSecond();

	native public int getNano();

	native public LocalTime with(TemporalAdjuster adjuster);

	native public LocalTime with(TemporalField field, long newValue);

	native public LocalTime withHour(int hour);

	native public LocalTime withMinute(int minute);

	native public LocalTime withSecond(int second);

	native public LocalTime withNano(int nanoOfSecond);

	native public LocalTime truncatedTo(TemporalUnit unit);

	native public LocalTime plus(TemporalAmount amountToAdd);

	native public LocalTime plus(long amountToAdd, TemporalUnit unit);

	native public LocalTime plusHours(long hoursToAdd);

	native public LocalTime plusMinutes(long minutesToAdd);

	native public LocalTime plusSeconds(long secondstoAdd);

	native public LocalTime plusNanos(long nanosToAdd);

	native public LocalTime minus(TemporalAmount amountToSubtract);

	native public LocalTime minus(long amountToSubtract, TemporalUnit unit);

	native public LocalTime minusHours(long hoursToSubtract);

	native public LocalTime minusMinutes(long minutesToSubtract);

	native public LocalTime minusSeconds(long secondsToSubtract);

	native public LocalTime minusNanos(long nanosToSubtract);

	native public <R> R query(TemporalQuery<R> query);

	native public Temporal adjustInto(Temporal temporal);

	native public long until(Temporal endExclusive, TemporalUnit unit);

	native public String format(DateTimeFormatter formatter);

	native public LocalDateTime atDate(LocalDate date);

	native public OffsetTime atOffset(ZoneOffset offset);

	native public int toSecondOfDay();

	native public long toNanoOfDay();

	native public int compareTo(LocalTime other);

	native public boolean isAfter(LocalTime other);

	native public boolean isBefore(LocalTime other);

	native public boolean equals(Object obj);

	native public int hashCode();

	native public String toString();
}