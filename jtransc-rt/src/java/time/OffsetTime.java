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

public final class OffsetTime implements Temporal, TemporalAdjuster, Comparable<OffsetTime>, Serializable {
	public static final OffsetTime MIN = new OffsetTime(LocalTime.MIN, ZoneOffset.MAX);
	public static final OffsetTime MAX = new OffsetTime(LocalTime.MAX, ZoneOffset.MIN);

	private final LocalTime time;
	private final ZoneOffset offset;

	public OffsetTime(LocalTime time, ZoneOffset offset) {
		this.time = time;
		this.offset = offset;
	}

	native public static OffsetTime now();

	native public static OffsetTime now(ZoneId zone);

	native public static OffsetTime now(Clock clock);

	native public static OffsetTime of(LocalTime time, ZoneOffset offset);

	native public static OffsetTime of(int hour, int minute, int second, int nanoOfSecond, ZoneOffset offset);

	native public static OffsetTime ofInstant(Instant instant, ZoneId zone);

	native public static OffsetTime from(TemporalAccessor temporal);

	native public static OffsetTime parse(CharSequence text);

	native public static OffsetTime parse(CharSequence text, DateTimeFormatter formatter);

	native public boolean isSupported(TemporalField field);

	native public boolean isSupported(TemporalUnit unit);

	native public ValueRange range(TemporalField field);

	native public int get(TemporalField field);

	native public long getLong(TemporalField field);

	native public ZoneOffset getOffset();

	native public OffsetTime withOffsetSameLocal(ZoneOffset offset);

	native public OffsetTime withOffsetSameInstant(ZoneOffset offset);

	native public LocalTime toLocalTime();

	native public int getHour();

	native public int getMinute();

	native public int getSecond();

	native public int getNano();

	native public OffsetTime with(TemporalAdjuster adjuster);

	native public OffsetTime with(TemporalField field, long newValue);

	native public OffsetTime withHour(int hour);

	native public OffsetTime withMinute(int minute);

	native public OffsetTime withSecond(int second);

	native public OffsetTime withNano(int nanoOfSecond);

	native public OffsetTime truncatedTo(TemporalUnit unit);

	native public OffsetTime plus(TemporalAmount amountToAdd);

	native public OffsetTime plus(long amountToAdd, TemporalUnit unit);

	native public OffsetTime plusHours(long hours);

	native public OffsetTime plusMinutes(long minutes);

	native public OffsetTime plusSeconds(long seconds);

	native public OffsetTime plusNanos(long nanos);

	native public OffsetTime minus(TemporalAmount amountToSubtract);

	native public OffsetTime minus(long amountToSubtract, TemporalUnit unit);

	native public OffsetTime minusHours(long hours);

	native public OffsetTime minusMinutes(long minutes);

	native public OffsetTime minusSeconds(long seconds);

	native public OffsetTime minusNanos(long nanos);

	native public <R> R query(TemporalQuery<R> query);

	native public Temporal adjustInto(Temporal temporal);

	native public long until(Temporal endExclusive, TemporalUnit unit);

	native public String format(DateTimeFormatter formatter);

	native public OffsetDateTime atDate(LocalDate date);

	native public int compareTo(OffsetTime other);

	native public boolean isAfter(OffsetTime other);

	native public boolean isBefore(OffsetTime other);

	native public boolean isEqual(OffsetTime other);

	native public boolean equals(Object obj);

	native public int hashCode();

	native public String toString();
}
