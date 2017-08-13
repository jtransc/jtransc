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
package java.time.chrono;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.ResolverStyle;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.ValueRange;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public interface Chronology extends Comparable<Chronology> {
	static Chronology from(TemporalAccessor temporal) {
		throw new RuntimeException("Not implemented");
	}

	static Chronology ofLocale(Locale locale) {
		throw new RuntimeException("Not implemented");
	}

	static Chronology of(String id) {
		throw new RuntimeException("Not implemented");
	}

	static Set<Chronology> getAvailableChronologies() {
		throw new RuntimeException("Not implemented");
	}

	String getId();

	String getCalendarType();

	default ChronoLocalDate date(Era era, int yearOfEra, int month, int dayOfMonth) {
		throw new RuntimeException("Not implemented");
	}

	ChronoLocalDate date(int prolepticYear, int month, int dayOfMonth);

	default ChronoLocalDate dateYearDay(Era era, int yearOfEra, int dayOfYear) {
		throw new RuntimeException("Not implemented");
	}

	ChronoLocalDate dateYearDay(int prolepticYear, int dayOfYear);

	ChronoLocalDate dateEpochDay(long epochDay);

	default ChronoLocalDate dateNow() {
		throw new RuntimeException("Not implemented");
	}

	default ChronoLocalDate dateNow(ZoneId zone) {
		throw new RuntimeException("Not implemented");
	}

	default ChronoLocalDate dateNow(Clock clock) {
		throw new RuntimeException("Not implemented");
	}

	ChronoLocalDate date(TemporalAccessor temporal);

	default ChronoLocalDateTime<? extends ChronoLocalDate> localDateTime(TemporalAccessor temporal) {
		throw new RuntimeException("Not implemented");
	}

	default ChronoZonedDateTime<? extends ChronoLocalDate> zonedDateTime(TemporalAccessor temporal) {
		throw new RuntimeException("Not implemented");
	}

	default ChronoZonedDateTime<? extends ChronoLocalDate> zonedDateTime(Instant instant, ZoneId zone) {
		throw new RuntimeException("Not implemented");
	}

	boolean isLeapYear(long prolepticYear);

	int prolepticYear(Era era, int yearOfEra);

	Era eraOf(int eraValue);

	List<Era> eras();

	ValueRange range(ChronoField field);

	default String getDisplayName(TextStyle style, Locale locale) {
		throw new RuntimeException("Not implemented");
	}

	ChronoLocalDate resolveDate(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle);

	default ChronoPeriod period(int years, int months, int days) {
		throw new RuntimeException("Not implemented");
	}

	int compareTo(Chronology other);

	boolean equals(Object obj);

	int hashCode();

	String toString();

}
