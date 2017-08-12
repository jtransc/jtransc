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

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.util.Comparator;

public interface ChronoLocalDateTime<D extends ChronoLocalDate> extends Temporal, TemporalAdjuster, Comparable<ChronoLocalDateTime<?>> {

	static Comparator<ChronoLocalDateTime<?>> timeLineOrder() {
		throw new RuntimeException("Not implemented");
	}

	static ChronoLocalDateTime<?> from(TemporalAccessor temporal) {
		throw new RuntimeException("Not implemented");
	}

	D toLocalDate();

	LocalTime toLocalTime();

	boolean isSupported(TemporalField field);

	ChronoLocalDateTime<D> with(TemporalField field, long newValue);

	ChronoLocalDateTime<D> plus(long amountToAdd, TemporalUnit unit);

	ChronoZonedDateTime<D> atZone(ZoneId zone);

	boolean equals(Object obj);

	int hashCode();

	String toString();

	default Chronology getChronology() {
		throw new RuntimeException("Not implemented");
	}

	default boolean isSupported(TemporalUnit unit) {
		throw new RuntimeException("Not implemented");
	}

	default ChronoLocalDateTime<D> with(TemporalAdjuster adjuster) {
		throw new RuntimeException("Not implemented");
	}

	default ChronoLocalDateTime<D> plus(TemporalAmount amount) {
		throw new RuntimeException("Not implemented");
	}

	default ChronoLocalDateTime<D> minus(TemporalAmount amount) {
		throw new RuntimeException("Not implemented");
	}

	default ChronoLocalDateTime<D> minus(long amountToSubtract, TemporalUnit unit) {
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

	default Instant toInstant(ZoneOffset offset) {
		throw new RuntimeException("Not implemented");
	}

	default long toEpochSecond(ZoneOffset offset) {
		throw new RuntimeException("Not implemented");
	}

	default int compareTo(ChronoLocalDateTime<?> other) {
		throw new RuntimeException("Not implemented");
	}

	default boolean isAfter(ChronoLocalDateTime<?> other) {
		throw new RuntimeException("Not implemented");
	}

	default boolean isBefore(ChronoLocalDateTime<?> other) {
		throw new RuntimeException("Not implemented");
	}

	default boolean isEqual(ChronoLocalDateTime<?> other) {
		throw new RuntimeException("Not implemented");
	}
}
