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

public interface ChronoZonedDateTime<D extends ChronoLocalDate> extends Temporal, Comparable<ChronoZonedDateTime<?>> {
	static Comparator<ChronoZonedDateTime<?>> timeLineOrder() {
		throw new RuntimeException("Not implemented");
	}

	static ChronoZonedDateTime<?> from(TemporalAccessor temporal) {
		throw new RuntimeException("Not implemented");
	}

	ChronoLocalDateTime<D> toLocalDateTime();

	ZoneOffset getOffset();

	ZoneId getZone();

	ChronoZonedDateTime<D> withEarlierOffsetAtOverlap();

	ChronoZonedDateTime<D> withLaterOffsetAtOverlap();

	ChronoZonedDateTime<D> withZoneSameLocal(ZoneId zone);

	ChronoZonedDateTime<D> withZoneSameInstant(ZoneId zone);

	boolean isSupported(TemporalField field);

	ChronoZonedDateTime<D> with(TemporalField field, long newValue);

	ChronoZonedDateTime<D> plus(long amountToAdd, TemporalUnit unit);

	boolean equals(Object obj);

	int hashCode();

	String toString();

	default ValueRange range(TemporalField field) {
		throw new RuntimeException("Not implemented");
	}

	default int get(TemporalField field) {
		throw new RuntimeException("Not implemented");
	}

	default long getLong(TemporalField field) {
		throw new RuntimeException("Not implemented");
	}

	default D toLocalDate() {
		throw new RuntimeException("Not implemented");
	}

	default LocalTime toLocalTime() {
		throw new RuntimeException("Not implemented");
	}

	default Chronology getChronology() {
		throw new RuntimeException("Not implemented");
	}

	default boolean isSupported(TemporalUnit unit) {
		throw new RuntimeException("Not implemented");
	}

	default ChronoZonedDateTime<D> with(TemporalAdjuster adjuster) {
		throw new RuntimeException("Not implemented");
	}

	default ChronoZonedDateTime<D> plus(TemporalAmount amount) {
		throw new RuntimeException("Not implemented");
	}

	default ChronoZonedDateTime<D> minus(TemporalAmount amount) {
		throw new RuntimeException("Not implemented");
	}

	default ChronoZonedDateTime<D> minus(long amountToSubtract, TemporalUnit unit) {
		throw new RuntimeException("Not implemented");
	}

	default <R> R query(TemporalQuery<R> query) {
		throw new RuntimeException("Not implemented");
	}

	default String format(DateTimeFormatter formatter) {
		throw new RuntimeException("Not implemented");
	}

	default Instant toInstant() {
		throw new RuntimeException("Not implemented");
	}

	default long toEpochSecond() {
		throw new RuntimeException("Not implemented");
	}

	default int compareTo(ChronoZonedDateTime<?> other) {
		throw new RuntimeException("Not implemented");
	}

	default boolean isBefore(ChronoZonedDateTime<?> other) {
		throw new RuntimeException("Not implemented");
	}

	default boolean isAfter(ChronoZonedDateTime<?> other) {
		throw new RuntimeException("Not implemented");
	}

	default boolean isEqual(ChronoZonedDateTime<?> other) {
		throw new RuntimeException("Not implemented");
	}

}
