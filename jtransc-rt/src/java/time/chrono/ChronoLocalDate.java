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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.util.Comparator;

public interface ChronoLocalDate extends Temporal, TemporalAdjuster, Comparable<ChronoLocalDate> {
	static Comparator<ChronoLocalDate> timeLineOrder() {
		throw new RuntimeException("Not implemented");
	}

	static ChronoLocalDate from(TemporalAccessor temporal) {
		throw new RuntimeException("Not implemented");
	}

	Chronology getChronology();

	int lengthOfMonth();

	long until(Temporal endExclusive, TemporalUnit unit);

	ChronoPeriod until(ChronoLocalDate endDateExclusive);

	boolean equals(Object obj);

	int hashCode();

	String toString();

	default Era getEra() {
		throw new RuntimeException("Not implemented");
	}

	default boolean isLeapYear() {
		throw new RuntimeException("Not implemented");
	}

	default int lengthOfYear() {
		throw new RuntimeException("Not implemented");
	}

	default boolean isSupported(TemporalField field) {
		throw new RuntimeException("Not implemented");
	}

	default boolean isSupported(TemporalUnit unit) {
		throw new RuntimeException("Not implemented");
	}

	default ChronoLocalDate with(TemporalAdjuster adjuster) {
		throw new RuntimeException("Not implemented");
	}

	default ChronoLocalDate with(TemporalField field, long newValue) {
		throw new RuntimeException("Not implemented");
	}

	default ChronoLocalDate plus(TemporalAmount amount) {
		throw new RuntimeException("Not implemented");
	}

	default ChronoLocalDate plus(long amountToAdd, TemporalUnit unit) {
		throw new RuntimeException("Not implemented");
	}

	default ChronoLocalDate minus(TemporalAmount amount) {
		throw new RuntimeException("Not implemented");
	}

	default ChronoLocalDate minus(long amountToSubtract, TemporalUnit unit) {
		throw new RuntimeException("Not implemented");
	}

	default <R> R query(TemporalQuery<R> query) {
		throw new RuntimeException("Not implemented");
	}

	default Temporal adjustInto(Temporal temporal) {
		throw new RuntimeException("Not implemented");
	}

	default String format(DateTimeFormatter formatter) {
		throw new RuntimeException("Not implemented");
	}

	default ChronoLocalDateTime<?> atTime(LocalTime localTime) {
		throw new RuntimeException("Not implemented");
	}

	default long toEpochDay() {
		throw new RuntimeException("Not implemented");
	}

	default int compareTo(ChronoLocalDate other) {
		throw new RuntimeException("Not implemented");
	}

	default boolean isAfter(ChronoLocalDate other) {
		throw new RuntimeException("Not implemented");
	}

	default boolean isBefore(ChronoLocalDate other) {
		throw new RuntimeException("Not implemented");
	}

	default boolean isEqual(ChronoLocalDate other) {
		throw new RuntimeException("Not implemented");
	}

}
